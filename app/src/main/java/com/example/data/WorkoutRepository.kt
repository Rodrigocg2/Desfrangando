package com.example.data

import com.example.model.ExerciseExecutionReference
import com.example.model.SavedWorkout
import com.example.model.WorkoutHistory
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val workoutDao: WorkoutDao) {

    val allSavedWorkouts: Flow<List<SavedWorkout>> = workoutDao.getAllSavedWorkouts()
    val workoutHistory: Flow<List<WorkoutHistory>> = workoutDao.getWorkoutHistory()
    val weightHistory: Flow<List<com.example.model.WeightHistory>> = workoutDao.getAllWeightHistory()

    suspend fun getSavedWorkoutById(id: String): SavedWorkout? {
        return workoutDao.getSavedWorkoutById(id)
    }

    suspend fun saveWorkout(workout: SavedWorkout) {
        workoutDao.insertSavedWorkout(workout)
    }

    suspend fun deleteWorkoutById(id: String) {
        workoutDao.deleteSavedWorkoutById(id)
    }

    suspend fun deleteAllWorkouts() {
        workoutDao.deleteAllSavedWorkouts()
    }

    suspend fun saveHistory(history: WorkoutHistory) {
         workoutDao.insertWorkoutHistory(history)
    }

    suspend fun deleteHistoryById(id: Long) {
         workoutDao.deleteHistoryById(id)
    }

    suspend fun clearHistory() {
         workoutDao.clearAllHistory()
    }

    suspend fun saveWeight(weight: com.example.model.WeightHistory) {
         workoutDao.insertWeightHistory(weight)
    }

    suspend fun deleteWeightById(id: Long) {
         workoutDao.deleteWeightHistoryById(id)
    }

    // Static Pre-loaded high-intensity references (Videos are animated via Jetpack Compose canvases dynamically)
    val referenceExercises = com.example.data.EXERCISES_DATABASE

    fun getExerciseReferenceById(id: String): ExerciseExecutionReference? {
        return referenceExercises.find { it.id == id }
    }
}
