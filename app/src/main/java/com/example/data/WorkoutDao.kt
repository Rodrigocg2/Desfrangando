package com.example.data

import androidx.room.*
import com.example.model.SavedWorkout
import com.example.model.WorkoutHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM saved_workouts ORDER BY dateCreated DESC")
    fun getAllSavedWorkouts(): Flow<List<SavedWorkout>>

    @Query("SELECT * FROM saved_workouts WHERE id = :id LIMIT 1")
    suspend fun getSavedWorkoutById(id: String): SavedWorkout?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedWorkout(workout: SavedWorkout)

    @Query("DELETE FROM saved_workouts WHERE id = :id")
    suspend fun deleteSavedWorkoutById(id: String)

    @Query("DELETE FROM saved_workouts")
    suspend fun deleteAllSavedWorkouts()

    @Query("SELECT * FROM workout_history ORDER BY dateCompleted DESC")
    fun getWorkoutHistory(): Flow<List<WorkoutHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutHistory(history: WorkoutHistory)

    @Query("DELETE FROM workout_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM workout_history")
    suspend fun clearAllHistory()

    @Query("SELECT * FROM weight_history ORDER BY dateRecorded DESC")
    fun getAllWeightHistory(): Flow<List<com.example.model.WeightHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightHistory(weight: com.example.model.WeightHistory)

    @Query("DELETE FROM weight_history WHERE id = :id")
    suspend fun deleteWeightHistoryById(id: Long)

    @Query("SELECT * FROM cached_youtube_videos WHERE exerciseName = :exerciseName LIMIT 1")
    suspend fun getCachedVideo(exerciseName: String): com.example.model.CachedYouTubeVideo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedVideo(video: com.example.model.CachedYouTubeVideo)

    @Query("DELETE FROM cached_youtube_videos")
    suspend fun clearCachedVideos()
}
