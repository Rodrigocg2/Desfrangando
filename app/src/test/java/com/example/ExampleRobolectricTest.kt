package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.api.GeminiClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Desfrangando", appName)
  }

  @Test
  fun `test fallback workout generation all configurations`() {
    val splits = listOf(
      "ABC_DENSIDADE", "PPL_PUSH", "PPL_PULL", "PPL_LEGS", "UPPER_LOWER",
      "MISTO_SUP_INF", "ARNOLD_SPLIT", "PONTO_FRACO", "MISTO_PERNA_BRACO_PEITO"
    )
    val objectives = listOf("Hipertrofia", "Força", "Emagrecimento", "Definição", "Condicionamento")
    val periods = listOf(3, 4, 5, 6)
    
    val moshi = com.squareup.moshi.Moshi.Builder().build()
    val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, com.example.model.WorkoutExercise::class.java)
    val adapter = moshi.adapter<List<com.example.model.WorkoutExercise>>(listType)

    // Exercise is generated sequentially to test all flows
    for (split in splits) {
      for (obj in objectives) {
        for (days in periods) {
          val result = runBlocking {
            GeminiClient.generateWorkout(
              splitType = split,
              focus = obj,
              specialNotes = "Duração de 60 min",
              experienceLevel = "Avançado",
              workoutsPerDay = 1,
              workoutsPerWeek = days
            )
          }
          assertNotNull(result)
          if (result is com.example.api.GeneratedWorkoutResult.Success) {
            for (work in result.cycle) {
              val json = adapter.toJson(work.exercises)
              assertNotNull(json)
              val parsed = adapter.fromJson(json)
              assertNotNull(parsed)
              assertEquals(work.exercises.size, parsed!!.size)
            }
          }
        }
      }
    }
  }
}

