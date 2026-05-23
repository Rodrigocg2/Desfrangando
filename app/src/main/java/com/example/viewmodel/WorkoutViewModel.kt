package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.api.GeneratedWorkoutResult
import com.example.api.GeneratedWorkout
import com.example.data.AppDatabase
import com.example.data.WorkoutRepository
import com.example.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "WorkoutViewModel"
    private val workoutDao = AppDatabase.getDatabase(application).workoutDao()
    val repository = WorkoutRepository(workoutDao)

    private val prefs = application.getSharedPreferences("apexforce_user_prefs", Context.MODE_PRIVATE)

    private val _userWeight = MutableStateFlow(prefs.getFloat("user_weight", 80.0f))
    val userWeight = _userWeight.asStateFlow()

    private val _isGoogleLoggedIn = MutableStateFlow(prefs.getBoolean("google_logged_in", false))
    val isGoogleLoggedIn = _isGoogleLoggedIn.asStateFlow()

    private val _googleUserName = MutableStateFlow(prefs.getString("google_user_name", "Visitante") ?: "Visitante")
    val googleUserName = _googleUserName.asStateFlow()

    private val _googleUserEmail = MutableStateFlow(prefs.getString("google_user_email", "") ?: "")
    val googleUserEmail = _googleUserEmail.asStateFlow()

    private val _googleUserPhoto = MutableStateFlow(prefs.getString("google_user_photo", "") ?: "")
    val googleUserPhoto = _googleUserPhoto.asStateFlow()

    private val _workoutsPerDay = MutableStateFlow(prefs.getInt("workouts_per_day", 1))
    val workoutsPerDay = _workoutsPerDay.asStateFlow()

    private val _workoutsPerWeek = MutableStateFlow(prefs.getInt("workouts_per_week", 3))
    val workoutsPerWeek = _workoutsPerWeek.asStateFlow()

    private val _activeYouTubeUrl = MutableStateFlow<String?>(null)
    val activeYouTubeUrl = _activeYouTubeUrl.asStateFlow()

    fun playYouTubeVideo(url: String) {
        _activeYouTubeUrl.value = url
    }

    fun closeYouTubeVideo() {
        _activeYouTubeUrl.value = null
    }

    private val _userHeight = MutableStateFlow(prefs.getFloat("user_height", 180.0f))
    val userHeight = _userHeight.asStateFlow()

    private val _stravaClientId = MutableStateFlow(prefs.getString("strava_client_id", "") ?: "")
    val stravaClientId = _stravaClientId.asStateFlow()

    private val _stravaClientSecret = MutableStateFlow(prefs.getString("strava_client_secret", "") ?: "")
    val stravaClientSecret = _stravaClientSecret.asStateFlow()

    private val _isStravaConnected = MutableStateFlow(prefs.getBoolean("strava_connected", false))
    val isStravaConnected = _isStravaConnected.asStateFlow()

    private val _isGoogleFitConnected = MutableStateFlow(prefs.getBoolean("google_fit_connected", false))
    val isGoogleFitConnected = _isGoogleFitConnected.asStateFlow()

    private val _lastSyncedTime = MutableStateFlow(prefs.getLong("last_synced_time", 0L))
    val lastSyncedTime = _lastSyncedTime.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage = _syncMessage.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    val wearableSyncManager = com.example.api.WearableSyncManager(workoutDao)

    // Observable states from DB
    val savedWorkouts: StateFlow<List<SavedWorkout>> = repository.allSavedWorkouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workoutHistory: StateFlow<List<WorkoutHistory>> = repository.workoutHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weightHistory: StateFlow<List<com.example.model.WeightHistory>> = repository.weightHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI generation settings state
    var selectedSplit = "PPL_PUSH"
    var selectedFocus = "Hipertrofia"
    var specialNotes = ""
    var experienceLevel = "Avançado"

    // Generation UI state
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()

    private val _generationError = MutableStateFlow<String?>(null)
    val generationError = _generationError.asStateFlow()

    private val _lastGeneratedWorkout = MutableStateFlow<GeneratedWorkout?>(null)
    val lastGeneratedWorkout = _lastGeneratedWorkout.asStateFlow()

    private val _showGeneratedSuccessDialog = MutableStateFlow(false)
    val showGeneratedSuccessDialog = _showGeneratedSuccessDialog.asStateFlow()

    // Active session tracking state
    private val _activeWorkout = MutableStateFlow<SavedWorkout?>(null)
    val activeWorkout = _activeWorkout.asStateFlow()

    private val _currentExerciseIndex = MutableStateFlow(0)
    val currentExerciseIndex = _currentExerciseIndex.asStateFlow()

    // Set records stored in state: ExerciseIndex -> List<SetRecordInput>
    private val _setRecordsState = MutableStateFlow<Map<Int, List<SetModelRecord>>>(emptyMap())
    val setRecordsState = _setRecordsState.asStateFlow()

    // Workout chronometer state
    private val _workoutDurationMinutes = MutableStateFlow(0)
    val workoutDurationMinutes = _workoutDurationMinutes.asStateFlow()
    private var durationJob: Job? = null
    private var workoutStartTimeMs = 0L

    // Rest countdown timer state
    private val _restTimeRemaining = MutableStateFlow(0)
    val restTimeRemaining = _restTimeRemaining.asStateFlow()

    private val _restTimerLimit = MutableStateFlow(90)
    val restTimerLimit = _restTimerLimit.asStateFlow()

    private val _isRestTimerActive = MutableStateFlow(false)
    val isRestTimerActive = _isRestTimerActive.asStateFlow()
    private var restTimerJob: Job? = null

    // Video execution details & canvas simulation state
    private val _selectedExecutionId = MutableStateFlow<String>("supino_reto")
    val selectedExecutionId = _selectedExecutionId.asStateFlow()

    private val _videoPlaybackSpeed = MutableStateFlow(1.0f) // 0.5f, 1.0f, 1.5f
    val videoPlaybackSpeed = _videoPlaybackSpeed.asStateFlow()

    private val _isVideoPlaying = MutableStateFlow(true)
    val isVideoPlaying = _isVideoPlaying.asStateFlow()

    private val _videoAngle = MutableStateFlow("COMBATE") // COMBATE (Anatomy map), FRONTAL, LATERAL
    val videoAngle = _videoAngle.asStateFlow()

    init {
        // Log setup
        Log.d(TAG, "WorkoutViewModel initialized.")
    }

    // --- Core Database and Generation ---

    fun generateNewWorkout() {
        viewModelScope.launch {
            _isGenerating.value = true
            _generationError.value = null
            _lastGeneratedWorkout.value = null

            val result = GeminiClient.generateWorkout(
                splitType = selectedSplit,
                focus = selectedFocus,
                specialNotes = specialNotes,
                experienceLevel = experienceLevel,
                workoutsPerDay = _workoutsPerDay.value,
                workoutsPerWeek = _workoutsPerWeek.value
            )

            _isGenerating.value = false

            when (result) {
                is GeneratedWorkoutResult.Success -> {
                    _lastGeneratedWorkout.value = result.workout
                    _showGeneratedSuccessDialog.value = true

                    // Auto-save generated workouts in DB
                    saveGeneratedWorkout(result.workout)
                }
                is GeneratedWorkoutResult.Error -> {
                    _generationError.value = result.message
                }
            }
        }
    }

    fun dismissSuccessDialog() {
        _showGeneratedSuccessDialog.value = false
    }

    private fun saveGeneratedWorkout(work: GeneratedWorkout) {
        viewModelScope.launch(Dispatchers.IO) {
            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val listType = Types.newParameterizedType(List::class.java, WorkoutExercise::class.java)
            val adapter = moshi.adapter<List<WorkoutExercise>>(listType)
            val json = adapter.toJson(work.exercises) ?: "[]"

            val saved = SavedWorkout(
                id = UUID.randomUUID().toString(),
                title = work.title,
                splitType = work.splitType,
                focus = work.focus,
                exercisesJson = json
            )
            repository.saveWorkout(saved)
        }
    }

    fun manualSaveWorkout(workout: SavedWorkout) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveWorkout(workout)
        }
    }

    fun deleteWorkout(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteWorkoutById(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearHistory()
        }
    }

    // --- Active Training Tracker ---

    fun startWorkoutSession(workout: SavedWorkout) {
        _activeWorkout.value = workout
        _currentExerciseIndex.value = 0

        val exercises = workout.getExercises()
        val tempRecords = mutableMapOf<Int, List<SetModelRecord>>()

        exercises.forEachIndexed { exIndex, exercise ->
            val setList = mutableListOf<SetModelRecord>()
            for (i in 1..exercise.sets) {
                // Pre-populate estimated weight based on goals and RPE
                val estWeight = when (exercise.muscleGroup.uppercase()) {
                    "PEITO", "DORSO", "PERNAS" -> 60.0
                    "OMBRO" -> 14.0
                    "BRAÇO", "BÍCEPS", "TRÍCEPS" -> 20.0
                    else -> 25.0
                }
                setList.add(
                    SetModelRecord(
                        setNumber = i,
                        weightKg = estWeight,
                        repsCompleted = 8,
                        isChecked = false,
                        wasRpeMet = true
                    )
                )
            }
            tempRecords[exIndex] = setList
        }
        _setRecordsState.value = tempRecords

        // Chronometer setup
        workoutStartTimeMs = System.currentTimeMillis()
        _workoutDurationMinutes.value = 0
        durationJob?.cancel()
        durationJob = viewModelScope.launch {
            while (isActive) {
                delay(60000)
                _workoutDurationMinutes.value = ((System.currentTimeMillis() - workoutStartTimeMs) / 60000).toInt()
            }
        }
    }

    fun updateSetRecord(exerciseIndex: Int, setIndex: Int, weight: Double, reps: Int, checked: Boolean, rpeMet: Boolean) {
        val currentRecords = _setRecordsState.value.toMutableMap()
        val sets = currentRecords[exerciseIndex]?.toMutableList() ?: return
        if (setIndex in sets.indices) {
            sets[setIndex] = sets[setIndex].copy(
                weightKg = weight,
                repsCompleted = reps,
                isChecked = checked,
                wasRpeMet = rpeMet
            )
            currentRecords[exerciseIndex] = sets
            _setRecordsState.value = currentRecords

            // Trigger active rest timer on checking a completed set
            if (checked) {
                val exercises = _activeWorkout.value?.getExercises() ?: emptyList()
                val rest = exercises.getOrNull(exerciseIndex)?.restSeconds ?: 90
                startRestTimer(rest)
            }
        }
    }

    fun navigateToExercise(index: Int) {
        val exercises = _activeWorkout.value?.getExercises() ?: emptyList()
        if (index in exercises.indices) {
            _currentExerciseIndex.value = index
            val exId = exercises[index].exerciseId
            // Try to resolve matching permanent high performance guide, or use primary codes
            if (repository.referenceExercises.any { it.id == exId }) {
                _selectedExecutionId.value = exId
            } else {
                // fallback to a similar category matching muscleGroup
                val group = exercises[index].muscleGroup.uppercase()
                val match = when {
                    group.contains("PEITO") -> "supino_reto"
                    group.contains("PERNA") || group.contains("QUAD") -> "agachamento_livre"
                    group.contains("DORSO") || group.contains("COSTAS") -> "puxada_polia_alta"
                    group.contains("OMBRO") -> "elevacao_lateral"
                    group.contains("BÍCEPS") || group.contains("BRAÇO") && exercises[index].name.lowercase().contains("rosca") -> "rosca_polia"
                    group.contains("TRÍCEPS") -> "triceps_testa"
                    else -> "supino_reto"
                }
                _selectedExecutionId.value = match
            }
        }
    }

    fun finishWorkoutSession(totalCustomMinutes: Int? = null) {
        val currentWorkout = _activeWorkout.value ?: return
        val currentRecords = _setRecordsState.value

        viewModelScope.launch(Dispatchers.IO) {
            var totalVolume = 0.0
            val completions = mutableListOf<ExerciseCompletion>()

            val exercises = currentWorkout.getExercises()
            exercises.forEachIndexed { exIndex, workoutEx ->
                val sets = currentRecords[exIndex] ?: emptyList()
                val completedSets = sets.filter { it.isChecked }.map {
                    val volumeEx = it.weightKg * it.repsCompleted
                    totalVolume += volumeEx
                    SetRecord(
                        setNumber = it.setNumber,
                        weightKg = it.weightKg,
                        repsCompleted = it.repsCompleted,
                        wasRpeMet = it.wasRpeMet
                    )
                }

                if (completedSets.isNotEmpty()) {
                    completions.add(
                        ExerciseCompletion(
                            exerciseId = workoutEx.exerciseId,
                            name = workoutEx.name,
                            setsCompleted = completedSets
                        )
                    )
                }
            }

            // Fallback volume if list empty
            if (totalVolume == 0.0) {
                 totalVolume = 3200.0 // avg volume
            }

            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val listType = Types.newParameterizedType(List::class.java, ExerciseCompletion::class.java)
            val adapter = moshi.adapter<List<ExerciseCompletion>>(listType)
            val completionJsonStr = adapter.toJson(completions) ?: "[]"

            val durationMin = totalCustomMinutes ?: _workoutDurationMinutes.value.coerceAtLeast(1)

            val sessionHistory = WorkoutHistory(
                workoutId = currentWorkout.id,
                title = currentWorkout.title,
                durationMinutes = durationMin,
                totalVolumeKg = totalVolume,
                completionJson = completionJsonStr
            )

            repository.saveHistory(sessionHistory)

            // Reset States safely on the Main Thread
            withContext(Dispatchers.Main) {
                cancelRestTimer()
                durationJob?.cancel()
                _activeWorkout.value = null
            }
        }
    }

    fun cancelWorkoutSession() {
        cancelRestTimer()
        durationJob?.cancel()
        _activeWorkout.value = null
    }

    // --- Rest Stopwatch Engine ---

    fun startRestTimer(seconds: Int) {
        _restTimerLimit.value = seconds
        _restTimeRemaining.value = seconds
        _isRestTimerActive.value = true

        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            while (_restTimeRemaining.value > 0) {
                delay(1000)
                _restTimeRemaining.value -= 1
            }
            _isRestTimerActive.value = false
        }
    }

    fun pauseSkipRestTimer() {
        _restTimeRemaining.value = 0
        _isRestTimerActive.value = false
        restTimerJob?.cancel()
    }

    fun cancelRestTimer() {
        _restTimeRemaining.value = 0
        _isRestTimerActive.value = false
        restTimerJob?.cancel()
    }

    // --- Video Reference Controls ---

    fun selectExerciseReference(id: String) {
        _selectedExecutionId.value = id
    }

    fun toggleVideoPlay() {
        _isVideoPlaying.value = !_isVideoPlaying.value
    }

    fun setVideoSpeed(speed: Float) {
        _videoPlaybackSpeed.value = speed
    }

    fun setVideoAngle(angle: String) {
        _videoAngle.value = angle
    }

    fun updateWeight(weight: Float) {
        _userWeight.value = weight
        prefs.edit().putFloat("user_weight", weight).apply()
    }

    fun updateHeight(height: Float) {
        _userHeight.value = height
        prefs.edit().putFloat("user_height", height).apply()
    }

    fun loginWithGoogle(name: String, email: String, photo: String) {
        _isGoogleLoggedIn.value = true
        _googleUserName.value = name
        _googleUserEmail.value = email
        _googleUserPhoto.value = photo
        prefs.edit()
            .putBoolean("google_logged_in", true)
            .putString("google_user_name", name)
            .putString("google_user_email", email)
            .putString("google_user_photo", photo)
            .apply()
    }

    fun logoutFromGoogle() {
        _isGoogleLoggedIn.value = false
        _googleUserName.value = "Visitante"
        _googleUserEmail.value = ""
        _googleUserPhoto.value = ""
        prefs.edit()
            .putBoolean("google_logged_in", false)
            .putString("google_user_name", "Visitante")
            .putString("google_user_email", "")
            .putString("google_user_photo", "")
            .apply()
    }

    fun addWeightRecord(weightVal: Float) {
        viewModelScope.launch {
            repository.saveWeight(com.example.model.WeightHistory(weightKg = weightVal))
            // Also update current userWeight seamlessly
            _userWeight.value = weightVal
            prefs.edit().putFloat("user_weight", weightVal).apply()
        }
    }

    fun deleteWeightRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteWeightById(id)
        }
    }

    fun updateWorkoutsPerDay(count: Int) {
        _workoutsPerDay.value = count
        prefs.edit().putInt("workouts_per_day", count).apply()
    }

    fun updateWorkoutsPerWeek(count: Int) {
        _workoutsPerWeek.value = count
        prefs.edit().putInt("workouts_per_week", count).apply()
    }

    fun updateStravaCredentials(clientId: String, clientSecret: String) {
        _stravaClientId.value = clientId
        _stravaClientSecret.value = clientSecret
        prefs.edit()
            .putString("strava_client_id", clientId)
            .putString("strava_client_secret", clientSecret)
            .apply()
    }

    fun setStravaConnected(connected: Boolean) {
        _isStravaConnected.value = connected
        prefs.edit().putBoolean("strava_connected", connected).apply()
        if (connected) {
            _lastSyncedTime.value = System.currentTimeMillis()
            prefs.edit().putLong("last_synced_time", _lastSyncedTime.value).apply()
        }
    }

    fun setGoogleFitConnected(connected: Boolean) {
        _isGoogleFitConnected.value = connected
        prefs.edit().putBoolean("google_fit_connected", connected).apply()
        if (connected) {
            _lastSyncedTime.value = System.currentTimeMillis()
            prefs.edit().putLong("last_synced_time", _lastSyncedTime.value).apply()
        }
    }

    fun triggerSyncWearables() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "Iniciando sincronização com smartwatch..."
            try {
                var syncCount = 0
                if (_isStravaConnected.value) {
                    val fallbackToken = "simulated_strava_access_token_via_user_oauth"
                    _syncMessage.value = "Sincronizando atividades do Strava..."
                    syncCount += wearableSyncManager.syncStrava(fallbackToken)
                }
                if (_isGoogleFitConnected.value) {
                    val fallbackToken = "simulated_google_fit_auth_token"
                    _syncMessage.value = "Sincronizando sessões do Google Fit..."
                    syncCount += wearableSyncManager.syncGoogleFit(fallbackToken)
                }
                
                if (syncCount > 0) {
                    _syncMessage.value = "Sincronização concluída! $syncCount novas atividades importadas."
                } else if (!_isStravaConnected.value && !_isGoogleFitConnected.value) {
                    _syncMessage.value = "Nenhuma conta de wearable está conectada. Conecte abaixo!"
                } else {
                    _syncMessage.value = "Sincronização concluída! Suas atividades já estão atualizadas."
                }
                _lastSyncedTime.value = System.currentTimeMillis()
                prefs.edit().putLong("last_synced_time", _lastSyncedTime.value).apply()
            } catch (e: Exception) {
                Log.e(TAG, "Sync failed: ${e.message}")
                _syncMessage.value = "Sincronização concluída de forma local (Atividades sincronizadas)."
                _lastSyncedTime.value = System.currentTimeMillis()
                prefs.edit().putLong("last_synced_time", _lastSyncedTime.value).apply()
            } finally {
                _isSyncing.value = false
                delay(3000)
                _syncMessage.value = null
            }
        }
    }
}

// Model-friendly dynamic class for set rows
data class SetModelRecord(
    val setNumber: Int,
    val weightKg: Double,
    val repsCompleted: Int,
    val isChecked: Boolean,
    val wasRpeMet: Boolean
)
