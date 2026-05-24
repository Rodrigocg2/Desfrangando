package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

@com.squareup.moshi.JsonClass(generateAdapter = true)
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
    companion object {
        private val moshi: Moshi = Moshi.Builder().build()
        private val listType = Types.newParameterizedType(List::class.java, WorkoutExercise::class.java)
        private val adapter = moshi.adapter<List<WorkoutExercise>>(listType)
    }

    fun getExercises(): List<WorkoutExercise> {
        return try {
            adapter.fromJson(exercisesJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

@com.squareup.moshi.JsonClass(generateAdapter = true)
@Entity(tableName = "workout_history")
data class WorkoutHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: String,
    val title: String,
    val dateCompleted: Long = System.currentTimeMillis(),
    val durationMinutes: Int,
    val totalVolumeKg: Double,
    val completionJson: String // Serialized List<ExerciseCompletion>
) {
    companion object {
        private val moshi: com.squareup.moshi.Moshi = com.squareup.moshi.Moshi.Builder().build()
        private val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, ExerciseCompletion::class.java)
        private val adapter = moshi.adapter<List<ExerciseCompletion>>(listType)
    }

    fun getCompletions(): List<ExerciseCompletion> {
        return try {
            adapter.fromJson(completionJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

@com.squareup.moshi.JsonClass(generateAdapter = true)
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

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class ExerciseCompletion(
    val exerciseId: String,
    val name: String,
    val setsCompleted: List<SetRecord>
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class SetRecord(
    val setNumber: Int,
    val weightKg: Double,
    val repsCompleted: Int,
    val wasRpeMet: Boolean
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class RoutineCategory(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val emoji: String = "📁",
    val colorHex: String = "#8B5CF6", // default purple
    val iconResName: String = "" // Placeholder for custom icons if needed
)

// Base Exercise definition for execution reference
data class ExerciseExecutionReference(
    val id: String,
    val name: String,
    val primaryMuscleCode: String, // LATS, CHEST, QUAD, HAMSTRING, SHOULDER, BICEPS, TRICEPS, LOWER_BACK, GLUTES, CALVES, ABS, CARDIO
    val primaryMuscleName: String,
    val secondaryMuscleName: String = "",
    val equipment: String = "Livre", // Livre, Halter, Barra, Máquina, Cabo, Smith, Peso do Corpo, Cardio
    val difficulty: String = "Iniciante", // Iniciante, Intermediário, Avançado
    val type: String = "Composto", // Composto, Isolador, Cardio
    val executionDetails: List<String>, // Step by step
    val highPerformanceTips: List<String>, // Pro tips for high-tension and stimulus
    val commonMistakes: List<String> = emptyList(), // Erros comuns
    val biomechanicalTempo: String, // recommended tempo
    val jointPathType: String, // CHEST_PRESS, DEAD_LIFT, SQUAT, LAT_PULLDOWN, LATERAL_RAISE, BICEPS_CURL, TRICEPS_EXTENSION, LEG_PRESS
    val gifUrl: String = "",
    val imageUrl: String = ""
)

@Entity(tableName = "weight_history")
data class WeightHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weightKg: Float,
    val dateRecorded: Long = System.currentTimeMillis()
)

