package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@Entity(tableName = "saved_workouts")
data class SavedWorkout(
    @PrimaryKey val id: String,
    val title: String,
    val splitType: String, // PPL, Arnold, ABCDE, Custom
    val focus: String, // Hipertrofia, Força, Densidade, Isometria
    val dateCreated: Long = System.currentTimeMillis(),
    val exercisesJson: String, // Serialized List<WorkoutExercise>
    val isFavorite: Boolean = false,
    val category: String = "",
    val emoji: String = "💪",
    val colorHex: String = "#8B5CF6"
) {
    fun getExercises(): List<WorkoutExercise> {
        return try {
            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val listType = Types.newParameterizedType(List::class.java, WorkoutExercise::class.java)
            val adapter = moshi.adapter<List<WorkoutExercise>>(listType)
            adapter.fromJson(exercisesJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

@Entity(tableName = "workout_history")
data class WorkoutHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: String,
    val title: String,
    val dateCompleted: Long = System.currentTimeMillis(),
    val durationMinutes: Int,
    val totalVolumeKg: Double,
    val completionJson: String // Serialized List<ExerciseCompletion>
)

data class WorkoutExercise(
    val exerciseId: String,
    val name: String,
    val muscleGroup: String,
    val targetMuscleDetail: String,
    val sets: Int,
    val repsRange: String,
    val tempo: String, // e.g. "4-0-1-0" (eccentric-isometric-concentric-isometric)
    val restSeconds: Int,
    val advancedTechnique: String, // Rest-Pause, Drop-set, Cluster, None
    val intensityRPE: Int, // 1 to 10 scale (RPE)
    val notes: String,
    val trainingPhase: String = "Principal" // Aquecimento, Principal, Acessório, Alongamento
)

data class ExerciseCompletion(
    val exerciseId: String,
    val name: String,
    val setsCompleted: List<SetRecord>
)

data class SetRecord(
    val setNumber: Int,
    val weightKg: Double,
    val repsCompleted: Int,
    val wasRpeMet: Boolean
)

// Base Exercise definition for execution reference
data class ExerciseExecutionReference(
    val id: String,
    val name: String,
    val primaryMuscleCode: String, // LATS, CHEST, QUAD, HAMSTRING, SHOULDER, BICEPS, TRICEPS, LOWER_BACK
    val primaryMuscleName: String,
    val executionDetails: List<String>, // Step by step
    val highPerformanceTips: List<String>, // Pro tips for high-tension and stimulus
    val biomechanicalTempo: String, // recommended tempo
    val jointPathType: String // CHEST_PRESS, DEAD_LIFT, SQUAT, LAT_PULLDOWN, LATERAL_RAISE, BICEPS_CURL, TRICEPS_EXTENSION, LEG_PRESS
)

@Entity(tableName = "weight_history")
data class WeightHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weightKg: Float,
    val dateRecorded: Long = System.currentTimeMillis()
)

