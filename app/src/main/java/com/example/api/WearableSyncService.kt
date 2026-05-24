package com.example.api

import android.util.Log
import com.example.data.WorkoutDao
import com.example.model.WorkoutHistory
import com.example.model.ExerciseCompletion
import com.example.model.SetRecord
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// Strava API models for actual web HTTP sync
@com.squareup.moshi.JsonClass(generateAdapter = true)
data class StravaActivity(
    val id: Long,
    val name: String,
    val distance: Double,
    val moving_time: Int,
    val elapsed_time: Int,
    val start_date: String,
    val type: String // e.g. "WeightTraining", "Workout", "Run"
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class StravaTokenResponse(
    val access_token: String,
    val refresh_token: String,
    val expires_at: Long,
    val expires_in: Int
)

interface StravaApi {
    @FormUrlEncoded
    @POST("oauth/token")
    suspend fun getAccessToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") code: String,
        @Field("grant_type") grantType: String = "authorization_code"
    ): StravaTokenResponse

    @GET("athlete/activities")
    suspend fun getActivities(
        @Header("Authorization") bearerToken: String,
        @Query("before") before: Long? = null,
        @Query("after") after: Long? = null,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null
    ): List<StravaActivity>
}

// Google Fit REST API models
@com.squareup.moshi.JsonClass(generateAdapter = true)
data class GoogleFitSessionResponse(
    val session: List<GoogleFitSession>
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class GoogleFitSession(
    val id: String,
    val name: String,
    val description: String?,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val activityType: Int
)

interface GoogleFitApi {
    @GET("fitness/v1/users/me/sessions")
    suspend fun getSessions(
        @Header("Authorization") bearerToken: String,
        @Query("startTime") startTime: String? = null,
        @Query("endTime") endTime: String? = null
    ): GoogleFitSessionResponse
}

class WearableSyncManager(private val workoutDao: WorkoutDao) {
    private val TAG = "WearableSyncManager"
    private val moshi = Moshi.Builder().build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val stravaRetrofit = Retrofit.Builder()
        .baseUrl("https://www.strava.com/api/v3/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val googleFitRetrofit = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val stravaApi: StravaApi = stravaRetrofit.create(StravaApi::class.java)
    val googleFitApi: GoogleFitApi = googleFitRetrofit.create(GoogleFitApi::class.java)

    suspend fun syncStrava(accessToken: String): Int = withContext(Dispatchers.IO) {
        try {
            val bearer = "Bearer $accessToken"
            val activities = stravaApi.getActivities(bearer, perPage = 10)
            var newSyncedCount = 0

            activities.forEach { activity ->
                if (activity.type == "WeightTraining" || activity.type == "Workout" || activity.type == "Run" || activity.type == "Gym") {
                    val durationMin = activity.moving_time / 60
                    
                    val completion = ExerciseCompletion(
                        exerciseId = "strava_ex_${activity.id}",
                        name = "Exercício Multi-articular (Via Strava)",
                        setsCompleted = listOf(
                            SetRecord(1, 0.0, 10, true)
                        )
                    )
                    
                    val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, ExerciseCompletion::class.java)
                    val adapter = moshi.adapter<List<ExerciseCompletion>>(listType)
                    val jsonStr = adapter.toJson(listOf(completion)) ?: "[]"

                    val formattedDate = try {
                        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
                            .parse(activity.start_date)?.time ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }

                    val workoutHistory = WorkoutHistory(
                        workoutId = "synced_strava_${activity.id}",
                        title = activity.name,
                        dateCompleted = formattedDate,
                        durationMinutes = if (durationMin > 0) durationMin else 40,
                        totalVolumeKg = 0.0,
                        completionJson = jsonStr
                    )
                    workoutDao.insertWorkoutHistory(workoutHistory)
                    newSyncedCount++
                }
            }
            newSyncedCount
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing Strava: ${e.message}", e)
            throw e
        }
    }

    suspend fun syncGoogleFit(accessToken: String): Int = withContext(Dispatchers.IO) {
        try {
            val bearer = "Bearer $accessToken"
            val response = googleFitApi.getSessions(bearer)
            var newSyncedCount = 0

            response.session.forEach { session ->
                val durationMin = ((session.endTimeMillis - session.startTimeMillis) / 60000).toInt()
                
                val completion = ExerciseCompletion(
                    exerciseId = "googlefit_ex_${session.id}",
                    name = session.name.ifEmpty { "Atividade de Alta Intensidade" },
                    setsCompleted = listOf(
                        SetRecord(1, 0.0, 12, true)
                    )
                )

                val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, ExerciseCompletion::class.java)
                val adapter = moshi.adapter<List<ExerciseCompletion>>(listType)
                val jsonStr = adapter.toJson(listOf(completion)) ?: "[]"

                val workoutHistory = WorkoutHistory(
                    workoutId = "synced_gfit_${session.id}",
                    title = session.name.ifEmpty { "Sessão Smartwatch (Google Fit)" },
                    dateCompleted = session.startTimeMillis,
                    durationMinutes = if (durationMin > 0) durationMin else 45,
                    totalVolumeKg = 0.0,
                    completionJson = jsonStr
                )
                workoutDao.insertWorkoutHistory(workoutHistory)
                newSyncedCount++
            }
            newSyncedCount
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing Google Fit: ${e.message}", e)
            throw e
        }
    }
}
