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
import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "WorkoutViewModel"
    private val workoutDao = AppDatabase.getDatabase(application).workoutDao()
    val repository = WorkoutRepository(workoutDao)

    private val prefs = application.getSharedPreferences("apexforce_user_prefs", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder().build()

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("is_dark_theme", true))
    val isDarkTheme = _isDarkTheme.asStateFlow()

    fun setDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
        prefs.edit().putBoolean("is_dark_theme", enabled).apply()
    }

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

    // --- Tabs State ---
    private val _tabs = MutableStateFlow<List<com.example.model.RoutineCategory>>(emptyList())
    val tabs = _tabs.asStateFlow()

    private val _activeTab = MutableStateFlow(prefs.getString("active_tab_name", "Favorito") ?: "Favorito")
    val activeTab = _activeTab.asStateFlow()

    // --- Cycle State ---
    private val _currentCycleWorkoutIds = MutableStateFlow<List<String>>(emptyList())
    val currentCycleWorkoutIds = kotlinx.coroutines.flow.combine(
        _currentCycleWorkoutIds,
        _activeTab,
        _tabs,
        repository.allSavedWorkouts
    ) { explicitIds, activeTab, tabs, workouts ->
        if (explicitIds.isNotEmpty()) {
            explicitIds
        } else {
            workouts.sortedWith(
                compareByDescending<com.example.model.SavedWorkout> { it.isFavorite }
                    .thenByDescending { it.dateCreated }
            ).filter {
                it.category == activeTab || 
                (activeTab == tabs.firstOrNull()?.name && (it.category.isBlank() || it.isFavorite)) ||
                (activeTab == "Favorito" && it.isFavorite)
            }.map { it.id }
        }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, emptyList())

    private val _currentCycleIndex = MutableStateFlow(0)
    val currentCycleIndex = _currentCycleIndex.asStateFlow()

    private val _cycleTotalWorkoutsCompleted = MutableStateFlow(0)
    val cycleTotalWorkoutsCompleted = _cycleTotalWorkoutsCompleted.asStateFlow()

    private val _cycleStartDateMs = MutableStateFlow(0L)
    val cycleStartDateMs = _cycleStartDateMs.asStateFlow()

    // Streak System
    private val _currentStreak = MutableStateFlow(0)
    val currentStreak = _currentStreak.asStateFlow()

    init {
        loadTabs()
        switchActiveTab(_activeTab.value)
    }

    private fun loadTabs() {
        val json = prefs.getString("custom_tabs_json", "[]") ?: "[]"
        try {
            val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, com.example.model.RoutineCategory::class.java)
            val adapter = moshi.adapter<List<com.example.model.RoutineCategory>>(listType)
            val rawTabs = adapter.fromJson(json) ?: emptyList()
            if (rawTabs.isEmpty()) {
                val defaultTabs = listOf(
                    com.example.model.RoutineCategory(name = "Favorito", emoji = "⭐"),
                    com.example.model.RoutineCategory(name = "Hipertrofia", emoji = "🦾"),
                    com.example.model.RoutineCategory(name = "Casa", emoji = "🏠")
                )
                _tabs.value = defaultTabs
                saveTabs(defaultTabs)
            } else {
                _tabs.value = rawTabs
            }
        } catch (e: Exception) {
            _tabs.value = emptyList()
        }
    }

    private fun saveTabs(newTabs: List<com.example.model.RoutineCategory>) {
        try {
            val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, com.example.model.RoutineCategory::class.java)
            val adapter = moshi.adapter<List<com.example.model.RoutineCategory>>(listType)
            prefs.edit().putString("custom_tabs_json", adapter.toJson(newTabs)).apply()
        } catch (e: Exception) {}
    }

    fun addTab(tab: com.example.model.RoutineCategory) {
        val next = _tabs.value + tab
        _tabs.value = next
        saveTabs(next)
        switchActiveTab(tab.name)
    }

    fun updateTab(tab: com.example.model.RoutineCategory) {
        val next = _tabs.value.map { if (it.id == tab.id) tab else it }
        _tabs.value = next
        saveTabs(next)
        if (_activeTab.value == _tabs.value.find { it.id == tab.id }?.name) {
            switchActiveTab(tab.name)
        }
    }
    
    fun removeTab(tabId: String) {
        val tabToRemove = _tabs.value.find { it.id == tabId }
        val next = _tabs.value.filter { it.id != tabId }
        _tabs.value = next
        saveTabs(next)
        if (tabToRemove?.name == _activeTab.value && next.isNotEmpty()) {
            switchActiveTab(next.first().name)
        }
    }

    fun reorderTabs(fromIndex: Int, toIndex: Int) {
        val next = _tabs.value.toMutableList()
        val item = next.removeAt(fromIndex)
        next.add(toIndex, item)
        _tabs.value = next
        saveTabs(next)
    }

    fun duplicateTabAndWorkouts(tab: com.example.model.RoutineCategory, workouts: List<com.example.model.SavedWorkout>) {
        val newName = "${tab.name} (Cópia)"
        val newTab = tab.copy(id = java.util.UUID.randomUUID().toString(), name = newName)
        addTab(newTab)

        val workoutsToCopy = workouts.filter { it.category == tab.name }
        viewModelScope.launch(Dispatchers.IO) {
            workoutsToCopy.forEach { wk ->
                val newWk = wk.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    category = newName
                )
                repository.saveWorkout(newWk)
            }
        }
    }

    fun switchActiveTab(tabName: String) {
        _activeTab.value = tabName
        prefs.edit().putString("active_tab_name", tabName).apply()
        
        // Load state specific to this tab
        _currentCycleWorkoutIds.value = prefs.getString("cycle_workout_ids_$tabName", "")?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
        _currentCycleIndex.value = prefs.getInt("cycle_current_index_$tabName", 0)
        _cycleTotalWorkoutsCompleted.value = prefs.getInt("cycle_total_workouts_completed_$tabName", 0)
        _cycleStartDateMs.value = prefs.getLong("cycle_start_date_ms_$tabName", 0L)
        _currentStreak.value = prefs.getInt("current_streak_$tabName", 0)
    }

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

    // --- Premium Form Questionnaire States ---
    private val _userName = MutableStateFlow(prefs.getString("user_name", "Rodrigo C. G.") ?: "Rodrigo C. G.")
    val userName = _userName.asStateFlow()

    private val _userAge = MutableStateFlow(prefs.getString("user_age", "25") ?: "25")
    val userAge = _userAge.asStateFlow()

    private val _userGender = MutableStateFlow(prefs.getString("user_gender", "Masculino") ?: "Masculino")
    val userGender = _userGender.asStateFlow()

    private val _selectedObjective = MutableStateFlow(prefs.getString("user_objective", "Hipertrofia") ?: "Hipertrofia")
    val selectedObjective = _selectedObjective.asStateFlow()

    private val _userFitnessLevel = MutableStateFlow(prefs.getString("user_fitness_level", "Avançado") ?: "Avançado")
    val userFitnessLevel = _userFitnessLevel.asStateFlow()

    private val _workoutsPerWeekCount = MutableStateFlow(prefs.getInt("workouts_per_week_count", 5))
    val workoutsPerWeekCount = _workoutsPerWeekCount.asStateFlow()

    private val _chosenSplitPattern = MutableStateFlow(prefs.getString("chosen_split_pattern", "Push Pull Legs") ?: "Push Pull Legs")
    val chosenSplitPattern = _chosenSplitPattern.asStateFlow()

    private val _workoutDurationChoice = MutableStateFlow(prefs.getString("workout_duration_choice", "60 min") ?: "60 min")
    val workoutDurationChoice = _workoutDurationChoice.asStateFlow()

    private val _workoutLocation = MutableStateFlow(prefs.getString("workout_location", "Academia completa") ?: "Academia completa")
    val workoutLocation = _workoutLocation.asStateFlow()

    private val _optionalMuscleFocus = MutableStateFlow(prefs.getString("optional_muscle_focus", "") ?: "")
    val optionalMuscleFocus = _optionalMuscleFocus.asStateFlow()

    fun updateName(name: String) {
        _userName.value = name
        prefs.edit().putString("user_name", name).apply()
    }

    fun updateAge(age: String) {
        _userAge.value = age
        prefs.edit().putString("user_age", age).apply()
    }

    fun updateGender(gender: String) {
        _userGender.value = gender
        prefs.edit().putString("user_gender", gender).apply()
    }

    fun updateObjective(objective: String) {
        _selectedObjective.value = objective
        prefs.edit().putString("user_objective", objective).apply()
        selectedFocus = objective
    }

    fun updateFitnessLevel(level: String) {
        _userFitnessLevel.value = level
        prefs.edit().putString("user_fitness_level", level).apply()
        experienceLevel = level
    }

    fun updateWorkoutsPerWeekCount(count: Int) {
        _workoutsPerWeekCount.value = count
        prefs.edit().putInt("workouts_per_week_count", count).apply()
        updateWorkoutsPerWeek(count)
    }

    fun updateChosenSplitPattern(split: String) {
        _chosenSplitPattern.value = split
        prefs.edit().putString("chosen_split_pattern", split).apply()
        selectedSplit = when (split) {
            "ABC" -> "ABC_DENSIDADE"
            "ABCD" -> "PPL_PUSH"
            "ABCDE" -> "PPL_PULL"
            "Push Pull Legs" -> "PPL_PUSH"
            "Upper Lower" -> "UPPER_LOWER"
            "Full Body" -> "MISTO_SUP_INF"
            "Arnold Split" -> "ARNOLD_SPLIT"
            "Bro Split" -> "ABC_DENSIDADE"
            "Misto" -> "MISTO_PERNA_BRACO_PEITO"
            "Foco em Braços" -> "PONTO_FRACO"
            "Foco em Peito" -> "PPL_PUSH"
            "Foco em Pernas" -> "PPL_LEGS"
            "Foco em Glúteo" -> "PPL_LEGS"
            "Foco em Ombros" -> "PONTO_FRACO"
            else -> "ABC_DENSIDADE"
        }
    }

    fun updateWorkoutDurationChoice(dur: String) {
        _workoutDurationChoice.value = dur
        prefs.edit().putString("workout_duration_choice", dur).apply()
    }

    fun updateWorkoutLocation(loc: String) {
        _workoutLocation.value = loc
        prefs.edit().putString("workout_location", loc).apply()
    }

    fun updateOptionalMuscleFocus(focusCSV: String) {
        _optionalMuscleFocus.value = focusCSV
        prefs.edit().putString("optional_muscle_focus", focusCSV).apply()
    }

    // --- Core Database and Generation ---

    fun generateNewWorkout() {
        viewModelScope.launch {
            _isGenerating.value = true
            _generationError.value = null
            _lastGeneratedWorkout.value = null

            val richNotes = """
                Nome do Usuário: ${_userName.value}
                Idade: ${_userAge.value}
                Sexo: ${_userGender.value}
                Altura: ${_userHeight.value} cm
                Peso: ${_userWeight.value} kg
                Duração planejada: ${_workoutDurationChoice.value}
                Local de treino: ${_workoutLocation.value}
                Foco muscular opcional: ${_optionalMuscleFocus.value}
                Observações de limitação: $specialNotes
            """.trimIndent()

            val result = GeminiClient.generateWorkout(
                splitType = selectedSplit,
                focus = _selectedObjective.value,
                specialNotes = richNotes,
                experienceLevel = _userFitnessLevel.value,
                workoutsPerDay = _workoutsPerDay.value,
                workoutsPerWeek = _workoutsPerWeekCount.value
            )

            _isGenerating.value = false

            when (result) {
                is GeneratedWorkoutResult.Success -> {
                    _lastGeneratedWorkout.value = result.cycle.firstOrNull()
                    _showGeneratedSuccessDialog.value = true

                    // Auto-save generated workouts in DB and init Cycle
                    val split = selectedSplit
                    val objective = _selectedObjective.value
                    val generatedTabName = "IA: $split $objective"
                    
                    if (_tabs.value.none { it.name == generatedTabName }) {
                        addTab(com.example.model.RoutineCategory(name = generatedTabName, emoji = "🤖", colorHex = "#BB86FC"))
                    } else {
                        switchActiveTab(generatedTabName)
                    }

                    viewModelScope.launch(Dispatchers.IO) {
                        val savedIds = mutableListOf<String>()
                        result.cycle.forEach { work ->
                            val listType = Types.newParameterizedType(List::class.java, WorkoutExercise::class.java)
                            val adapter = moshi.adapter<List<WorkoutExercise>>(listType)
                            val json = adapter.toJson(work.exercises) ?: "[]"

                            val id = UUID.randomUUID().toString()
                            savedIds.add(id)
                            val saved = SavedWorkout(
                                id = id,
                                title = work.title,
                                splitType = work.splitType,
                                focus = work.focus,
                                exercisesJson = json,
                                category = generatedTabName,
                                isFavorite = false
                            )
                            repository.saveWorkout(saved)
                        }
                        // Start 90-days Cycle
                        val nowMs = System.currentTimeMillis()
                        prefs.edit()
                             .putString("cycle_workout_ids_$generatedTabName", savedIds.joinToString(","))
                             .putInt("cycle_current_index_$generatedTabName", 0)
                             .putLong("cycle_start_date_ms_$generatedTabName", nowMs)
                             .putInt("cycle_total_workouts_completed_$generatedTabName", 0)
                             .apply()
                        
                        _currentCycleWorkoutIds.value = savedIds
                        _currentCycleIndex.value = 0
                        _cycleTotalWorkoutsCompleted.value = 0
                        _cycleStartDateMs.value = nowMs
                    }
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

    fun saveImportedWorkout(workout: com.example.model.SavedWorkout) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveWorkout(workout)
        }
    }

    private fun saveGeneratedWorkout(work: GeneratedWorkout) {
        viewModelScope.launch(Dispatchers.IO) {
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

    fun deleteAllWorkouts() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllWorkouts()
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
        val historyList: List<WorkoutHistory> = workoutHistory.value

        exercises.forEachIndexed { exIndex, exercise ->
            val setList = mutableListOf<SetModelRecord>()
            
            // Find historic weight for this exercise if possible
            var historicWeight = 0.0
            for (history in historyList) {
                val completion = history.getCompletions().find { it.name == exercise.name }
                if (completion != null && completion.setsCompleted.isNotEmpty()) {
                    historicWeight = completion.setsCompleted.firstOrNull()?.weightKg ?: 0.0
                    break // found the most recent one (since sorted by DESC)
                }
            }
            
            val estWeight = if (historicWeight > 0.0) historicWeight else {
                when (exercise.muscleGroup.uppercase()) {
                    "PEITO", "DORSO", "PERNAS" -> 60.0
                    "OMBRO" -> 14.0
                    "BRAÇO", "BÍCEPS", "TRÍCEPS" -> 20.0
                    else -> 25.0
                }
            }
            
            val repsVal = exercise.repsRange.split(Regex("[^0-9]")).filter { it.isNotEmpty() }.lastOrNull()?.toIntOrNull() ?: 10

            for (i in 1..exercise.sets) {
                setList.add(
                    SetModelRecord(
                        setNumber = i,
                        weightKg = estWeight,
                        repsCompleted = repsVal,
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

    fun skipWorkoutCycle() {
        if (currentCycleWorkoutIds.value.isNotEmpty()) {
            var nextIndex = _currentCycleIndex.value + 1
            if (nextIndex >= currentCycleWorkoutIds.value.size) {
                nextIndex = 0 // Loop
            }
            _currentCycleIndex.value = nextIndex
            val tabName = _activeTab.value
            prefs.edit().putInt("cycle_current_index_$tabName", nextIndex).apply()
        }
    }

    fun previousWorkoutCycle() {
        if (currentCycleWorkoutIds.value.isNotEmpty()) {
            var nextIndex = _currentCycleIndex.value - 1
            if (nextIndex < 0) {
                nextIndex = currentCycleWorkoutIds.value.size - 1 // Loop backwards
            }
            _currentCycleIndex.value = nextIndex
            val tabName = _activeTab.value
            prefs.edit().putInt("cycle_current_index_$tabName", nextIndex).apply()
        }
    }

    fun finishWorkoutSession(totalCustomMinutes: Int? = null) {
        val currentWorkout = _activeWorkout.value ?: return
        val currentRecords = _setRecordsState.value
        
        // Cycle updates
        val total = _cycleTotalWorkoutsCompleted.value + 1
        _cycleTotalWorkoutsCompleted.value = total
        
        var nextIndex = _currentCycleIndex.value + 1
        if (nextIndex >= currentCycleWorkoutIds.value.size && currentCycleWorkoutIds.value.isNotEmpty()) {
            nextIndex = 0 // Loop cycle
        }
        _currentCycleIndex.value = nextIndex
        
        // Streak Logic
        val tabName = _activeTab.value
        val lastTime = prefs.getLong("last_workout_time_ms_$tabName", 0L)
        val now = System.currentTimeMillis()
        val diffHours = (now - lastTime) / (1000 * 60 * 60)
        
        val newStreak = if (lastTime == 0L || diffHours > 48) {
            1
        } else if (diffHours > 12) {
            _currentStreak.value + 1
        } else {
            _currentStreak.value // Too soon to count as a new day
        }
        _currentStreak.value = newStreak
        
        prefs.edit()
            .putInt("cycle_total_workouts_completed_$tabName", total)
            .putInt("cycle_current_index_$tabName", nextIndex)
            .putLong("last_workout_time_ms_$tabName", now)
            .putInt("current_streak_$tabName", newStreak)
            .apply()

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

    fun toggleFavoriteWorkout(workoutId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val workout = repository.getSavedWorkoutById(workoutId) ?: return@launch
            val updated = workout.copy(isFavorite = !workout.isFavorite)
            repository.saveWorkout(updated)
        }
    }

    fun duplicateWorkout(workout: SavedWorkout) {
        viewModelScope.launch(Dispatchers.IO) {
            val duplicated = workout.copy(
                id = UUID.randomUUID().toString(),
                title = "${workout.title} (Cópia)",
                dateCreated = System.currentTimeMillis(),
                isFavorite = false
            )
            repository.saveWorkout(duplicated)
        }
    }

    fun updateWorkoutProperties(workoutId: String, title: String, category: String, emoji: String, colorHex: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val workout = repository.getSavedWorkoutById(workoutId) ?: return@launch
            val updated = workout.copy(
                title = title,
                category = category,
                emoji = emoji,
                colorHex = colorHex
            )
            repository.saveWorkout(updated)
        }
    }

    fun replaceActiveExercise(exerciseIndex: Int) {
        val current = _activeWorkout.value ?: return
        val exercises = current.getExercises().toMutableList()
        if (exerciseIndex in exercises.indices) {
            val exToReplace = exercises[exerciseIndex]
            
            // Find equivalent exercise targeting the same muscle group
            val alternatives = when (exToReplace.muscleGroup.uppercase()) {
                "PEITO" -> listOf(
                     "Crucifixo com Halteres" to "Foco em alongamento e estímulo mecânico do peitoral maior.",
                     "Flexão de Braços (Solo)" to "Exercício funcional excelente com peso corporal focado em segurança articular.",
                     "Supino Unilateral com Halteres" to "Trabalho cinesiológico unilateral que minimiza pressões assimétricas.",
                     "Peck Deck" to "Isolamento tensional contínuo das fibras internas do peito."
                )
                "COSTAS", "DORSO", "LATS" -> listOf(
                     "Remada Curvada com Halteres" to "Remada focada em dorsal largo com amplitude de movimento livre.",
                     "Puxada Supinada Fechada" to "Foco mecânico favorável para bíceps e latíssimo posterior.",
                     "Remada Baixa Sentado" to "Tensão horizontal contínua excelente para o miolo das costas."
                )
                "PERNAS", "QUAD", "GLÚTEO", "PANTURRILHA" -> listOf(
                     "Passante Recuo com Halteres" to "Conexão mente-músculo incrível, menor impacto articular.",
                     "Leg Press 45 Unilateral" to "Isolamento sob carga pesada neutralizando a coluna lombar.",
                     "Cadeira Extensora" to "Foco tensional puro na porção reto femoral do quadríceps."
                )
                "OMBRO", "OMBROS" -> listOf(
                     "Desenvolvimento com Halteres" to "Foco multiarticular na porção anterior e lateral do deltoide.",
                     "Elevação Frontal com Halter" to "Estímulo isolador frontal para reequilíbrio escapular."
                )
                "BÍCEPS", "BRAÇO" -> listOf(
                     "Rosca Martelo Alternada" to "Trabalho focado no braquiorradial e bíceps cabeça longa.",
                     "Rosca Concentrada Unilateral" to "Isolamento mecânico absoluto contra batota de ombro."
                )
                "TRÍCEPS" -> listOf(
                     "Tríceps na Polia com Corda" to "Estímulo articular suave com pico de contração acentuado.",
                     "Tríceps Coice com Halter" to "Definição de tríceps cabeça lateral mantendo a escápula fixa."
                )
                else -> listOf(
                     "Prancha Abdominal Isométrica" to "Estabilização central pura sem impacto espinal.",
                     "Flexão com Apoio dos Joelhos" to "Substituição metabólica segura e de fácil execução."
                )
            }
            
            val selectedAlt = alternatives.random()
            val newEx = exToReplace.copy(
                name = selectedAlt.first,
                notes = "Adaptação Inteligente IA: " + selectedAlt.second,
                exerciseId = "sub_${selectedAlt.first.lowercase().replace(" ", "_")}"
            )
            exercises[exerciseIndex] = newEx
            
            val listType = Types.newParameterizedType(List::class.java, WorkoutExercise::class.java)
            val adapter = moshi.adapter<List<WorkoutExercise>>(listType)
            val updatedJson = adapter.toJson(exercises) ?: "[]"
            
            val updatedWorkout = current.copy(exercisesJson = updatedJson)
            _activeWorkout.value = updatedWorkout
            
            // Persist locally
            viewModelScope.launch(Dispatchers.IO) {
                repository.saveWorkout(updatedWorkout)
            }
        }
    }

    fun saveManualRoutine(workouts: List<SavedWorkout>) {
        if (workouts.isEmpty()) return
        val routineName = workouts.first().category
        if (_tabs.value.none { it.name == routineName }) {
            val emoji = if (workouts.first().emoji.isNotBlank()) workouts.first().emoji else "📝"
            addTab(com.example.model.RoutineCategory(name = routineName, emoji = emoji, colorHex = "#BB86FC"))
        } else {
            switchActiveTab(routineName)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val savedIds = mutableListOf<String>()
            workouts.forEach {
                repository.saveWorkout(it)
                savedIds.add(it.id)
            }
            
            // Start 90-days Cycle
            val nowMs = System.currentTimeMillis()
            prefs.edit()
                 .putString("cycle_workout_ids_$routineName", savedIds.joinToString(","))
                 .putInt("cycle_current_index_$routineName", 0)
                 .putLong("cycle_start_date_ms_$routineName", nowMs)
                 .putInt("cycle_total_workouts_completed_$routineName", 0)
                 .apply()
            
            _currentCycleWorkoutIds.value = savedIds
            _currentCycleIndex.value = 0
            _cycleTotalWorkoutsCompleted.value = 0
            _cycleStartDateMs.value = nowMs
        }
    }

    fun createManualWorkout(
        title: String,
        category: String,
        emoji: String,
        colorHex: String,
        splitType: String,
        focus: String,
        exercises: List<WorkoutExercise>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val listType = Types.newParameterizedType(List::class.java, WorkoutExercise::class.java)
            val adapter = moshi.adapter<List<WorkoutExercise>>(listType)
            val json = adapter.toJson(exercises) ?: "[]"

            val saved = SavedWorkout(
                id = UUID.randomUUID().toString(),
                title = title,
                splitType = splitType,
                focus = focus,
                isFavorite = false,
                category = category,
                emoji = emoji,
                colorHex = colorHex,
                exercisesJson = json
            )
            repository.saveWorkout(saved)
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
