package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import com.example.model.SavedWorkout
import com.example.model.WorkoutExercise
import com.example.ui.components.BiomechanicalPlayer
import com.example.ui.theme.*
import com.example.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymAppRoot(viewModel: WorkoutViewModel) {
    // Current Active Tab
    var currentTab by remember { mutableStateOf("GENERATOR") } // GENERATOR, WORKOUTS, EXPLORE, HISTORY
    
    // States from ViewModel
    val activeWorkout by viewModel.activeWorkout.collectAsState()
    val isRestTimerActive by viewModel.isRestTimerActive.collectAsState()
    val activeRestTimeRemaining by viewModel.restTimeRemaining.collectAsState()
    val activeRestTimerLimit by viewModel.restTimerLimit.collectAsState()
    val activeYouTubeUrl by viewModel.activeYouTubeUrl.collectAsState()
    val activeCrashLog by viewModel.activeCrashLog.collectAsState()

    val googleLoggedIn by viewModel.isGoogleLoggedIn.collectAsState()
    var hasBypassedEntrance by remember { mutableStateOf(false) }

    if (!googleLoggedIn && !hasBypassedEntrance) {
        LoginEntranceScreen(
            viewModel = viewModel,
            onBypass = { hasBypassedEntrance = true }
        )
    } else {
        Scaffold(
            bottomBar = {
                if (activeWorkout == null) {
                    NavigationBar(
                        containerColor = CarbonSurface,
                        tonalElevation = 8.dp,
                        windowInsets = WindowInsets.navigationBars,
                        modifier = Modifier.testTag("app_navigation_bar")
                    ) {
                        val tabs = listOf<Triple<String, String, ImageVector>>(
                            Triple("GENERATOR", "Gerar AI", Icons.Default.AutoAwesome),
                            Triple("WORKOUTS", "Meus Treinos", Icons.AutoMirrored.Outlined.ListAlt),
                            Triple("EXPLORE", "Comunidade", Icons.Default.Public),
                            Triple("HISTORY", "Histórico", Icons.Default.History),
                            Triple("PROFILE", "Perfil", Icons.Default.Person)
                        )
                        
                        tabs.forEach { (tabId, label, icon) ->
                            val selected = currentTab == tabId
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentTab = tabId },
                                icon = { Icon(icon, contentDescription = label, tint = if (selected) ToxicGreen else Color.LightGray) },
                                label = { Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ToxicGreen,
                                    indicatorColor = ToxicGreen.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("nav_tab_$tabId")
                            )
                        }
                    }
                }
            },
            containerColor = CarbonBg
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Main views routing
                if (activeWorkout != null) {
                    // Active fullscreen Workout tracking overlay
                    ActiveWorkoutSessionHud(viewModel = viewModel)
                } else {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            fadeIn(tween(220)) togetherWith fadeOut(tween(180))
                        },
                        label = "tab_routing"
                    ) { tab ->
                        when (tab) {
                            "GENERATOR" -> WorkoutGeneratorScreen(viewModel = viewModel, onNavigateToWorkouts = { currentTab = "WORKOUTS" })
                            "WORKOUTS" -> SavedWorkoutsListScreen(viewModel = viewModel, onNavigateToGenerator = { currentTab = "GENERATOR" })
                            "EXPLORE" -> CommunityExploreScreen(viewModel = viewModel)
                            "HISTORY" -> WorkoutHistoryLogsScreen(viewModel = viewModel)
                            "PROFILE" -> UserProfileScreen(viewModel = viewModel)
                        }
                    }
                }

                // Quick Floating mini view for current active REST TIMER
                if (isRestTimerActive && activeWorkout != null) {
                    RestTimerMiniOverlay(
                        remaining = activeRestTimeRemaining,
                        limit = activeRestTimerLimit,
                        onSkip = { viewModel.pauseSkipRestTimer() }
                    )
                }

                activeYouTubeUrl?.let { url ->
                    InAppYouTubePlayerDialog(
                        url = url,
                        viewModel = viewModel,
                        onClose = { viewModel.closeYouTubeVideo() }
                    )
                }

                activeCrashLog?.let { crashLog ->
                    val context = LocalContext.current
                    AlertDialog(
                        onDismissRequest = { viewModel.clearCrashLog() },
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.BugReport, contentDescription = null, tint = ElectricOrange)
                                Text("Oops! Erro Detectado", fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "O aplicativo detectou que ocorreu uma falha na sessão anterior. O log de erro abaixo foi salvo:",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 240.dp)
                                        .background(Color.Black, RoundedCornerShape(8.dp))
                                        .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        text = crashLog,
                                        fontFamily = TechMonospace,
                                        fontSize = 11.sp,
                                        color = ElectricOrange
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    try {
                                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Crash Log", crashLog)
                                        clipboard.setPrimaryClip(clip)
                                        android.widget.Toast.makeText(context, "Log de erro copiado!", android.widget.Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        android.util.Log.e("GymApp", "Failed to copy crash log", e)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen, contentColor = Color.Black)
                            ) {
                                Text("COPIAR ERRO")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.clearCrashLog() }) {
                                Text("IGNORAR & LIMPAR", color = Color.Gray)
                            }
                        }
                    )
                }
            }
        }
    }
}

// 1. --- WORKOUT AI GENERATOR SCREEN ---

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WorkoutGeneratorScreen(
    viewModel: WorkoutViewModel,
    onNavigateToWorkouts: () -> Unit
) {
    val isGenerating by viewModel.isGenerating.collectAsState()
    val lastGenerated by viewModel.lastGeneratedWorkout.collectAsState()
    val generationError by viewModel.generationError.collectAsState()
    val showSuccessDialog by viewModel.showGeneratedSuccessDialog.collectAsState()
    
    val focusManager = LocalFocusManager.current

    // Collect variables from VM
    val userName by viewModel.userName.collectAsState()
    val userAge by viewModel.userAge.collectAsState()
    val userGender by viewModel.userGender.collectAsState()
    val selectedObjective by viewModel.selectedObjective.collectAsState()
    val userFitnessLevel by viewModel.userFitnessLevel.collectAsState()
    val workoutsPerWeekCount by viewModel.workoutsPerWeekCount.collectAsState()
    val chosenSplitPattern by viewModel.chosenSplitPattern.collectAsState()
    val workoutDurationChoice by viewModel.workoutDurationChoice.collectAsState()
    val workoutLocation by viewModel.workoutLocation.collectAsState()
    val optionalMuscleFocus by viewModel.optionalMuscleFocus.collectAsState()
    val height by viewModel.userHeight.collectAsState()
    val weight by viewModel.userWeight.collectAsState()

    var notesInput by remember { mutableStateOf("") }
    var heightInputLocal by remember { mutableStateOf(height.toInt().toString()) }
    var weightInputLocal by remember { mutableStateOf(String.format(Locale.US, "%.1f", weight)) }

    if (showSuccessDialog && lastGenerated != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSuccessDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Celebration, contentDescription = null, tint = ToxicGreen)
                    Text("Treino Criado!", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    "O treino '${lastGenerated?.title}' foi compilado de forma cinesiológica de acordo com suas escolhas premium.",
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissSuccessDialog()
                        onNavigateToWorkouts()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen, contentColor = Color.Black)
                ) {
                    Text("Ver Meus Treinos")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissSuccessDialog() }) {
                    Text("Fechar", color = Color.Gray)
                }
            },
            containerColor = CarbonSurface
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("workout_generator_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "DESFRANGANDO A.I.",
                    style = MaterialTheme.typography.labelLarge,
                    color = ToxicGreen,
                    fontFamily = TechMonospace
                )
                Text(
                    text = "Formulário Premium IA",
                    style = MaterialTheme.typography.displayLarge,
                    color = TextPrimary
                )
                Text(
                    text = "Responda as etapas para prescrever sua nova rotina de alta performance assistida por inteligência artificial.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
        }

        // ETAPA 1: DADOS FÍSICOS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = ToxicGreen, modifier = Modifier.size(18.dp))
                        Text("Etapa 1: Antropometria & Identificação", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    // Nome
                    Text("Nome do Atleta", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { viewModel.updateName(it) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = ToxicGreen,
                            unfocusedBorderColor = BorderDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Idade
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Idade", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = userAge,
                                onValueChange = { viewModel.updateAge(it) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = ToxicGreen,
                                    unfocusedBorderColor = BorderDark
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Sexo
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text("Gênero", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("M", "F").forEach { g ->
                                    val isSelected = (g == "M" && userGender == "Masculino") || (g == "F" && userGender == "Feminino")
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(if (isSelected) ToxicGreen.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(8.dp))
                                            .border(1.dp, if (isSelected) ToxicGreen else BorderDark, RoundedCornerShape(8.dp))
                                            .clickable { viewModel.updateGender(if (g == "M") "Masculino" else "Feminino") }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(if (g == "M") "Masc" else "Fem", color = if (isSelected) ToxicGreen else TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Altura
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Altura (cm)", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = heightInputLocal,
                                onValueChange = {
                                    if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                                        heightInputLocal = it
                                        val h = it.toFloatOrNull()
                                        if (h != null && h > 0) {
                                            viewModel.updateHeight(h)
                                        }
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = ToxicGreen,
                                    unfocusedBorderColor = BorderDark
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Peso
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Peso (kg)", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = weightInputLocal,
                                onValueChange = {
                                    val sanitized = it.replace(',', '.')
                                    if (sanitized.isEmpty() || sanitized.toDoubleOrNull() != null || sanitized == "." || sanitized.endsWith(".")) {
                                        weightInputLocal = it
                                        val w = sanitized.toFloatOrNull()
                                        if (w != null && w > 0f) {
                                            viewModel.updateWeight(w)
                                        }
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = ToxicGreen,
                                    unfocusedBorderColor = BorderDark
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // ETAPA 2: OBJETIVO
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = ToxicGreen, modifier = Modifier.size(18.dp))
                        Text("Etapa 2: Objetivo de Alta Performance", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    val rawObjectives = listOf(
                        "Hipertrofia" to "Hipertrofia",
                        "Emagrecimento" to "Emagrecimento",
                        "Definição" to "Definição",
                        "Força" to "Força",
                        "Condicionamento" to "Condicionamento",
                        "Recomposição corporal" to "Recomposição"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        rawObjectives.chunked(2).forEach { rowList ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowList.forEach { (objKey, objLabel) ->
                                    val isSelected = selectedObjective == objKey
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(if (isSelected) ToxicGreen.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(8.dp))
                                            .border(1.dp, if (isSelected) ToxicGreen else BorderDark, RoundedCornerShape(8.dp))
                                            .clickable { viewModel.updateObjective(objKey) }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            objLabel, 
                                            color = if (isSelected) ToxicGreen else TextPrimary, 
                                            fontSize = 12.sp, 
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ETAPA 3: NÍVEL
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Leaderboard, contentDescription = null, tint = ToxicGreen, modifier = Modifier.size(18.dp))
                        Text("Etapa 3: Nível de Treinabilidade", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Iniciante", "Intermediário", "Avançado").forEach { level ->
                            val isSelected = userFitnessLevel == level
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSelected) ToxicGreen.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(8.dp))
                                    .border(1.dp, if (isSelected) ToxicGreen else BorderDark, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.updateFitnessLevel(level) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    level, 
                                    color = if (isSelected) ToxicGreen else TextPrimary, 
                                    fontSize = 12.sp, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // ETAPA 4: DIAS VAI TREINAR
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = ToxicGreen, modifier = Modifier.size(18.dp))
                        Text("Etapa 4: Quantidade de dias para treinar", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        (1..7).forEach { day ->
                            val isSelected = workoutsPerWeekCount == day
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSelected) ToxicGreen.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(8.dp))
                                    .border(1.dp, if (isSelected) ToxicGreen else BorderDark, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.updateWorkoutsPerWeekCount(day) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$day d", 
                                    color = if (isSelected) ToxicGreen else TextPrimary, 
                                    fontSize = 12.sp, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // ETAPA 5: DIVISÃO DE TREINO
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CompareArrows, contentDescription = null, tint = ToxicGreen, modifier = Modifier.size(18.dp))
                        Text("Etapa 5: Escolha a Divisão de Treino", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    val splitsList = listOf(
                        "ABC", "ABCD", "ABCDE", "Push Pull Legs", "Upper Lower", 
                        "Full Body", "Arnold Split", "Bro Split", "Misto", 
                        "Foco em Braços", "Foco em Peito", "Foco em Pernas", 
                        "Foco em Glúteo", "Foco em Ombros"
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = 3
                    ) {
                        splitsList.forEach { splitItem ->
                            val isSelected = chosenSplitPattern == splitItem
                            Box(
                                modifier = Modifier
                                    .background(if (isSelected) ToxicGreen.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(8.dp))
                                    .border(1.dp, if (isSelected) ToxicGreen else BorderDark, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.updateChosenSplitPattern(splitItem) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    splitItem, 
                                    color = if (isSelected) ToxicGreen else TextPrimary, 
                                    fontSize = 11.sp, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // ETAPA 6: TEMPO DE TREINO
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = ToxicGreen, modifier = Modifier.size(18.dp))
                        Text("Etapa 6: Tempo de Duração do Treino", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("30 min", "45 min", "60 min", "90 min").forEach { duration ->
                            val isSelected = workoutDurationChoice == duration
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSelected) ToxicGreen.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(8.dp))
                                    .border(1.dp, if (isSelected) ToxicGreen else BorderDark, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.updateWorkoutDurationChoice(duration) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    duration, 
                                    color = if (isSelected) ToxicGreen else TextPrimary, 
                                    fontSize = 12.sp, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // ETAPA 7: LOCAL DE TREINO
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.HomeWork, contentDescription = null, tint = ToxicGreen, modifier = Modifier.size(18.dp))
                        Text("Etapa 7: Local de Treino", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Academia completa", "Academia básica", "Casa", "Peso corporal").chunked(2).forEach { rowList ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowList.forEach { loc ->
                                    val isSelected = workoutLocation == loc
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(if (isSelected) ToxicGreen.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(8.dp))
                                            .border(1.dp, if (isSelected) ToxicGreen else BorderDark, RoundedCornerShape(8.dp))
                                            .clickable { viewModel.updateWorkoutLocation(loc) }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            loc, 
                                            color = if (isSelected) ToxicGreen else TextPrimary, 
                                            fontSize = 12.sp, 
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ETAPA 8: FOCO MUSCULAR OPCIONAL
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Adjust, contentDescription = null, tint = ToxicGreen, modifier = Modifier.size(18.dp))
                        Text("Etapa 8: Foco Muscular Opcional", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    val musclesList = listOf("Peito", "Costas", "Ombro", "Bíceps", "Tríceps", "Pernas", "Glúteo", "Abdômen", "Panturrilha")
                    val activeSet = optionalMuscleFocus.split(",").filter { it.isNotEmpty() }.toSet()

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = 3
                    ) {
                        musclesList.forEach { muscle ->
                            val isSelected = activeSet.contains(muscle)
                            Box(
                                modifier = Modifier
                                    .background(if (isSelected) ToxicGreen.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(8.dp))
                                    .border(1.dp, if (isSelected) ToxicGreen else BorderDark, RoundedCornerShape(8.dp))
                                    .clickable {
                                        val nextSet = if (isSelected) activeSet - muscle else activeSet + muscle
                                        viewModel.updateOptionalMuscleFocus(nextSet.joinToString(","))
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    muscle, 
                                    color = if (isSelected) ToxicGreen else TextPrimary, 
                                    fontSize = 12.sp, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // REQUISITOS ESPECIAIS / LIMITAÇÕES
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CompassCalibration, contentDescription = null, tint = ElectricOrange, modifier = Modifier.size(18.dp))
                        Text("Requisitos Especiais & Limitações", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Adicione observações de dor crônica, lesões ou listas de halteres limitados para que o IA reorganize as pressões articulares de forma cinesiológica e segura.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = {
                            notesInput = it
                            viewModel.specialNotes = it
                        },
                        placeholder = { Text("Ex: Dor no ombro esquerdo, evitar puxadas extremas. Apenas polias.", color = Color.Gray, fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("special_notes_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricOrange,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                }
            }
        }

        // CTA GENERATION BUTTON
        item {
            if (isGenerating) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF191307)),
                    border = BorderStroke(1.dp, ElectricOrange),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(color = ElectricOrange)
                        Text(
                            "ANALISANDO VETORES DE INTENSIDADE...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = TechMonospace,
                            color = ElectricOrange
                        )
                        Text(
                            "O Treinador Pro está integrando curvas de tensão, esquemas de contração neuromuscular baseados na sua solicitação especial.",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = TextMuted
                        )
                    }
                }
            } else {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.generateNewWorkout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("generate_workout_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ToxicGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text("COMPILAI TREINO CIENTÍFICO", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        // DISPLAY GENERATION ERROR
        generationError?.let { err ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x33FF0000)),
                    border = BorderStroke(1.dp, Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Erro: $err",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}


// 2. --- MY SAVED WORKOUTS SCREEN ---

@Composable
fun SavedWorkoutsListScreen(
    viewModel: WorkoutViewModel,
    onNavigateToGenerator: () -> Unit
) {
    val savedWorkouts by viewModel.savedWorkouts.collectAsState()
    
    var showMultiDayCreator by remember { mutableStateOf(false) }
    var showManualCreator by remember { mutableStateOf(false) }
    var workoutToEditByManualCreator by remember { mutableStateOf<SavedWorkout?>(null) }
    var workoutToEditProperties by remember { mutableStateOf<SavedWorkout?>(null) }
    var workoutToShare by remember { mutableStateOf<SavedWorkout?>(null) }
    var routineToShare by remember { mutableStateOf<com.example.model.RoutineCategory?>(null) }
    var showDeleteAllConfirmation by remember { mutableStateOf(false) }
    var showNewTabDialog by remember { mutableStateOf(false) }
    var tabToEdit by remember { mutableStateOf<com.example.model.RoutineCategory?>(null) }

    val sortedWorkouts = remember(savedWorkouts) {
        savedWorkouts.sortedWith(
            compareByDescending<SavedWorkout> { it.isFavorite }
                .thenByDescending { it.dateCreated }
        )
    }

    if (showMultiDayCreator) {
        val currentTabForNewRoutine = viewModel.activeTab.collectAsState().value
        MultiDayRoutineCreatorScreen(
            availableExercises = viewModel.repository.referenceExercises,
            activeTabName = currentTabForNewRoutine,
            onDismiss = { showMultiDayCreator = false },
            onSaveRoutine = { workouts ->
                viewModel.saveManualRoutine(workouts)
                showMultiDayCreator = false
            }
        )
    } else if (showManualCreator) {
        ManualWorkoutCreatorScreen(
            initialWorkout = workoutToEditByManualCreator,
            availableExercises = viewModel.repository.referenceExercises,
            onDismiss = { showManualCreator = false },
            onSave = { title, category, emoji, colorHex, splitType, focus, exercises ->
                val moshi = com.squareup.moshi.Moshi.Builder().build()
                val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, WorkoutExercise::class.java)
                val adapter = moshi.adapter<List<WorkoutExercise>>(listType)
                val exercisesJson = adapter.toJson(exercises) ?: "[]"

                if (workoutToEditByManualCreator == null) {
                    viewModel.createManualWorkout(title, category, emoji, colorHex, splitType, focus, exercises)
                } else {
                    val updated = workoutToEditByManualCreator!!.copy(
                        title = title,
                        category = category,
                        emoji = emoji,
                        colorHex = colorHex,
                        splitType = splitType,
                        focus = focus,
                        exercisesJson = exercisesJson
                    )
                    viewModel.manualSaveWorkout(updated)
                }
                showManualCreator = false
            }
        )
    } else {
        val tabs by viewModel.tabs.collectAsState()
        val activeTab by viewModel.activeTab.collectAsState()

        val filteredWorkouts = remember(sortedWorkouts, activeTab, tabs) {
            sortedWorkouts.filter {
                it.category == activeTab || 
                (activeTab == tabs.firstOrNull()?.name && (it.category.isBlank() || it.isFavorite)) ||
                (activeTab == "Favorito" && it.isFavorite)
            }
        }

        val cycleIds by viewModel.currentCycleWorkoutIds.collectAsState()
        val cycleIndex by viewModel.currentCycleIndex.collectAsState()
        val totalCompleted by viewModel.cycleTotalWorkoutsCompleted.collectAsState()
        val currentStreak by viewModel.currentStreak.collectAsState()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("saved_workouts_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PRESCRIÇÕES",
                            style = MaterialTheme.typography.labelLarge,
                            color = TechCyan,
                            fontFamily = TechMonospace
                        )
                        Text(
                            text = "Meus Treinos",
                            style = MaterialTheme.typography.displayLarge,
                            color = TextPrimary
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (sortedWorkouts.isNotEmpty()) {
                            IconButton(
                                onClick = { showDeleteAllConfirmation = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Apagar todos", tint = Color.Red, modifier = Modifier.size(20.dp))
                            }
                        }
                        Button(
                            onClick = {
                                workoutToEditByManualCreator = null
                                showMultiDayCreator = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("CRIAR TREINO", fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = TechMonospace)
                            }
                        }
                    }
                }
            }
            
            item {
                @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(tabs, key = { it.id }) { tab ->
                        val isSelected = activeTab == tab.name
                        val parsedColor = try { Color(android.graphics.Color.parseColor(tab.colorHex)) } catch(e: Exception) { ToxicGreen }
                        Box(
                            modifier = Modifier
                                .background(if (isSelected) parsedColor else CarbonSurface, RoundedCornerShape(16.dp))
                                .border(1.dp, if (isSelected) parsedColor else BorderDark, RoundedCornerShape(16.dp))
                                .combinedClickable(
                                    onClick = { viewModel.switchActiveTab(tab.name) },
                                    onLongClick = { tabToEdit = tab }
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("${tab.emoji} ${tab.name}", color = if (isSelected) Color.Black else TextMuted, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    item {
                        OutlinedButton(
                            onClick = { showNewTabDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, BorderDark),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text("+ Nova Aba", fontSize = 14.sp)
                        }
                    }
                }
            }

            if (cycleIds.isNotEmpty() && filteredWorkouts.isNotEmpty()) {
                item {
                    val totalDays = 90
                    val percent = if (totalDays > 0) (totalCompleted.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f) else 0f
                    val daysLeft = (totalDays - totalCompleted).coerceAtLeast(0)
                    
                    if (daysLeft == 0) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                            border = BorderStroke(1.dp, ToxicGreen),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Seu ciclo foi concluído!", color = ToxicGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Você avançou expressivamente durante os $totalDays dias, alcançando um novo patamar de condicionamento.", color = TextMuted, textAlign = TextAlign.Center, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { onNavigateToGenerator() },
                                    colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen, contentColor = Color.Black),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("GERAR NOVO CICLO", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                            border = BorderStroke(1.dp, ToxicGreen),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("🔥 STREAK: $currentStreak Dias", color = ToxicGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("$daysLeft DIAS RESTANTES", color = TechCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Progress bar
                                LinearProgressIndicator(
                                    progress = { percent },
                                    modifier = Modifier.fillMaxWidth().height(6.dp),
                                    color = ToxicGreen,
                                    trackColor = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Progresso 90 Dias", color = TextMuted, fontSize = 10.sp)
                                    Text("${(percent * 100).toInt()}%", color = TextMuted, fontSize = 10.sp)
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                                
                                Text("TREINO DE HOJE", color = TextMuted, fontSize = 10.sp, fontFamily = TechMonospace)
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                val currentWorkoutId = cycleIds.getOrNull(cycleIndex)
                                val currentWorkout = filteredWorkouts.find { it.id == currentWorkoutId }
                                if (currentWorkout != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(currentWorkout.emoji, fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(currentWorkout.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Text("${currentWorkout.splitType} • ${currentWorkout.focus}", color = TextMuted, fontSize = 12.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { viewModel.startWorkoutSession(currentWorkout) },
                                        colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen, contentColor = Color.Black),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("INICIAR TREINO", fontWeight = FontWeight.Bold)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        OutlinedButton(
                                            onClick = { viewModel.previousWorkoutCycle() },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                            border = BorderStroke(1.dp, Color.DarkGray),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("VOLTAR", fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        OutlinedButton(
                                            onClick = { viewModel.skipWorkoutCycle() },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                            border = BorderStroke(1.dp, Color.DarkGray),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("PULAR", fontSize = 12.sp)
                                        }
                                    }
                                } else {
                                    Text("Treino não encontrado.", color = TextMuted)
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("PRÓXIMOS TREINOS DA ROTINA", color = TextMuted, fontSize = 10.sp, fontFamily = TechMonospace)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Highlight sequence 
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    cycleIds.forEachIndexed { i, id ->
                                        val wk = filteredWorkouts.find { it.id == id }
                                        val isCurrent = (i == cycleIndex)
                                        val isPassed = (i < cycleIndex)
                                        val colorActive = if(isCurrent) ToxicGreen else if (isPassed) Color.DarkGray else Color.DarkGray
                                        val textColor = if(isCurrent) Color.Black else Color.White
                                        
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 4.dp)
                                                .background(colorActive, RoundedCornerShape(6.dp))
                                                .border(1.dp, if(isCurrent) ToxicGreen else Color.DarkGray, RoundedCornerShape(6.dp))
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(wk?.title?.take(1) ?: "?", color = textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (filteredWorkouts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.DarkGray)
                            Text(
                                "Nenhuma prescrição salva ainda.",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                "Gere seu primeiro programa biomecânico com IA ou clique em 'CRIAR TREINO' para prescrever manualmente.",
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onNavigateToGenerator,
                                colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Ir para Gerador de Treinos", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filteredWorkouts, key = { it.id }) { workout ->
                    SavedWorkoutCard(
                        workout = workout,
                        onStart = { viewModel.startWorkoutSession(workout) },
                        onDelete = { viewModel.deleteWorkout(workout.id) },
                        onPlayVideo = { url, name -> viewModel.playYouTubeVideoForExercise(name, url) },
                        onFavorite = { viewModel.toggleFavoriteWorkout(workout.id) },
                        onDuplicate = { viewModel.duplicateWorkout(workout) },
                        onEdit = {
                            workoutToEditByManualCreator = workout
                            showManualCreator = true
                        },
                        onEditCustomProperties = { workoutToEditProperties = workout },
                        onShare = { workoutToShare = workout }
                    )
                }
            }
        }
    }

    workoutToEditProperties?.let { workout ->
        EditWorkoutDialog(
            workout = workout,
            onDismiss = { workoutToEditProperties = null },
            onSave = { name, category, emoji, colorHex ->
                viewModel.updateWorkoutProperties(workout.id, name, category, emoji, colorHex)
                workoutToEditProperties = null
            }
        )
    }

    workoutToShare?.let { workout ->
        ShareWorkoutDialog(
            workout = workout,
            onDismiss = { workoutToShare = null }
        )
    }

    routineToShare?.let { tab ->
        val wkCount = savedWorkouts.count { it.category == tab.name }
        ShareRoutineDialog(
            tab = tab,
            workoutCount = wkCount,
            savedWorkouts = savedWorkouts.filter { it.category == tab.name },
            onDismiss = { routineToShare = null }
        )
    }

    if (showDeleteAllConfirmation) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteAllConfirmation = false },
            title = { Text("Apagar Todos", color = Color.White) },
            text = { Text("Tem certeza de que deseja apagar todos os treinos? Esta ação não pode ser desfeita.", color = Color.LightGray) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllWorkouts()
                    showDeleteAllConfirmation = false
                }) {
                    Text("APAGAR", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllConfirmation = false }) {
                    Text("CANCELAR", color = Color.LightGray)
                }
            },
            containerColor = CarbonCard
        )
    }

    if (showNewTabDialog) {
        var newName by remember { mutableStateOf("") }
        var newEmoji by remember { mutableStateOf("📁") }
        var newColorHex by remember { mutableStateOf("#8B5CF6") }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showNewTabDialog = false },
            title = { Text("Nova Aba", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nome da Aba") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newEmoji,
                            onValueChange = { newEmoji = it },
                            label = { Text("Emoji") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        OutlinedTextField(
                            value = newColorHex,
                            onValueChange = { newColorHex = it },
                            label = { Text("Cor (Hex)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank()) {
                        viewModel.addTab(com.example.model.RoutineCategory(name = newName, emoji = newEmoji, colorHex = newColorHex))
                    }
                    showNewTabDialog = false
                }) {
                    Text("CRIAR ABA", color = ToxicGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewTabDialog = false }) {
                    Text("CANCELAR", color = Color.LightGray)
                }
            },
            containerColor = CarbonCard
        )
    }

    tabToEdit?.let { tab ->
        var editName by remember { mutableStateOf(tab.name) }
        var editEmoji by remember { mutableStateOf(tab.emoji) }
        var editColorHex by remember { mutableStateOf(tab.colorHex) }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { tabToEdit = null },
            title = { Text("Editar Aba", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nome da Aba") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editEmoji,
                            onValueChange = { editEmoji = it },
                            label = { Text("Emoji") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        OutlinedTextField(
                            value = editColorHex,
                            onValueChange = { editColorHex = it },
                            label = { Text("Cor (Hex)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                    val tabsList by viewModel.tabs.collectAsState()
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Posição", color = Color.White, fontSize = 14.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { 
                                    val idx = tabsList.indexOfFirst { it.id == tab.id }
                                    if (idx > 0) viewModel.reorderTabs(idx, idx - 1)
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(36.dp),
                                border = BorderStroke(1.dp, Color.DarkGray)
                            ) { Text("<", color = Color.White, fontSize = 16.sp) }
                            
                            OutlinedButton(
                                onClick = { 
                                    val idx = tabsList.indexOfFirst { it.id == tab.id }
                                    if (idx != -1 && idx < tabsList.size - 1) viewModel.reorderTabs(idx, idx + 1)
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(36.dp),
                                border = BorderStroke(1.dp, Color.DarkGray)
                            ) { Text(">", color = Color.White, fontSize = 16.sp) }
                        }
                    }
                    Button(
                        onClick = {
                            routineToShare = tab
                            tabToEdit = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Text("COMPARTILHAR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                viewModel.duplicateTabAndWorkouts(tab, savedWorkouts)
                                tabToEdit = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CarbonSurface, contentColor = TechCyan),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("DUPLICAR", fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                viewModel.removeTab(tab.id)
                                tabToEdit = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("APAGAR", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editName.isNotBlank()) {
                        viewModel.updateTab(tab.copy(name = editName, emoji = editEmoji, colorHex = editColorHex))
                    }
                    tabToEdit = null
                }) {
                    Text("SALVAR", color = ToxicGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { tabToEdit = null }) {
                    Text("CANCELAR", color = Color.LightGray)
                }
            },
            containerColor = CarbonCard
        )
    }
}

@Composable
fun SavedWorkoutCard(
    workout: SavedWorkout,
    onStart: () -> Unit,
    onDelete: () -> Unit,
    onPlayVideo: (String, String) -> Unit,
    onFavorite: () -> Unit,
    onDuplicate: () -> Unit,
    onEdit: () -> Unit,
    onEditCustomProperties: () -> Unit,
    onShare: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val exercises = workout.getExercises()
    val themeCardColor = parseHexColor(workout.colorHex)
    
    // Dynamic division calculations
    var totalSets = 0
    var musclesWorked = ""
    var muscleVolumeSummary = ""
    var estMin = 0
    var intensityLabel = "Moderada (RPE 7+)"
    
    try {
        musclesWorked = exercises.map { (it.muscleGroup as Any?).toString() }.distinct().filter { it.isNotBlank() && it != "null" }.take(4).joinToString(", ")
        totalSets = exercises.sumOf { ((it.sets as Any?) as? Int) ?: 0 }
        
        muscleVolumeSummary = exercises.groupBy { (it.muscleGroup as Any?).toString() }
            .filterKeys { it.isNotBlank() && it != "null" }
            .map { (muscle, list) -> "$muscle: ${list.sumOf { ((it.sets as Any?) as? Int) ?: 0 }}s" }
            .joinToString(" | ")
            
        val totalSecondsRest = exercises.sumOf { (((it.sets as Any?) as? Int) ?: 0) * (((it.restSeconds as Any?) as? Int) ?: 0) }
        if (exercises.isNotEmpty()) {
            val estimatedMinutes = (exercises.sumOf { (((it.sets as Any?) as? Int) ?: 0) * 1.5 } + (totalSecondsRest / 60.0) + 5).toInt()
            estMin = estimatedMinutes.coerceIn(20, 120)
        }
        
        val validRpes = exercises.mapNotNull { ((it.intensityRPE as Any?) as? Int) ?: 8 }
        val avgRpe = if (validRpes.isNotEmpty()) validRpes.average() else 8.0
        intensityLabel = when {
            avgRpe >= 9.0 -> "Intensa (RPE 9+)"
            avgRpe >= 8.0 -> "Alta (RPE 8+)"
            else -> "Moderada (RPE 7+)"
        }
    } catch (e: Exception) {
        // Safe fallback in case of deserialization nullability mismatch
    }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
        border = BorderStroke(1.2.dp, themeCardColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("workout_card_${workout.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(workout.emoji, fontSize = 20.sp)
                        Text(
                            workout.title,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onFavorite,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (workout.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favoritar",
                                tint = if (workout.isFavorite) Color(0xFFFFD700) else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (workout.category.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .background(themeCardColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .border(0.5.dp, themeCardColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                workout.category.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeCardColor,
                                fontFamily = TechMonospace
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.AltRoute, contentDescription = null, modifier = Modifier.size(11.dp), tint = TechCyan)
                            Text(workout.splitType, fontSize = 11.sp, fontFamily = TechMonospace, color = TechCyan)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.MonitorWeight, contentDescription = null, modifier = Modifier.size(11.dp), tint = ElectricOrange)
                            Text(workout.focus, fontSize = 11.sp, fontFamily = TechMonospace, color = ElectricOrange)
                        }
                    }
                }
            }

            // Elegant Performance Info summary card of the Split
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.25f)),
                border = BorderStroke(0.5.dp, BorderDark),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "Músculos",
                            tint = ToxicGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "MÚSCULOS:",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = TechMonospace
                        )
                        Text(
                            text = musclesWorked.ifEmpty { "Corpo Inteiro" },
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Divider(color = BorderDark.copy(alpha = 0.5f), thickness = 0.5.dp)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("EXERCÍCIOS", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                            Text("${exercises.size} un", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Column {
                            Text("VOLUME TOTAL", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                            Text("$totalSets séries", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Column {
                            Text("TEMPO ESTIMADO", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                            Text("$estMin min", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Column {
                            Text("INTENSIDADE", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                            Text(intensityLabel, color = ElectricOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    if (muscleVolumeSummary.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Volume por Grupo: $muscleVolumeSummary",
                            color = TechCyan,
                            fontSize = 9.sp,
                            fontFamily = TechMonospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.LightGray)
                    Text("${exercises.size} Exercícios de Alto Rendimento", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }

                // Quick compact actions toolbar
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar exercícios", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onEditCustomProperties, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Palette, contentDescription = "Personalizar cores e nomes", tint = themeCardColor, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDuplicate, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicar treino", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Compartilhar", tint = TechCyan, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Excluir", tint = Color.Red, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Expanded view of exercises inside this workout
            if (expanded) {
                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = BorderDark)
                Spacer(modifier = Modifier.height(10.dp))
                
                exercises.forEachIndexed { index, ex ->
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${index + 1}. ${ex.name}",
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                )
                                val phaseColor = when (ex.trainingPhase) {
                                    "Aquecimento" -> TechCyan
                                    "Acessório" -> ElectricOrange
                                    "Alongamento" -> Color(0xFF81C784)
                                    else -> ToxicGreen
                                }
                                Box(
                                    modifier = Modifier
                                        .background(phaseColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .border(0.5.dp, phaseColor.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        ex.trainingPhase.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = phaseColor
                                    )
                                }
                            }
                            Text(
                                "${ex.sets}x ${ex.repsRange}",
                                fontWeight = FontWeight.Bold,
                                color = TechCyan,
                                fontSize = 13.sp,
                                fontFamily = TechMonospace
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(start = 14.dp, top = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Tempo: ${ex.tempo}", fontSize = 11.sp, color = TextMuted)
                            Text("Pausa: ${ex.restSeconds}s", fontSize = 11.sp, color = TextMuted)
                            if (ex.advancedTechnique != "Nenhuma") {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF231604), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(ex.advancedTechnique, fontSize = 10.sp, color = ElectricOrange, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Row(
                                modifier = Modifier
                                    .clickable { onPlayVideo(getYouTubeUrl(ex.name), ex.name) }
                                    .background(Color(0xFF2B0B0B), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, Color(0xFFFF5252), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = "Vídeo no YouTube",
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    "YOUTUBE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFEAEA),
                                    fontFamily = TechMonospace
                                )
                            }
                        }
                        Text(
                            text = ex.notes,
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            modifier = Modifier.padding(start = 14.dp, top = 4.dp)
                        )
                    }
                    if (index < exercises.lastIndex) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Divider(color = Color(0xFF1E1E26), thickness = 0.5.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onStart,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeCardColor, contentColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_start_workout_${workout.id}")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("INICIAR SESSÃO DE TREINO", fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }
        }
    }
}


// 3. --- PRO VIDEO EXECUTION ENCYCLOPEDIA SCREEN ---

@Composable
fun VideoExecutionEncyclopediaScreen(viewModel: WorkoutViewModel) {
    val refId by viewModel.selectedExecutionId.collectAsState()
    val isPlaying by viewModel.isVideoPlaying.collectAsState()
    val playbackSpeed by viewModel.videoPlaybackSpeed.collectAsState()
    val angleMode by viewModel.videoAngle.collectAsState()
    val uriHandler = LocalUriHandler.current

    val currentExRef = viewModel.repository.referenceExercises.find { it.id == refId } ?: viewModel.repository.referenceExercises.first()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("videos_encyclopedia_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "BIOMECÂNICA",
                    style = MaterialTheme.typography.labelLarge,
                    color = ToxicGreen,
                    fontFamily = TechMonospace
                )
                Text(
                    text = "Execuções em Vídeo",
                    style = MaterialTheme.typography.displayLarge,
                    color = TextPrimary
                )
                Text(
                    text = "Simulações cinesiológicas 3D mostrando estiramento de fibras sob tensão e caminhos articulares ótimos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
        }

        // BIOMECHANICAL MOTION GRAPH VIDEO PLAYER CANVAS
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BiomechanicalPlayer(
                    jointPathType = currentExRef.jointPathType,
                    isPlaying = isPlaying,
                    playbackSpeed = playbackSpeed,
                    viewMode = angleMode,
                    modifier = Modifier.fillMaxWidth()
                )

                // Video Player Controls Row
                Card(
                    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                    border = BorderStroke(1.dp, BorderDark)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Play / Pause Toggle Button
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.toggleVideoPlay() },
                                    modifier = Modifier.testTag("btn_toggle_play")
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Executar / Pausar",
                                        tint = ToxicGreen
                                    )
                                }
                                Text(if (isPlaying) "REPRODUZINDO" else "PAUSADO", fontSize = 11.sp, fontFamily = TechMonospace, color = TextPrimary)
                            }

                            // Angles Selection
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                val angles = listOf("ANATOMY" to "Mapa", "LATERAL" to "Lateral", "FRONTCUT" to "Front")
                                angles.forEach { (mode, label) ->
                                    val active = angleMode == mode
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (active) ToxicGreen else Color(0xFF1E1E24),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .clickable { viewModel.setVideoAngle(mode) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(label, color = if (active) Color.Black else Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Play Speed options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Velocidade Biomecânica", fontSize = 12.sp, color = TextMuted)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val speeds = listOf(0.5f, 1.0f, 1.5f)
                                speeds.forEach { spd ->
                                    val active = playbackSpeed == spd
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (active) TechCyan else Color(0xFF1E1E24),
                                                CircleShape
                                            )
                                            .clickable { viewModel.setVideoSpeed(spd) }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text("${spd}x", color = if (active) Color.Black else Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = BorderDark, thickness = 0.5.dp)

                        Button(
                            onClick = {
                                viewModel.playYouTubeVideoForExercise(currentExRef.name, getYouTubeUrl(currentExRef.name))
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF0000), // YouTube Red
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("btn_youtube_encyclopedia")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = "YouTube",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Text(
                                    "ASSISTIR EXECUÇÃO REAL (YOUTUBE)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = TechMonospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Horizontal Exercises Selection Tabs
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Selecione o Exercício:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                var searchFilter by remember { mutableStateOf("") }
                androidx.compose.material3.OutlinedTextField(
                    value = searchFilter,
                    onValueChange = { searchFilter = it },
                    label = { Text("Buscar...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                val filteredExs = remember(searchFilter, viewModel.repository.referenceExercises) {
                    if (searchFilter.isBlank()) {
                        viewModel.repository.referenceExercises.take(30)
                    } else {
                        viewModel.repository.referenceExercises.filter { it.name.contains(searchFilter, true) }.take(30)
                    }
                }
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredExs, key = { it.id }) { ex ->
                        val selected = ex.id == refId
                        Box(
                            modifier = Modifier
                                .background(
                                    if (selected) ToxicGreen else CarbonSurface,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(1.dp, if (selected) ToxicGreen else BorderDark, RoundedCornerShape(8.dp))
                                .clickable { viewModel.selectExerciseReference(ex.id) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("ref_ex_tab_${ex.id}")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    ex.name,
                                    color = if (selected) Color.Black else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    ex.primaryMuscleName,
                                    color = if (selected) Color.DarkGray else TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // EXERCISE TECHNIQUE EXECUTION DETAIL DESCRIPTIONS CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Adjust, contentDescription = null, tint = ToxicGreen, modifier = Modifier.size(18.dp))
                        Text("Execução Passo a Passo", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    }

                    // Steps
                    currentExRef.executionDetails.forEachIndexed { index, step ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(vertical = 2.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(Color(0xFF22280A), CircleShape)
                                    .border(0.5.dp, ToxicGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${index + 1}", color = ToxicGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(step, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // PRO HARDCORE TECHNIQUE TIP CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF221307)),
                border = BorderStroke(1.dp, ElectricOrange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Whatshot, contentDescription = null, tint = ElectricOrange, modifier = Modifier.size(18.dp))
                        Text("Dicas de Sobrecarga Miofibrilar (Treinador Pro)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ElectricOrange)
                    }

                    currentExRef.highPerformanceTips.forEach { tip ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("▶", color = ElectricOrange, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                            Text(tip, color = TextPrimary, fontSize = 13.sp)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F0701), RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TEMPO DE COMPRESSÃO RECOMENDADO:", fontSize = 11.sp, color = TextMuted)
                        Text(currentExRef.biomechanicalTempo, fontSize = 13.sp, fontFamily = TechMonospace, fontWeight = FontWeight.Bold, color = ElectricOrange)
                    }
                }
            }
        }
    }
}


// 4. --- WORKOUT HISTORY SCREEN ---

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun WorkoutHistoryLogsScreen(viewModel: WorkoutViewModel) {
    val history by viewModel.workoutHistory.collectAsState()
    val savedWorkouts by viewModel.savedWorkouts.collectAsState()
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val moshiAdapter = remember {
        val moshi = com.squareup.moshi.Moshi.Builder().build()
        val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, com.example.model.ExerciseCompletion::class.java)
        moshi.adapter<List<com.example.model.ExerciseCompletion>>(listType)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("workout_history_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ESTATÍSTICAS ELITE",
                    style = MaterialTheme.typography.labelLarge,
                    color = ElectricOrange,
                    fontFamily = TechMonospace
                )
                Text(
                    text = "Meu Histórico",
                    style = MaterialTheme.typography.displayLarge,
                    color = TextPrimary
                )
            }
            if (history.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.clearHistory() },
                    modifier = Modifier.testTag("btn_clear_history")
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Limpar tudo", tint = Color.Red)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.HistoryToggleOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.DarkGray)
                    Text("Nenhuma sessão registrada.", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text("Seus treinos completados e volume total em kgs levantados de sobrecarga aparecerão aqui.", color = TextMuted, textAlign = TextAlign.Center, fontSize = 13.sp)
                }
            }
        } else {
            // General Stats Hub bar
            val totalWorkouts = history.size
            val totalMinutes = history.sumOf { it.durationMinutes }
            val totalVolume = history.sumOf { it.totalVolumeKg }

            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("SESSÕES", fontSize = 10.sp, fontFamily = TechMonospace, color = TextMuted)
                        Text("$totalWorkouts", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ToxicGreen)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("MINUTOS", fontSize = 10.sp, fontFamily = TechMonospace, color = TextMuted)
                        Text("$totalMinutes min", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TechCyan)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("TONELAGEM", fontSize = 10.sp, fontFamily = TechMonospace, color = TextMuted)
                        Text(String.format(Locale.US, "%.0f kg", totalVolume), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ElectricOrange)
                    }
                }
            }

            // History rows
            val displayHistory = remember(history) { history.take(30) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(displayHistory, key = { it.id }) { log ->
                    val relatedWorkout = savedWorkouts.find { it.id == log.workoutId }
                    val musclePercents = remember(log, relatedWorkout) {
                        val map = mutableMapOf<String, Int>()
                        try {
                            if (relatedWorkout != null) {
                                val exercises = relatedWorkout.getExercises()
                                val completions = try { moshiAdapter.fromJson(log.completionJson) ?: emptyList() } catch(e: Exception) { emptyList() }
                                
                                val muscleGroups = exercises.map { (it.muscleGroup as Any?).toString() }.distinct().filter { it.isNotBlank() && it != "null" }
                                for (group in muscleGroups) {
                                    val groupExs = exercises.filter { (it.muscleGroup as Any?).toString() == group }
                                    val plannedSets = groupExs.sumOf { ((it.sets as Any?) as? Int) ?: 0 }
                                    
                                    val completedSets = completions.filter { comp ->
                                        groupExs.any { it.exerciseId == comp.exerciseId }
                                    }.sumOf { (it.setsCompleted as Any? as? List<*>)?.size ?: 0 }
                                    
                                    val percent = if (plannedSets > 0) (completedSets.toFloat() / plannedSets.toFloat() * 100).toInt() else 0
                                    map[group] = percent.coerceIn(0, 100)
                                }
                            }
                        } catch (e: Exception) {
                            // Safe fallback in case of mismatched schemas
                        }
                        map
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                        border = BorderStroke(1.dp, BorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    log.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = ToxicGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    sdf.format(Date(log.dateCompleted)),
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    fontFamily = TechMonospace
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Vol: ${String.format(Locale.US, "%.0f kg", log.totalVolumeKg)}", fontSize = 12.sp, color = ElectricOrange, fontFamily = TechMonospace, fontWeight = FontWeight.Bold)
                                Text("Duração: ${log.durationMinutes} min", fontSize = 12.sp, color = TextPrimary)
                            }
                            
                            if (musclePercents.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                                ) {
                                    musclePercents.forEach { (muscle, pct) ->
                                        val cTint = if (pct >= 80) ToxicGreen else if (pct >= 50) ElectricOrange else Color.Gray
                                        Box(
                                            modifier = Modifier
                                                .padding(bottom = 6.dp)
                                                .background(cTint.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                .border(1.dp, cTint.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("$muscle: $pct%", color = cTint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// 5. --- IMMERSIVE ACTIVE WORKOUT SCREEN HUD ---

@Composable
fun ActiveWorkoutSessionHud(viewModel: WorkoutViewModel) {
    val activeWorkout by viewModel.activeWorkout.collectAsState()
    val currentIndex by viewModel.currentExerciseIndex.collectAsState()
    val setRecordsState by viewModel.setRecordsState.collectAsState()
    val workoutDurationState by viewModel.workoutDurationMinutes.collectAsState()
    val uriHandler = LocalUriHandler.current

    val currentWorkout = activeWorkout ?: return
    val exercises = currentWorkout.getExercises()
    val currentEx = exercises.getOrNull(currentIndex) ?: return

    val currentExSets = setRecordsState[currentIndex] ?: emptyList()

    // Video reference controls properties
    val isPlaying by viewModel.isVideoPlaying.collectAsState()
    val playbackSpeed by viewModel.videoPlaybackSpeed.collectAsState()
    val angleMode by viewModel.videoAngle.collectAsState()

    var showHelpVideo by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonBg)
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
            .testTag("active_workout_hud_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Workout Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "TREINO ATIVO HUD",
                    fontFamily = TechMonospace,
                    fontSize = 11.sp,
                    color = ElectricOrange,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    currentWorkout.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp), tint = TechCyan)
                Text(
                    String.format(Locale.US, "%02d min", workoutDurationState),
                    fontFamily = TechMonospace,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TechCyan
                )
            }
        }

        Divider(color = BorderDark)

        // Exercise navigation bar progress card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (currentIndex > 0) viewModel.navigateToExercise(currentIndex - 1) },
                enabled = currentIndex > 0,
                modifier = Modifier.testTag("btn_prev_ex")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Anterior",
                    tint = if (currentIndex > 0) ToxicGreen else Color.DarkGray
                )
            }

            Text(
                text = "EXERCÍCIO ${currentIndex + 1} DE ${exercises.size}",
                fontSize = 13.sp,
                fontFamily = TechMonospace,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            IconButton(
                onClick = { if (currentIndex < exercises.lastIndex) viewModel.navigateToExercise(currentIndex + 1) },
                enabled = currentIndex < exercises.lastIndex,
                modifier = Modifier.testTag("btn_next_ex")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Próximo",
                    tint = if (currentIndex < exercises.lastIndex) ToxicGreen else Color.DarkGray
                )
            }
        }

        // Active Exercise Main Board Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonSurface),
            border = BorderStroke(1.5.dp, ToxicGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val phaseColor = when (currentEx.trainingPhase) {
                                "Aquecimento" -> TechCyan
                                "Acessório" -> ElectricOrange
                                "Alongamento" -> Color(0xFF81C784)
                                else -> ToxicGreen
                            }
                            Box(
                                modifier = Modifier
                                    .background(phaseColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, phaseColor.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    currentEx.trainingPhase.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = phaseColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            currentEx.name,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = ToxicGreen
                        )
                        Text(
                            "${currentEx.muscleGroup} (${currentEx.targetMuscleDetail})",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .clickable {
                                    viewModel.replaceActiveExercise(currentIndex)
                                }
                                .background(Color(0xFF2A1010), RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapCalls,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                "NÃO CONSIGO FAZER (Substituir por IA)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFEAEA),
                                fontFamily = TechMonospace
                            )
                        }
                    }
                    
                    // Help Execution Video Overlay Button
                    Button(
                        onClick = {
                            viewModel.navigateToExercise(currentIndex) // align references
                            showHelpVideo = !showHelpVideo
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (showHelpVideo) ElectricOrange else Color(0xFF1F2F01)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_help_video")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (showHelpVideo) Color.Black else ToxicGreen)
                            Text("Vídeo Guia", fontSize = 11.sp, color = if (showHelpVideo) Color.Black else TextPrimary)
                        }
                    }
                }

                // Inline Expand Video player cinesiológico
                AnimatedVisibility(visible = showHelpVideo) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                        BiomechanicalPlayer(
                            jointPathType = getBiomechanicalPathType(currentEx.exerciseId),
                            isPlaying = isPlaying,
                            playbackSpeed = playbackSpeed,
                            viewMode = angleMode,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.toggleVideoPlay() }) {
                                    Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, tint = ToxicGreen, modifier = Modifier.size(16.dp))
                                }
                                Text("VELOCIDADE:", fontSize = 10.sp, color = TextMuted, fontFamily = TechMonospace)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${playbackSpeed}x", fontSize = 11.sp, color = ToxicGreen, fontFamily = TechMonospace, fontWeight = FontWeight.Bold)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                val modes = listOf("ANATOMY" to "Mapa", "LATERAL" to "Lateral")
                                modes.forEach { (m, lbl) ->
                                    Box(
                                        modifier = Modifier
                                            .background(if (angleMode == m) ToxicGreen else Color(0xFF222228), RoundedCornerShape(4.dp))
                                            .clickable { viewModel.setVideoAngle(m) }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(lbl, fontSize = 9.sp, color = if (angleMode == m) Color.Black else Color.LightGray)
                                    }
                                }
                            }
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1303)),
                            border = BorderStroke(0.5.dp, ElectricOrange)
                        ) {
                            Text(
                                "Dica Avançada: ${currentEx.notes}",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Button(
                            onClick = { viewModel.playYouTubeVideoForExercise(currentEx.name, getYouTubeUrl(currentEx.name)) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000), contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("btn_youtube_active_hud")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Text("VER EXECUÇÃO REAL (YOUTUBE)", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                            }
                        }
                    }
                }

                Divider(color = BorderDark)

                // Technical tags row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TechBadge(label = "TEMPO: ${currentEx.tempo}", color = TechCyan)
                        TechBadge(label = "RPE: ${currentEx.intensityRPE}", color = ElectricOrange)
                    }
                    if (currentEx.advancedTechnique != "Nenhuma") {
                        TechBadge(label = currentEx.advancedTechnique, color = ToxicGreen)
                    }
                }
            }
        }

        // Sets checklists headers
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonSurface),
            border = BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Table title titles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SÉRIE", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextMuted, fontFamily = TechMonospace, modifier = Modifier.weight(1f))
                    Text("CARGA (KG)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextMuted, fontFamily = TechMonospace, modifier = Modifier.weight(2f), textAlign = TextAlign.Center)
                    Text("REPS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextMuted, fontFamily = TechMonospace, modifier = Modifier.weight(2f), textAlign = TextAlign.Center)
                    Text("STATUS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextMuted, fontFamily = TechMonospace, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                }

                Divider(color = BorderDark)

                // Render dynamic sets
                currentExSets.forEachIndexed { setIdx, record ->
                    var weightStr by remember(currentIndex, setIdx) { mutableStateOf(record.weightKg.toString()) }
                    var repsStr by remember(currentIndex, setIdx) { mutableStateOf(record.repsCompleted.toString()) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Set number
                        Text(
                            "#${record.setNumber}",
                            fontWeight = FontWeight.Bold,
                            color = if (record.isChecked) TextMuted else TechCyan,
                            fontFamily = TechMonospace,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )

                        // Weight input with minus and plus buttons for quick elite adjustments
                        Row(
                            modifier = Modifier.weight(2f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "◀",
                                color = ToxicGreen,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clickable {
                                        val w = (weightStr.toDoubleOrNull() ?: 10.0) - 2.0
                                        weightStr = w.coerceAtLeast(0.0).toString()
                                        viewModel.updateSetRecord(currentIndex, setIdx, w.coerceAtLeast(0.0), repsStr.toIntOrNull() ?: 8, record.isChecked, record.wasRpeMet)
                                    }
                                    .padding(horizontal = 6.dp)
                            )
                            BasicTextFieldCustom(
                                value = weightStr,
                                onValueChange = {
                                    val sanitized = it.trim().replace(',', '.')
                                    if (sanitized.isEmpty() || sanitized.toDoubleOrNull() != null || sanitized == "." || sanitized.endsWith(".")) {
                                        weightStr = sanitized
                                        val w = sanitized.toDoubleOrNull()
                                        if (w != null) {
                                            viewModel.updateSetRecord(currentIndex, setIdx, w, repsStr.toIntOrNull() ?: 8, record.isChecked, record.wasRpeMet)
                                        }
                                    }
                                },
                                modifier = Modifier.width(42.dp),
                                isChecked = record.isChecked
                            )
                            Text(
                                "▶",
                                color = ToxicGreen,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clickable {
                                        val w = (weightStr.toDoubleOrNull() ?: 10.0) + 2.0
                                        weightStr = w.toString()
                                        viewModel.updateSetRecord(currentIndex, setIdx, w, repsStr.toIntOrNull() ?: 8, record.isChecked, record.wasRpeMet)
                                    }
                                    .padding(horizontal = 6.dp)
                            )
                        }

                        // Reps input with increments / decrements
                        Row(
                            modifier = Modifier.weight(2f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "◀",
                                color = TechCyan,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clickable {
                                        val r = (repsStr.toIntOrNull() ?: 8) - 1
                                        repsStr = r.coerceAtLeast(0).toString()
                                        viewModel.updateSetRecord(currentIndex, setIdx, weightStr.toDoubleOrNull() ?: 10.0, r.coerceAtLeast(0), record.isChecked, record.wasRpeMet)
                                    }
                                    .padding(horizontal = 6.dp)
                            )
                            BasicTextFieldCustom(
                                value = repsStr,
                                onValueChange = {
                                    val sanitized = it.filter { char -> char.isDigit() }
                                    repsStr = sanitized
                                    val r = sanitized.toIntOrNull()
                                    if (r != null) {
                                        viewModel.updateSetRecord(currentIndex, setIdx, weightStr.toDoubleOrNull() ?: 10.0, r, record.isChecked, record.wasRpeMet)
                                    }
                                },
                                modifier = Modifier.width(36.dp),
                                isChecked = record.isChecked
                            )
                            Text(
                                "▶",
                                color = TechCyan,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clickable {
                                        val r = (repsStr.toIntOrNull() ?: 8) + 1
                                        repsStr = r.toString()
                                        viewModel.updateSetRecord(currentIndex, setIdx, weightStr.toDoubleOrNull() ?: 10.0, r, record.isChecked, record.wasRpeMet)
                                    }
                                    .padding(horizontal = 6.dp)
                            )
                        }

                        // Done Status Checkbox
                        Box(
                            modifier = Modifier.weight(1.5f),
                            contentAlignment = Alignment.Center
                        ) {
                            Checkbox(
                                checked = record.isChecked,
                                onCheckedChange = { checked ->
                                    viewModel.updateSetRecord(
                                        exerciseIndex = currentIndex,
                                        setIndex = setIdx,
                                        weight = weightStr.toDoubleOrNull() ?: 10.0,
                                        reps = repsStr.toIntOrNull() ?: 8,
                                        checked = checked ?: false,
                                        rpeMet = record.wasRpeMet
                                    )
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = ToxicGreen,
                                    uncheckedColor = BorderDark,
                                    checkmarkColor = Color.Black
                                ),
                                modifier = Modifier.testTag("chk_set_${setIdx}")
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Session complete/cancel actions buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.cancelWorkoutSession() },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("btn_cancel_session"),
                border = BorderStroke(1.dp, Color.Red),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Desistir", fontSize = 14.sp)
            }

            Button(
                onClick = { viewModel.finishWorkoutSession() },
                modifier = Modifier
                    .weight(1.5f)
                    .height(50.dp)
                    .testTag("btn_finish_session"),
                colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen, contentColor = Color(0xFF381E72)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Celebration, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("FINALIZAR", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

// Helpers active screen

@Composable
fun TechBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
            .border(0.5.dp, color.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            label.uppercase(),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = TechMonospace
        )
    }
}

@Composable
fun BasicTextFieldCustom(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isChecked: Boolean
) {
    val focusManager = LocalFocusManager.current
    
    Box(
        modifier = modifier
            .background(Color(0xFF1E1E24), RoundedCornerShape(4.dp))
            .border(0.5.dp, if (isChecked) Color.Gray else TechCyan, RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                color = if (isChecked) TextMuted else TextPrimary,
                fontSize = 14.sp,
                fontFamily = TechMonospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// Map dynamic exercise ID to static cinesiological paths
private fun getBiomechanicalPathType(exId: String): String {
    return when {
        exId.contains("supino_reto") -> "CHEST_PRESS"
        exId.contains("agachamento_livre") -> "SQUAT"
        exId.contains("levantamento_terra") -> "DEAD_LIFT"
        exId.contains("puxada_polia_alta") -> "LAT_PULLDOWN"
        exId.contains("elevacao_lateral") -> "LATERAL_RAISE"
        exId.contains("rosca") -> "BICEPS_CURL"
        exId.contains("testa") -> "TRICEPS_EXTENSION"
        exId.contains("leg_press_45") -> "LEG_PRESS"
        else -> "CHEST_PRESS"
    }
}


// 6. --- QUICK FLOATING REST STOPWATCH OVERLAY ---

@Composable
fun RestTimerMiniOverlay(
    remaining: Int,
    limit: Int,
    onSkip: () -> Unit
) {
    val progress = if (limit > 0) (remaining.toFloat() / limit.toFloat()).coerceIn(0f, 1f) else 1f
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .testTag("rest_timer_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonSurface),
            border = BorderStroke(2.dp, ToxicGreen),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .width(260.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "DESCANSO ATIVO",
                    fontSize = 11.sp,
                    fontFamily = TechMonospace,
                    fontWeight = FontWeight.Bold,
                    color = ToxicGreen
                )

                // Large Progress visual circle
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = ToxicGreen,
                        strokeWidth = 10.dp,
                        trackColor = Color.DarkGray
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%02d", remaining),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = TechMonospace,
                            color = TextPrimary
                        )
                        Text("SEGUNDOS", fontSize = 10.sp, color = TextMuted)
                    }
                }

                Text(
                    "Mantenha a postura alinhada, respire profundamente para reduzir a acidose celular.",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = TextPrimary
                )

                Button(
                    onClick = onSkip,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_skip_rest")
                ) {
                    Text("Pular Descanso", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun getYouTubeUrl(exerciseName: String): String {
    val cleanName = exerciseName.lowercase()
    return when {
        cleanName.contains("supino reto") || cleanName.contains("supino_reto") -> "https://youtu.be/sqOw2Y6u9G4"
        cleanName.contains("agachamento livre") || cleanName.contains("agachamento_livre") -> "https://youtu.be/m9Z88FALHeg"
        cleanName.contains("levantamento terra") || cleanName.contains("levantamento_terra") || cleanName.contains("deadlift") -> "https://youtu.be/EPK_99XidgI"
        cleanName.contains("puxada aberta") || cleanName.contains("puxada_polia_alta") || cleanName.contains("puxada na polia") -> "https://youtu.be/uF02M89A-0g"
        cleanName.contains("elevação lateral") || cleanName.contains("elevacao_lateral") -> "https://youtu.be/aMAd_m_4Cxs"
        cleanName.contains("rosca direta") || cleanName.contains("rosca_polia") || cleanName.contains("rosca no cabo") -> "https://youtu.be/T4c_uG94Pko"
        cleanName.contains("tríceps testa") || cleanName.contains("triceps_testa") -> "https://youtu.be/Osnk6p7g1t4"
        cleanName.contains("leg press") || cleanName.contains("leg_press_45") -> "https://youtu.be/S_r1K_eL_T4"
        else -> {
            val query = exerciseName.replace(" ", "+").replace("&", "%26")
            "https://www.youtube.com/results?search_query=execucao+$query+curto"
        }
    }
}

@Composable
fun UserProfileScreen(viewModel: WorkoutViewModel) {
    val weight by viewModel.userWeight.collectAsState()
    val height by viewModel.userHeight.collectAsState()
    val history by viewModel.workoutHistory.collectAsState()

    val googleLoggedIn by viewModel.isGoogleLoggedIn.collectAsState()
    val googleName by viewModel.googleUserName.collectAsState()
    val googleEmail by viewModel.googleUserEmail.collectAsState()
    val googlePhoto by viewModel.googleUserPhoto.collectAsState()

    val weightLogList by viewModel.weightHistory.collectAsState()
    var showAccountChooser by remember { mutableStateOf(false) }

    val stravaId by viewModel.stravaClientId.collectAsState()
    val stravaSecret by viewModel.stravaClientSecret.collectAsState()
    val isStravaConnected by viewModel.isStravaConnected.collectAsState()
    val isGoogleFitConnected by viewModel.isGoogleFitConnected.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val lastSync by viewModel.lastSyncedTime.collectAsState()

    var weightInput by remember(weight) { mutableStateOf(weight.toString()) }
    var heightInput by remember(height) { mutableStateOf(height.toString()) }

    var localStravaId by remember(stravaId) { mutableStateOf(stravaId) }
    var localStravaSecret by remember(stravaSecret) { mutableStateOf(stravaSecret) }

    var showCredentialsDialog by remember { mutableStateOf(false) }
    var showSuccessBanner by remember { mutableStateOf(false) }

    var isAddingCustomAccount by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("Rodrigo") }
    var customEmail by remember { mutableStateOf("Rodrigocg2@gmail.com") }

    // Statistics
    val totalWorkouts = history.size
    val totalVolume = history.sumOf { it.totalVolumeKg }
    val totalTime = history.sumOf { it.durationMinutes }

    // IMC
    val heightInMeters = height / 100f
    val imc = if (heightInMeters > 0) weight / (heightInMeters * heightInMeters) else 0f
    val (imcClassification, imcColor) = when {
        imc <= 0 -> "Incapaz de calcular" to Color.Gray
        imc < 18.5f -> "Abaixo do Peso" to TechCyan
        imc < 25.0f -> "Peso Saudável" to ToxicGreen
        imc < 30.0f -> "Sobrepeso" to ElectricOrange
        else -> "Obesidade" to Color(0xFFFF5252)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("user_profile_screen")
            .background(CarbonBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner principal do perfil
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "PERFIL BIOMÉTRICO",
                    fontFamily = TechMonospace,
                    color = ToxicGreen,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
                Text(
                    "Mantenha suas medidas atualizadas e sincronize com seu smartwatch para treinar em alto nível.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // Card de Configurações de Tema (Aparência)
        item {
            val isDark by viewModel.isDarkTheme.collectAsState()
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = null,
                                tint = ToxicGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text("Tema do Aplicativo", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                                Text(
                                    if (isDark) "Tema Escuro: Roxo & Preto" else "Tema Claro: Alta Visibilidade",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Switch(
                            checked = isDark,
                            onCheckedChange = { viewModel.setDarkTheme(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ToxicGreen,
                                checkedTrackColor = ToxicGreen.copy(alpha = 0.4f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = BorderDark
                            )
                        )
                    }
                }
            }
        }

        // Google Connection Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Lock, 
                                contentDescription = null, 
                                tint = if (googleLoggedIn) ToxicGreen else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("Perfil do Google", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                        }
                        
                        if (googleLoggedIn) {
                            Box(
                                modifier = Modifier
                                    .background(ToxicGreen.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("CONECTADO", color = ToxicGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    if (!googleLoggedIn) {
                        Text(
                            "Faça login com sua Conta Google para sincronizar seu perfil, preferências de aparelhos e histórico de evolução na nuvem.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Button(
                            onClick = { viewModel.triggerOfficialGoogleLogin() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("G", fontWeight = FontWeight.Black, color = Color(0xFF4285F4), fontSize = 14.sp)
                                }
                                Text("ENTRAR COM O GOOGLE", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(BorderDark),
                                contentAlignment = Alignment.Center
                            ) {
                                if (googlePhoto.isNotEmpty()) {
                                    coil.compose.AsyncImage(
                                        model = googlePhoto,
                                        contentDescription = "Foto de perfil do Google",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.AccountCircle, 
                                        contentDescription = "Perfil", 
                                        tint = ToxicGreen, 
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(googleName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                Text(googleEmail, color = TextMuted, fontSize = 11.sp)
                            }
                            
                            OutlinedButton(
                                onClick = { viewModel.logoutFromGoogle() },
                                border = BorderStroke(0.5.dp, Color(0xFFFF5252)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("SAIR", color = Color(0xFFFF5252), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                            }
                        }
                    }
                }
            }
        }

        // Histórico acumulado
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "HISTÓRICO ACUMULADO",
                        fontFamily = TechMonospace,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(totalWorkouts.toString(), fontSize = 24.sp, fontWeight = FontWeight.Black, color = ToxicGreen)
                            Text("TREINOS", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        }
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(BorderDark).align(Alignment.CenterVertically))
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.2f)) {
                            Text("${totalVolume.toInt()}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = ElectricOrange)
                            Text("VOL. TOTAL (KG)", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        }
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(BorderDark).align(Alignment.CenterVertically))
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("${totalTime}m", fontSize = 24.sp, fontWeight = FontWeight.Black, color = TechCyan)
                            Text("TEMPO TOTAL", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Card de biometria
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = ToxicGreen, modifier = Modifier.size(18.dp))
                        Text("Medidas Corporais", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Peso
                        Column(modifier = Modifier.weight(1f)) {
                            Text("PESO (KG)", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = weightInput,
                                onValueChange = { weightInput = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                placeholder = { Text("Ex: 80.0", color = TextMuted) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = CarbonBg,
                                    unfocusedContainerColor = CarbonBg,
                                    focusedBorderColor = ToxicGreen,
                                    unfocusedBorderColor = BorderDark,
                                    cursorColor = ToxicGreen
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        // Altura
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ALTURA (CM)", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = heightInput,
                                onValueChange = { heightInput = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                placeholder = { Text("Ex: 180", color = TextMuted) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = CarbonBg,
                                    unfocusedContainerColor = CarbonBg,
                                    focusedBorderColor = ToxicGreen,
                                    unfocusedBorderColor = BorderDark,
                                    cursorColor = ToxicGreen
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val w = weightInput.toFloatOrNull() ?: 80.0f
                            val h = heightInput.toFloatOrNull() ?: 180.0f
                            viewModel.updateWeight(w)
                            viewModel.updateHeight(h)
                            viewModel.addWeightRecord(w)
                            showSuccessBanner = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("SALVAR MEDIDAS", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = TechMonospace)
                    }

                    if (showSuccessBanner) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B3213)),
                            border = BorderStroke(0.5.dp, ToxicGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ToxicGreen, modifier = Modifier.size(16.dp))
                                Text("Medidas atualizadas com sucesso!", color = ToxicGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text(
                                    "OK",
                                    color = ToxicGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.clickable { showSuccessBanner = false }.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Card de Evolução de Peso com Gráfico de Canvas
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = ToxicGreen, modifier = Modifier.size(18.dp))
                        Text("Evolução do Peso", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Acompanhe suas oscilações de massa corporal e a curva de tendência biométrica diretamente do banco de dados local.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )
                    
                    // Canvas-drawn trend line graph
                    if (weightLogList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("CURVA DE EVOLUÇÃO", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CarbonBg)
                                .border(0.5.dp, BorderDark, RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            if (weightLogList.size < 2) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Estreando histórico (mínimo 2 registros para gerar a curva).", fontSize = 11.sp, color = TextMuted)
                                }
                            } else {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val width = size.width
                                    val height = size.height
                                    
                                    val points = weightLogList.reversed() // oldest to newest
                                    val minW = (points.minOfOrNull { it.weightKg } ?: 70f) - 4f
                                    val maxW = (points.maxOfOrNull { it.weightKg } ?: 90f) + 4f
                                    val rangeW = if (maxW - minW > 0) maxW - minW else 1f
                                    
                                    val stepX = width / (points.size - 1).coerceAtLeast(1)
                                    val path = androidx.compose.ui.graphics.Path()
                                    
                                    points.forEachIndexed { index, wt ->
                                        val pctY = (wt.weightKg - minW) / rangeW
                                        val x = index * stepX
                                        val y = height - (pctY * height)
                                        if (index == 0) {
                                            path.moveTo(x, y)
                                        } else {
                                            path.lineTo(x, y)
                                        }
                                        
                                        // nodes
                                        drawCircle(
                                            color = ToxicGreen,
                                            radius = 5f,
                                            center = androidx.compose.ui.geometry.Offset(x, y)
                                        )
                                    }
                                    
                                    drawPath(
                                        path = path,
                                        color = ToxicGreen,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("LINHA DO TEMPO", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (weightLogList.isEmpty()) {
                        Text(
                            "Sem pesagens registradas até o momento. Edite seu peso acima e salve para inaugurar sua linha do tempo.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            style = TextStyle(textAlign = TextAlign.Center),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            weightLogList.take(5).forEach { wt ->
                                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(CarbonBg, RoundedCornerShape(8.dp))
                                        .border(0.5.dp, BorderDark, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = ToxicGreen, modifier = Modifier.size(16.dp))
                                        Text(
                                            "${wt.weightKg} kg",
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            fontFamily = TechMonospace
                                        )
                                    }
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            sdf.format(Date(wt.dateRecorded)),
                                            color = TextMuted,
                                            fontSize = 11.sp,
                                            fontFamily = TechMonospace
                                        )
                                        IconButton(
                                            onClick = { viewModel.deleteWeightRecord(wt.id) },
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete, 
                                                contentDescription = "Excluir", 
                                                tint = Color.Red.copy(alpha = 0.8f), 
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Card de cálculo do IMC
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "CÁLCULO DE IMC",
                        fontFamily = TechMonospace,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Seu Índice atual:",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    String.format(java.util.Locale.US, "%.1f", imc),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = imcColor
                                )
                                Text(
                                    "kg/m²",
                                    fontSize = 14.sp,
                                    color = TextMuted,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(imcColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .border(1.dp, imcColor, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                imcClassification.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = imcColor,
                                fontFamily = TechMonospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Simulated visual bar metric
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("18.5", fontSize = 9.sp, color = TextMuted, fontFamily = TechMonospace)
                            Text("Normal", fontSize = 9.sp, color = ToxicGreen, fontWeight = FontWeight.Bold)
                            Text("25.0", fontSize = 9.sp, color = TextMuted, fontFamily = TechMonospace)
                            Text("Sobrepeso", fontSize = 9.sp, color = ElectricOrange, fontWeight = FontWeight.Bold)
                            Text("30.0", fontSize = 9.sp, color = TextMuted, fontFamily = TechMonospace)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(BorderDark)
                        ) {
                            val fillFraction = ((imc - 15f) / 20f).coerceIn(0.01f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fillFraction)
                                    .background(imcColor)
                            )
                        }
                    }
                }
            }
        }

        // Card de cálculo de calorias para emagrecimento (Peso x 20)
        item {
            val caloriesNeeded = (weight * 20).toInt()
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(ElectricOrange.copy(alpha = 0.15f), CircleShape)
                                .padding(6.dp)
                        ) {
                            Text("🔥", fontSize = 16.sp) // Fire symbol emoji
                        }
                        Text(
                            "META CALÓRICA (EMAGRECIMENTO)",
                            fontFamily = TechMonospace,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        "Consumo diário recomendado para perda de gordura corporal:",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CarbonBg, RoundedCornerShape(8.dp))
                            .border(0.5.dp, BorderDark, RoundedCornerShape(8.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "FÓRMULA (PESO × 20)",
                                fontSize = 9.sp,
                                fontFamily = TechMonospace,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${String.format(java.util.Locale.US, "%.1f", weight)} kg × 20 Kcal",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TechCyan,
                                fontFamily = TechMonospace
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "RECOMENDAÇÃO DIÁRIA",
                                fontSize = 9.sp,
                                fontFamily = TechMonospace,
                                color = ElectricOrange,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "$caloriesNeeded Kcal",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = ToxicGreen,
                                fontFamily = TechMonospace
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        "Esta estimativa calcula o aporte calórico restrito (peso x 20 kcal) ideal para induzir o emagrecimento de forma sustentável, preservando ao máximo a massa muscular acumulada nos treinos do Desfrangando.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Wearables sync & setup
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Watch, contentDescription = null, tint = ToxicGreen, modifier = Modifier.size(18.dp))
                            Text("Sincronização de Smartwatch", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                        }

                        // Connected indicator dot
                        if (isStravaConnected || isGoogleFitConnected) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(ToxicGreen)
                            )
                        }
                    }
                    Text(
                        "Monitore e importe treinos feitos no seu relógio Garmin, Polar, Strava ou celular perfeitamente.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                    )

                    // Synchronization button
                    Button(
                        onClick = { viewModel.triggerSyncWearables() },
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isStravaConnected || isGoogleFitConnected) ToxicGreen else BorderDark,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Black)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("FORÇAR SINCRONIZAÇÃO AGORA", fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = TechMonospace)
                            }
                        }
                    }

                    syncMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            msg,
                            color = ToxicGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            fontFamily = TechMonospace
                        )
                    }

                    if (lastSync > 0L) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val sdf = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault())
                        Text(
                            "Última Sincronização: ${sdf.format(java.util.Date(lastSync))}",
                            fontSize = 10.sp,
                            color = TextMuted,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontFamily = TechMonospace
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Strava integration setup row
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text("Strava", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                Text(
                                    if (isStravaConnected) "Conta Ativa e Vinculada" else "Não conectado",
                                    fontSize = 11.sp,
                                    color = if (isStravaConnected) ToxicGreen else TextMuted
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                if (!isStravaConnected) {
                                    Button(
                                        onClick = { showCredentialsDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFC6100)), // Strava orange Color
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        modifier = Modifier.height(32.dp).fillMaxWidth()
                                    ) {
                                        Text("CONECTAR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { viewModel.setStravaConnected(false) },
                                        border = BorderStroke(0.5.dp, Color(0xFFFF5252)),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                        modifier = Modifier.height(32.dp).fillMaxWidth()
                                    ) {
                                        Text("DESCONECTAR", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Google Fit integration setup row
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text("Google Fit / Health Connect", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                Text(
                                    if (isGoogleFitConnected) "Conectado à Saúde de Android" else "Não conectado",
                                    fontSize = 11.sp,
                                    color = if (isGoogleFitConnected) ToxicGreen else TextMuted
                                )
                            }

                            Row(
                                modifier = Modifier.weight(1.2f)
                            ) {
                                if (!isGoogleFitConnected) {
                                    Button(
                                        onClick = { viewModel.setGoogleFitConnected(true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)), // Google Blue
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        modifier = Modifier.height(32.dp).fillMaxWidth()
                                    ) {
                                        Text("PERMITIR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { viewModel.setGoogleFitConnected(false) },
                                        border = BorderStroke(0.5.dp, Color(0xFFFF5252)),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                        modifier = Modifier.height(32.dp).fillMaxWidth()
                                    ) {
                                        Text("BLOQUEAR", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Credentials dialog for Strava Integration
    if (showCredentialsDialog) {
        AlertDialog(
            onDismissRequest = { showCredentialsDialog = false },
            containerColor = CarbonSurface,
            title = {
                Text(
                    "CONEXÃO DE API STRAVA",
                    fontFamily = TechMonospace,
                    fontWeight = FontWeight.Bold,
                    color = ToxicGreen,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Para conexão em tempo real à API de Desenvolvedores Strava, insira suas credenciais abaixo ou pule para conexão simulada.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    
                    Column {
                        Text("CLIENT ID", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = localStravaId,
                            onValueChange = { localStravaId = it },
                            placeholder = { Text("12345", color = TextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = CarbonBg,
                                unfocusedContainerColor = CarbonBg,
                                focusedBorderColor = ToxicGreen,
                                unfocusedBorderColor = BorderDark,
                                cursorColor = ToxicGreen
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column {
                        Text("CLIENT SECRET", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = localStravaSecret,
                            onValueChange = { localStravaSecret = it },
                            placeholder = { Text("abcde12345...", color = TextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = CarbonBg,
                                unfocusedContainerColor = CarbonBg,
                                focusedBorderColor = ToxicGreen,
                                unfocusedBorderColor = BorderDark,
                                cursorColor = ToxicGreen
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateStravaCredentials(localStravaId, localStravaSecret)
                        viewModel.setStravaConnected(true)
                        showCredentialsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen, contentColor = Color.Black),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("SALVAR & AUTORIZAR", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCredentialsDialog = false }) {
                    Text("CANCELAR", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                }
            }
        )
    }

    val cachedAccounts = remember(showAccountChooser, isAddingCustomAccount) { viewModel.getSavedGoogleAccounts() }

    if (showAccountChooser) {
        Dialog(
            onDismissRequest = { 
                showAccountChooser = false
                isAddingCustomAccount = false
                viewModel.clearGoogleLoginError()
            }
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                border = BorderStroke(1.dp, BorderDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Contas Google no Dispositivo",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        IconButton(
                            onClick = { 
                                showAccountChooser = false
                                isAddingCustomAccount = false
                                viewModel.clearGoogleLoginError()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted)
                        }
                    }
                    
                    if (!isAddingCustomAccount && cachedAccounts.isNotEmpty()) {
                        Text(
                            text = "Selecione uma conta Google do seu dispositivo para entrar automaticamente:",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)
                        ) {
                            items(cachedAccounts) { acc ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(CarbonBg, RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.loginWithGoogle(acc.first, acc.second, acc.third)
                                            showAccountChooser = false
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(ToxicGreen.copy(alpha = 0.2f), RoundedCornerShape(18.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            acc.first.take(1).uppercase(),
                                            color = ToxicGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(acc.first, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(acc.second, color = TextMuted, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        TextButton(
                            onClick = { isAddingCustomAccount = true },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = ToxicGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Usar outra conta", color = ToxicGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Text(
                            text = "Por favor, digite os detalhes da conta do aparelho:",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("SEU NOME", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                            OutlinedTextField(
                                value = customName,
                                onValueChange = { customName = it },
                                placeholder = { Text("Ex: João Silva", color = TextMuted) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = CarbonBg,
                                    unfocusedContainerColor = CarbonBg,
                                    focusedBorderColor = ToxicGreen,
                                    unfocusedBorderColor = BorderDark,
                                    cursorColor = ToxicGreen
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("custom_login_name_input_profile")
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("GMAIL / EMAIL OBRIGATÓRIO", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                            OutlinedTextField(
                                value = customEmail,
                                onValueChange = { customEmail = it },
                                placeholder = { Text("Ex: joao@gmail.com", color = TextMuted) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = CarbonBg,
                                    unfocusedContainerColor = CarbonBg,
                                    focusedBorderColor = ToxicGreen,
                                    unfocusedBorderColor = BorderDark,
                                    cursorColor = ToxicGreen
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("custom_login_email_input_profile")
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (cachedAccounts.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = { isAddingCustomAccount = false },
                                    border = BorderStroke(1.dp, BorderDark),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("VOLTAR", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { 
                                        showAccountChooser = false
                                        viewModel.clearGoogleLoginError()
                                    },
                                    border = BorderStroke(1.dp, BorderDark),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("CANCELAR", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                                }
                            }
                            
                            Button(
                                onClick = {
                                    val finalName = customName.trim().ifEmpty { "Atleta Desfrangando" }
                                    val finalEmail = customEmail.trim().ifEmpty { "atleta@gmail.com" }
                                    viewModel.loginWithGoogle(finalName, finalEmail, "")
                                    isAddingCustomAccount = false
                                    showAccountChooser = false
                                    viewModel.clearGoogleLoginError()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen, contentColor = Color.Black),
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("ENTRAR", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class CuratedVideo(
    val videoId: String,
    val title: String,
    val trainer: String,
    val views: String,
    val rating: String,
    val isHd: Boolean = true
)

private val curatedVideosMap = mapOf(
    "supino" to listOf(
        CuratedVideo("sqOw2Y6u9G4", "Supino Reto - Execução Correta", "Renato Cariani", "1.2M visualizações", "★★★★★ [99.2% de aprovação]", true),
        CuratedVideo("3_g_b6E0gK8", "Como Fazer Supino Reto Sem Lesão", "Leandro Twin", "850K visualizações", "★★★★★ [98.8% de aprovação]", true),
        CuratedVideo("8N_OasW3drc", "Supino Reto - Biomecânica Prática", "Laércio Refundini", "450K visualizações", "★★★★★ [99.1% de aprovação]", true)
    ),
    "agachamento" to listOf(
        CuratedVideo("m9Z88FALHeg", "Agachamento Livre Perfeito", "Leandro Twin", "1.5M visualizações", "★★★★★ [99.5% de aprovação]", true),
        CuratedVideo("Y99s2oD8m_Y", "Como Agachar Correto e Seguro", "Treino Pesado", "600K visualizações", "★★★★★ [98.2% de aprovação]", true)
    ),
    "terra" to listOf(
        CuratedVideo("EPK_99XidgI", "Levantamento Terra - Técnica Completa", "Renato Cariani", "2.0M visualizações", "★★★★★ [99.6% de aprovação]", true),
        CuratedVideo("oA065B3Nn2g", "Stiff vs Levantamento Terra Diferenças", "Leandro Twin", "350K visualizações", "★★★★☆ [97.5% de aprovação]", true)
    ),
    "puxada" to listOf(
        CuratedVideo("uF02M89A-0g", "Puxada Frente / Puxada Costas", "Leandro Twin", "950K visualizações", "★★★★★ [98.9% de aprovação]", true),
        CuratedVideo("B8mG0Zc_VvM", "Puxada na Polia Técnica Absoluta", "Laércio Refundini", "530K visualizações", "★★★★★ [99.0% de aprovação]", true)
    ),
    "elevação" to listOf(
        CuratedVideo("aMAd_m_4Cxs", "Elevação Lateral - Erro Imperdoável", "Leandro Twin", "2.1M visualizações", "★★★★★ [99.4% de aprovação]", true),
        CuratedVideo("tqcoE1oY578", "Elevação Lateral Técnica Incrível", "Laércio Refundini", "1.2M visualizações", "★★★★★ [98.7% de aprovação]", true)
    ),
    "rosca" to listOf(
        CuratedVideo("T4c_uG94Pko", "Rosca Direta de Biceps Perfeita", "Renato Cariani", "1.1M visualizações", "★★★★★ [99.3% de aprovação]", true),
        CuratedVideo("Eee93b4t0_g", "Rosca Direta com Barra ou Halteres", "Leandro Twin", "780K visualizações", "★★★★★ [98.5% de aprovação]", true)
    ),
    "tríceps" to listOf(
        CuratedVideo("Osnk6p7g1t4", "Tríceps Testa de Verdade", "Leandro Twin", "720K visualizações", "★★★★★ [99.1% de aprovação]", true),
        CuratedVideo("X14xM22Gv2A", "Dicas de Tríceps Testa Sem Dor", "Laércio Refundini", "480K visualizações", "★★★★★ [98.4% de aprovação]", true)
    ),
    "leg" to listOf(
        CuratedVideo("S_r1K_eL_T4", "Leg Press 45 sem ferrar o joelho/lombar", "Renato Cariani", "1.8M visualizações", "★★★★★ [99.2% de aprovação]", true)
    ),
    "desenvolvimento" to listOf(
        CuratedVideo("tB_VqYhC6d0", "Desenvolvimento com Halter para Ombros", "Leandro Twin", "880K visualizações", "★★★★★ [98.9% de aprovação]", true)
    ),
    "cadeira" to listOf(
        CuratedVideo("C7bXitV7-2Y", "Cadeira Extensora Perfeita", "Laércio Refundini", "950K visualizações", "★★★★★ [99.1% de aprovação]", true)
    )
)

private fun getCuratedVideosForExercise(exerciseName: String): List<CuratedVideo> {
    val clean = exerciseName.lowercase()
    for ((key, list) in curatedVideosMap) {
        if (clean.contains(key)) return list
    }
    return listOf(
        CuratedVideo(
            "SEARCH_QUERY_FALLBACK", 
            "$exerciseName - Guia de Execução Correta", 
            "Instrutor Fitness Pro", 
            "Conteúdo Integrado • HD", 
            "★★★★★ [Excelente] "
        )
    )
}

private fun generateYouTubeSearchQuery(exerciseName: String): String {
    val cleanName = exerciseName.trim()
    val isTecnicaKeyword = cleanName.lowercase().let { 
        it.contains("agachamento") || it.contains("terra") || it.contains("stiff") || it.contains("clean") || it.contains("snatch")
    }
    val suffix = if (isTecnicaKeyword) "técnica correta" else "execução correta"
    return "$cleanName $suffix"
}

@Composable
fun InAppYouTubePlayerDialog(url: String, viewModel: WorkoutViewModel, onClose: () -> Unit) {
    val exerciseNameFromVM by viewModel.activeExerciseNameForVideo.collectAsState()
    val exerciseName = exerciseNameFromVM.ifBlank { getExerciseNameFromUrl(url) }
    
    val curatedList = remember(exerciseName) { getCuratedVideosForExercise(exerciseName) }
    var selectedVideo by remember(exerciseName) { mutableStateOf(curatedList.firstOrNull()) }
    
    var isMiniPlayer by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    
    // Track in history when played
    LaunchedEffect(selectedVideo) {
        selectedVideo?.let { video ->
            if (video.videoId != "SEARCH_QUERY_FALLBACK") {
                viewModel.addToVideoHistory(video.videoId, exerciseName, video.title, video.trainer, video.views)
            }
        }
    }
    
    val favoriteIds by viewModel.favoriteVideoIds.collectAsState()
    val isCurrentFavorited = selectedVideo?.let { favoriteIds.contains(it.videoId) } ?: false
    
    val watchedHistory by viewModel.watchedVideoHistory.collectAsState()

    // Determine embed URL
    val embedUrl = remember(selectedVideo) {
        selectedVideo?.let { video ->
            if (video.videoId == "SEARCH_QUERY_FALLBACK") {
                val q = generateYouTubeSearchQuery(exerciseName)
                val encodedQ = try {
                    java.net.URLEncoder.encode(q, "UTF-8")
                } catch (e: Exception) {
                    q.replace(" ", "+")
                }
                "https://www.youtube.com/embed?listType=search&list=$encodedQ&autoplay=0"
            } else {
                "https://www.youtube.com/embed/${video.videoId}?autoplay=0"
            }
        } ?: "https://www.youtube.com/embed/sqOw2Y6u9G4?autoplay=0"
    }

    if (isMiniPlayer) {
        // Floating Mini Player overlay card at bottom right
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp, end = 16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                border = BorderStroke(1.dp, ToxicGreen),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier
                    .width(260.dp)
                    .height(200.dp)
                    .clickable(enabled = false) {}
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Title/Control bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CarbonBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = exerciseName.uppercase(),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            fontFamily = TechMonospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Maximize
                            IconButton(
                                onClick = { isMiniPlayer = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = "Expandir",
                                    tint = ToxicGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            // Close
                            IconButton(
                                onClick = onClose,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fechar",
                                    tint = Color.Red,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                    
                    // Embedded Player Block
                    Box(modifier = Modifier.weight(1f)) {
                        if (selectedVideo?.videoId == "SEARCH_QUERY_FALLBACK") {
                            Column(
                                modifier = Modifier.fillMaxSize().background(Color.Black),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, tint = Color.Red, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val query = generateYouTubeSearchQuery(exerciseName)
                                        val searchUrl = "https://www.youtube.com/results?search_query=${query.replace(" ", "+")}"
                                        try { uriHandler.openUri(searchUrl) } catch (e: Exception) {}
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                ) {
                                    Text("PESQUISAR NO YOUTUBE", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        } else {
                            selectedVideo?.let { v ->
                                LazyYouTubePlayer(videoId = v.videoId, exerciseName = exerciseName, modifier = Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Full Size Dialog Panel
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable(enabled = true, onClick = onClose)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                border = BorderStroke(1.dp, ToxicGreen),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.95f)
                    .clickable(enabled = false) {}
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CarbonBg)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "ANÁLISE DE EXECUÇÃO EM VÍDEO",
                                fontFamily = TechMonospace,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 11.sp
                            )
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            // Minimize button
                            IconButton(onClick = { isMiniPlayer = true }) {
                                Icon(
                                    imageVector = Icons.Default.Minimize,
                                    contentDescription = "Mini Player",
                                    tint = TechCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(onClick = onClose) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fechar",
                                    tint = TextMuted,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    
                    HorizontalDivider(color = BorderDark, thickness = 1.dp)
                    
                    // Frame Content List Scroll
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color.Black)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = exerciseName.uppercase(),
                                color = ToxicGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                fontFamily = TechMonospace
                            )
                        }

                        // Webview Player Block
                        item {
                            if (selectedVideo?.videoId == "SEARCH_QUERY_FALLBACK") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .background(Color.Black, RoundedCornerShape(8.dp))
                                        .border(1.dp, BorderDark, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                                        Text(text = "Vídeo indisponível para este exercício.", color = Color.White)
                                        Button(
                                            onClick = {
                                                val query = generateYouTubeSearchQuery(exerciseName)
                                                val searchUrl = "https://www.youtube.com/results?search_query=${query.replace(" ", "+")}"
                                                try { uriHandler.openUri(searchUrl) } catch (e: Exception) {}
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                        ) {
                                            Text("VÍDEOS RELACIONADOS", color = Color.White)
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .background(Color.Black, RoundedCornerShape(8.dp))
                                        .border(1.dp, BorderDark, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    selectedVideo?.let { v ->
                                        LazyYouTubePlayer(videoId = v.videoId, exerciseName = exerciseName, modifier = Modifier.fillMaxSize())
                                    }
                                }
                            }
                        }
                        
                        // Video Metadata Card
                        selectedVideo?.let { video ->
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                                    border = BorderStroke(0.5.dp, BorderDark),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color(0xFF1B3D3B), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("PREMIUM", color = TechCyan, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                                                }
                                                if (video.isHd) {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(Color(0xFF332F0C), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("HD ULTRA", color = Color(0xFFFFD700), fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                                                    }
                                                }
                                            }
                                            Text(
                                                text = video.rating, 
                                                color = ToxicGreen, 
                                                fontSize = 10.sp, 
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        
                                        Text(
                                            text = video.title,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Canal: ${video.trainer}",
                                                color = TextPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "•  ${video.views}",
                                                color = TextMuted,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Action buttons row
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Favorite Button
                                Button(
                                    onClick = { selectedVideo?.let { viewModel.toggleFavoriteVideo(it.videoId) } },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isCurrentFavorited) Color(0xFF3C111A) else CarbonSurface,
                                        contentColor = if (isCurrentFavorited) Color.Red else Color.LightGray
                                    ),
                                    border = BorderStroke(0.5.dp, if (isCurrentFavorited) Color.Red else BorderDark),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isCurrentFavorited) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                            contentDescription = "Favoritar",
                                            tint = if (isCurrentFavorited) Color.Red else Color.LightGray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(if (isCurrentFavorited) "FAVORITADO" else "FAVORITAR", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                                    }
                                }
                                
                                // Direct Search Youtube external redirect (The specific search request query from users!)
                                Button(
                                    onClick = {
                                        val query = generateYouTubeSearchQuery(exerciseName)
                                        val searchUrl = "https://www.youtube.com/results?search_query=${query.replace(" ", "+")}"
                                        try {
                                            uriHandler.openUri(searchUrl)
                                        } catch (e: Exception) {
                                            // ignore
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFF0000),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = "Pesquisar",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text("ABRIR PESQUISA NO YT", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                                    }
                                }
                            }
                        }

                        // Alternative Channels / Recommendations
                        if (curatedList.size > 1) {
                            item {
                                Text(
                                    "OUTRAS OPÇÕES DE EXECUÇÃO PROFISSIONAL",
                                    color = TechCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = TechMonospace
                                )
                            }
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    curatedList.forEach { videoRec ->
                                        if (videoRec.videoId != selectedVideo?.videoId) {
                                            Card(
                                                onClick = { selectedVideo = videoRec },
                                                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                                                border = BorderStroke(0.5.dp, BorderDark),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayCircle,
                                                        contentDescription = null,
                                                        tint = ToxicGreen,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(videoRec.title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        Text("Canal: ${videoRec.trainer} • Dica Pro", color = TextMuted, fontSize = 10.sp)
                                                    }
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowForward,
                                                        contentDescription = "Carregar",
                                                        tint = Color.Gray,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Histórico list horizontal (Horizontal row)
                        if (watchedHistory.isNotEmpty()) {
                            item {
                                Text(
                                    "VISTOS RECENTEMENTE",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = TechMonospace
                                )
                            }
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    watchedHistory.forEach { itemStr ->
                                        val parts = itemStr.split("|")
                                        val hid = parts.getOrNull(0) ?: ""
                                        val hexName = parts.getOrNull(1) ?: ""
                                        val htitle = parts.getOrNull(2) ?: "Vídeo"
                                        val htrainer = parts.getOrNull(3) ?: "Canal"
                                        val hviews = parts.getOrNull(4) ?: ""
                                        
                                        Card(
                                            onClick = {
                                                selectedVideo = CuratedVideo(hid, htitle, htrainer, hviews, "★★★★★")
                                            },
                                            colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                                            border = BorderStroke(0.5.dp, if (selectedVideo?.videoId == hid) ToxicGreen else BorderDark),
                                            modifier = Modifier.width(180.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(hexName.uppercase(), color = ToxicGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = TechMonospace, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(htitle, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                                Text(htrainer, color = TextMuted, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LazyYouTubePlayer(videoId: String, exerciseName: String, modifier: Modifier = Modifier) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var isPlayerActive by remember(videoId) { mutableStateOf(false) }
    var context = androidx.compose.ui.platform.LocalContext.current
    
    if (!isPlayerActive) {
        Box(
            modifier = modifier
                .background(Color.Black, RoundedCornerShape(8.dp))
                .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                .clickable { isPlayerActive = true },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.PlayCircle, 
                    contentDescription = null, 
                    tint = Color.Red, 
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "VER EXECUÇÃO: ${exerciseName.uppercase()}", 
                    color = Color.White, 
                    fontFamily = TechMonospace, 
                    fontSize = 12.sp
                )
                Text(
                    text = "Toque para carregar o vídeo", 
                    color = Color.Gray, 
                    fontSize = 10.sp
                )
            }
        }
    } else {
        AndroidView(
            factory = { ctx ->
                com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView(ctx).apply {
                    lifecycleOwner.lifecycle.addObserver(this)
                    addYouTubePlayerListener(object : com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer) {
                            youTubePlayer.loadVideo(videoId, 0f)
                        }
                    })
                }
            },
            modifier = modifier,
            onRelease = { view ->
                lifecycleOwner.lifecycle.removeObserver(view)
                view.release()
            }
        )
    }
}

// Fixed syntax error from partial replace:
/*
    val exerciseName = remember(url) { getExerciseNameFromUrl(url) }
    var showWebView by remember { mutableStateOf(false) } // Represents "internal player (beta)" mode
    var activeLabTab by remember { mutableStateOf(0) } // 0 = Video, 1 = Biomechanics
    val uriHandler = LocalUriHandler.current
    
    // Bio Simulation playback states
    var isSimulatedPlaying by remember { mutableStateOf(true) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    
    // Smooth transition using Compose animation system (offloads from Main thread)
    val infiniteTransition = rememberInfiniteTransition(label = "biomechanics_dialog")
    val baseDuration = 4000
    val duration = (baseDuration / playbackSpeed.coerceAtLeast(0.1f)).toInt().coerceIn(200, 20000)
    
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "motion_dialog"
    )
    
    val animationProgress = if (isSimulatedPlaying) animatedProgress else 0.25f
    val animationTime = animationProgress * 2f * Math.PI.toFloat()
    
    // Simulate dialog using a full-screen overlay Box to avoid window token / input session issues
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(enabled = true, onClick = onClose)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonSurface),
            border = BorderStroke(1.dp, ToxicGreen),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clickable(enabled = false) { /* Prevent click-through from dismissing */ }
                .testTag("in_app_youtube_dialog")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CarbonBg)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "COMENTÁRIO & COMPENDIO AD-HOC",
                            fontFamily = TechMonospace,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_player")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar Reprodutor",
                            tint = TextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                HorizontalDivider(color = BorderDark, thickness = 1.dp)
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (!showWebView) {
                        // Choice selection screen
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(CarbonBg)
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color(0xFF202020), CircleShape)
                                    .border(1.dp, ToxicGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tv,
                                    contentDescription = null,
                                    tint = ToxicGreen,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Text(
                                text = exerciseName.uppercase(),
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                fontFamily = TechMonospace,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Para lhe proporcionar o melhor guia prático de biomecânica, possuímos duas formas de visualização do vídeo:",
                                color = TextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 17.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Choice Card 1: Open external (Fast/100% recommended/stable)
                            Card(
                                onClick = {
                                    try {
                                        uriHandler.openUri(url)
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                    onClose()
                                },
                                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                                border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_watch_external_opt")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color(0xFFFF5252).copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = null,
                                            tint = Color(0xFFFF5252),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "ABRIR NO APP DO YOUTUBE",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontFamily = TechMonospace
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "RECOMENDADO: Seguro, fluido e sem falhas. Ideal para assistir de forma ágil e sem riscos de fechar o app.",
                                            color = TextMuted,
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }

                            // Choice Card 2: Open internally inside webview
                            Card(
                                onClick = {
                                    showWebView = true
                                },
                                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                                border = BorderStroke(1.dp, BorderDark),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_watch_internal_opt")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(TechCyan.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = TechCyan,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "USAR REPRODUTOR INTEGRADO (LAB)",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontFamily = TechMonospace
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Analise o trajeto biomecânico e métricas táticas diretamente nesta janela.",
                                            color = TextMuted,
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Lab Biomechanics interactive simulator with Real Youtube Video support
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(CarbonBg)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // High contrast selector tabs at the top of the integrated player
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CarbonSurface, RoundedCornerShape(8.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Button(
                                    onClick = { activeLabTab = 0 },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (activeLabTab == 0) ToxicGreen else Color.Transparent,
                                        contentColor = if (activeLabTab == 0) Color.Black else Color.White
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(
                                            imageVector = Icons.Default.PlayCircle,
                                            contentDescription = null,
                                            tint = if (activeLabTab == 0) Color.Black else ToxicGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text("REPRODUZIR VÍDEO", fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = TechMonospace)
                                    }
                                }
                                Button(
                                    onClick = { activeLabTab = 1 },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (activeLabTab == 1) ToxicGreen else Color.Transparent,
                                        contentColor = if (activeLabTab == 1) Color.Black else Color.White
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(
                                            imageVector = Icons.Default.Tv,
                                            contentDescription = null,
                                            tint = if (activeLabTab == 1) Color.Black else TechCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text("ESTUDO BIOMECÂNICO", fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = TechMonospace)
                                    }
                                }
                            }

                            if (activeLabTab == 0) {
                                // Dynamic WebView player that loads and plays the real YouTube video
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        "REPRODUTOR INTEGRADO - VÍDEO DO EXERCÍCIO",
                                        color = ToxicGreen,
                                        fontFamily = TechMonospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )

                                    val videoId = remember(url) {
                                        try {
                                            if (url.contains("youtu.be/")) {
                                                url.substringAfter("youtu.be/").substringBefore("?").substringBefore("&")
                                            } else if (url.contains("v=")) {
                                                url.substringAfter("v=").substringBefore("&")
                                            } else if (url.contains("embed/")) {
                                                url.substringAfter("embed/").substringBefore("?").substringBefore("&")
                                            } else {
                                                ""
                                            }
                                        } catch (e: Exception) {
                                            ""
                                        }
                                    }

                                    if (videoId.isNotBlank()) {
                                        LazyYouTubePlayer(
                                            videoId = videoId,
                                            exerciseName = exerciseName,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                                .background(Color.Black, RoundedCornerShape(8.dp))
                                                .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red, modifier = Modifier.size(32.dp))
                                                Text("URL do YouTube inválida ou não suportada.", color = TextMuted, textAlign = TextAlign.Center)
                                                Text(url, color = Color.Gray, fontSize = 10.sp, textAlign = TextAlign.Center)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Button(
                                        onClick = {
                                            try {
                                                uriHandler.openUri(url)
                                            } catch (e: Exception) {
                                                // Ignore
                                            }
                                            onClose()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.OpenInNew,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                "ABRIR NO APLICATIVO DO YOUTUBE",
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontFamily = TechMonospace
                                            )
                                        }
                                    }
                                }
                            } else {
                                // ORIGINAL SIMULATION LAB SECTION (biomechanics Canvas + info card)
                                Text(
                                    "MODELO BIOMECÂNICO COMPUTACIONAL",
                                    color = ToxicGreen,
                                    fontFamily = TechMonospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )

                                // Visual Canvas box
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .background(Color.Black, RoundedCornerShape(8.dp))
                                        .border(1.dp, BorderDark, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val width = size.width
                                        val height = size.height
                                        val centerX = width / 2f
                                        val centerY = height / 2f

                                        // 1. Draw grid lines
                                        val numGridLines = 10
                                        for (i in 1 until numGridLines) {
                                            val x = (width / numGridLines) * i
                                            val y = (height / numGridLines) * i
                                            drawLine(
                                                color = Color(0xFF151515),
                                                start = androidx.compose.ui.geometry.Offset(x, 0f),
                                                end = androidx.compose.ui.geometry.Offset(x, height),
                                                strokeWidth = 1f
                                            )
                                            drawLine(
                                                color = Color(0xFF151515),
                                                start = androidx.compose.ui.geometry.Offset(0f, y),
                                                end = androidx.compose.ui.geometry.Offset(width, y),
                                                strokeWidth = 1f
                                            )
                                        }

                                        // 2. Draw axes and biomechanics data
                                        val cycleValue = Math.sin(animationTime.toDouble()).toFloat() // oscillates between -1f and 1f

                                        when (exerciseName) {
                                            "Supino Reto" -> {
                                                // Bench line
                                                drawLine(
                                                    color = Color.DarkGray,
                                                    start = androidx.compose.ui.geometry.Offset(centerX - 120.dp.toPx(), centerY + 30.dp.toPx()),
                                                    end = androidx.compose.ui.geometry.Offset(centerX + 120.dp.toPx(), centerY + 30.dp.toPx()),
                                                    strokeWidth = 4f
                                                )
                                                // Human trunk / shoulder joint
                                                drawCircle(
                                                    color = TechCyan,
                                                    radius = 6.dp.toPx(),
                                                    center = androidx.compose.ui.geometry.Offset(centerX, centerY + 25.dp.toPx())
                                                )
                                                // Barbell path
                                                val barY = centerY - 20.dp.toPx() + (cycleValue * 35.dp.toPx())

                                                // Draw elbows mapping
                                                val elbowX = centerX - 35.dp.toPx() - (cycleValue * 10.dp.toPx())
                                                val elbowY = (centerY + 25.dp.toPx() + barY) / 2f + 15.dp.toPx()

                                                drawLine(
                                                    color = Color.LightGray,
                                                    start = androidx.compose.ui.geometry.Offset(centerX, centerY + 25.dp.toPx()),
                                                    end = androidx.compose.ui.geometry.Offset(elbowX, elbowY),
                                                    strokeWidth = 3f
                                                )
                                                drawLine(
                                                    color = Color.LightGray,
                                                    start = androidx.compose.ui.geometry.Offset(elbowX, elbowY),
                                                    end = androidx.compose.ui.geometry.Offset(centerX - 50.dp.toPx(), barY),
                                                    strokeWidth = 3f
                                                )

                                                val elbowRightX = centerX + 35.dp.toPx() + (cycleValue * 10.dp.toPx())
                                                drawLine(
                                                    color = Color.LightGray,
                                                    start = androidx.compose.ui.geometry.Offset(centerX, centerY + 25.dp.toPx()),
                                                    end = androidx.compose.ui.geometry.Offset(elbowRightX, elbowY),
                                                    strokeWidth = 3f
                                                )
                                                drawLine(
                                                    color = Color.LightGray,
                                                    start = androidx.compose.ui.geometry.Offset(elbowRightX, elbowY),
                                                    end = androidx.compose.ui.geometry.Offset(centerX + 50.dp.toPx(), barY),
                                                    strokeWidth = 3f
                                                )

                                                // Barbell bar and weights
                                                drawLine(
                                                    color = Color.White,
                                                    start = androidx.compose.ui.geometry.Offset(centerX - 90.dp.toPx(), barY),
                                                    end = androidx.compose.ui.geometry.Offset(centerX + 90.dp.toPx(), barY),
                                                    strokeWidth = 6f
                                                )
                                                drawRect(
                                                    color = Color(0xFFFF5252),
                                                    topLeft = androidx.compose.ui.geometry.Offset(centerX - 88.dp.toPx(), barY - 15.dp.toPx()),
                                                    size = androidx.compose.ui.geometry.Size(12.dp.toPx(), 30.dp.toPx())
                                                )
                                                drawRect(
                                                    color = Color(0xFFFF5252),
                                                    topLeft = androidx.compose.ui.geometry.Offset(centerX + 76.dp.toPx(), barY - 15.dp.toPx()),
                                                    size = androidx.compose.ui.geometry.Size(12.dp.toPx(), 30.dp.toPx())
                                                )
                                            }
                                            "Agachamento Livre" -> {
                                                drawLine(
                                                    color = Color.DarkGray,
                                                    start = androidx.compose.ui.geometry.Offset(centerX - 100.dp.toPx(), centerY + 70.dp.toPx()),
                                                    end = androidx.compose.ui.geometry.Offset(centerX + 100.dp.toPx(), centerY + 70.dp.toPx()),
                                                    strokeWidth = 4f
                                                )
                                                val footX = centerX - 15.dp.toPx()
                                                val footY = centerY + 70.dp.toPx()

                                                val hipY = centerY - 15.dp.toPx() + ((cycleValue + 1f) * 30.dp.toPx())
                                                val hipX = centerX - 35.dp.toPx() - ((cycleValue + 1f) * 10.dp.toPx())

                                                val kneeY = centerY + 30.dp.toPx() + ((cycleValue + 1f) * 18.dp.toPx())
                                                val kneeX = centerX - 60.dp.toPx()

                                                drawLine(
                                                    color = Color.LightGray,
                                                    start = androidx.compose.ui.geometry.Offset(hipX, hipY),
                                                    end = androidx.compose.ui.geometry.Offset(kneeX, kneeY),
                                                    strokeWidth = 3f
                                                )
                                                drawLine(
                                                    color = Color.LightGray,
                                                    start = androidx.compose.ui.geometry.Offset(kneeX, kneeY),
                                                    end = androidx.compose.ui.geometry.Offset(footX, footY),
                                                    strokeWidth = 3f
                                                )

                                                val headX = hipX + 15.dp.toPx() - ((cycleValue + 1f) * 8.dp.toPx())
                                                val headY = hipY - 50.dp.toPx()
                                                drawLine(
                                                    color = TechCyan,
                                                    start = androidx.compose.ui.geometry.Offset(hipX, hipY),
                                                    end = androidx.compose.ui.geometry.Offset(headX, headY),
                                                    strokeWidth = 4f
                                                )

                                                // Back Barbell
                                                val barX = headX - 5.dp.toPx()
                                                val barY = headY + 5.dp.toPx()
                                                drawCircle(
                                                    color = Color.White,
                                                    radius = 8.dp.toPx(),
                                                    center = androidx.compose.ui.geometry.Offset(barX, barY)
                                                )
                                            }
                                            else -> {
                                                // Dynamic muscular tension spectrogram
                                                val wavePoints = 120
                                                val step = width / wavePoints
                                                val path = androidx.compose.ui.graphics.Path()
                                                path.moveTo(0f, centerY)
                                                for (px in 0..wavePoints) {
                                                    val x = px * step
                                                    val ratio = px.toFloat() / wavePoints
                                                    val amplitude = 50.dp.toPx() * (1f - ratio) * (1f - Math.abs(cycleValue) * 0.3f)
                                                    val freqOffset = animationTime * 3f
                                                    val y = centerY + Math.sin(px * 0.15 + freqOffset).toFloat() * amplitude + Math.cos(px * 0.08 + freqOffset * 1.5).toFloat() * (amplitude * 0.3f)
                                                    path.lineTo(x, y)
                                                }
                                                drawPath(
                                                    path = path,
                                                    color = ToxicGreen,
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                                                )

                                                drawLine(
                                                    color = Color(0xFFFF5252).copy(alpha = 0.6f),
                                                    start = androidx.compose.ui.geometry.Offset(centerX - 80.dp.toPx() + cycleValue * 30.dp.toPx(), centerY - 60.dp.toPx()),
                                                    end = androidx.compose.ui.geometry.Offset(centerX - 80.dp.toPx() + cycleValue * 30.dp.toPx(), centerY + 60.dp.toPx()),
                                                    strokeWidth = 2f
                                                )
                                            }
                                        }

                                        // Range of motion overlay circle
                                        drawCircle(
                                            color = ToxicGreen.copy(alpha = 0.15f),
                                            radius = 18.dp.toPx() + (cycleValue * 4.dp.toPx()),
                                            center = androidx.compose.ui.geometry.Offset(centerX + 80.dp.toPx(), centerY - 40.dp.toPx())
                                        )
                                        drawCircle(
                                            color = ToxicGreen,
                                            radius = 3.dp.toPx(),
                                            center = androidx.compose.ui.geometry.Offset(centerX + 80.dp.toPx(), centerY - 40.dp.toPx())
                                        )
                                    }

                                    // Telemetry screen details
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(10.dp),
                                        contentAlignment = Alignment.TopStart
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    "EX: ${exerciseName.uppercase()}",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp,
                                                    fontFamily = TechMonospace
                                                )
                                                Text(
                                                    "FREQ: ${(60f + Math.sin(animationTime.toDouble()).toFloat() * 12f).toInt()} Hz",
                                                    color = Color.Gray,
                                                    fontSize = 8.sp,
                                                    fontFamily = TechMonospace
                                                )
                                            }
                                            Text(
                                                if (isSimulatedPlaying) "● SIMULAÇÃO RENDERIZANDO" else "⏸ PAUSADO",
                                                color = if (isSimulatedPlaying) ToxicGreen else Color(0xFFFF5252),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 8.sp,
                                                fontFamily = TechMonospace
                                            )
                                        }
                                    }
                                }

                                // Interactive simulation controls
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { isSimulatedPlaying = !isSimulatedPlaying },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(CarbonSurface, RoundedCornerShape(4.dp))
                                                .border(0.5.dp, BorderDark, RoundedCornerShape(4.dp))
                                        ) {
                                            Icon(
                                                imageVector = if (isSimulatedPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                                contentDescription = "Controle de Execução",
                                                tint = ToxicGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        TextButton(
                                            onClick = {
                                                playbackSpeed = when (playbackSpeed) {
                                                    0.5f -> 1.0f
                                                    1.0f -> 2.0f
                                                    else -> 0.5f
                                                }
                                            },
                                            modifier = Modifier
                                                .height(36.dp)
                                                .background(CarbonSurface, RoundedCornerShape(4.dp))
                                                .border(0.5.dp, BorderDark, RoundedCornerShape(4.dp)),
                                            contentPadding = PaddingValues(horizontal = 10.dp)
                                        ) {
                                            Text(
                                                "VEL: ${playbackSpeed}X",
                                                color = ToxicGreen,
                                                fontSize = 10.sp,
                                                fontFamily = TechMonospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    val sinVal = Math.sin(animationTime.toDouble())
                                    val angleValue = (100f + sinVal * 25f).toInt()
                                    val velocityValue = 0.4f + sinVal * 0.15f
                                    val formattedVelocity = String.format(java.util.Locale.US, "%.2f", velocityValue)
                                    Text(
                                        text = "ANG: ${angleValue}º  |  VEL: $formattedVelocity m/s",
                                        color = TextMuted,
                                        fontSize = 9.sp,
                                        fontFamily = TechMonospace
                                    )
                                }

                                // Specifications Card
                                val tele = remember(exerciseName) { getExerciseTelemetry(exerciseName) }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                                    border = BorderStroke(0.5.dp, BorderDark),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            "Músculos-Alvo: ${tele.targetMuscles}",
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Amplitude Ótima (ROM): ${tele.rom}",
                                            color = TechCyan,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = TechMonospace
                                        )
                                        Text(
                                            "Tática de Segurança: ${tele.safetyTactics}",
                                            color = Color(0xFFFFCC00),
                                            fontSize = 10.sp,
                                            lineHeight = 13.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                // High fidelity YouTube backup trigger
                                Button(
                                    onClick = {
                                        try {
                                            uriHandler.openUri(url)
                                        } catch (e: Exception) {
                                            // Ignore
                                        }
                                        onClose()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("btn_simulation_open_external")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            "ABRIR VÍDEO COMPLETO NO YOUTUBE",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontFamily = TechMonospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CarbonBg)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick = {
                            try {
                                uriHandler.openUri(url)
                            } catch (e: Exception) {
                                // Ignore
                            }
                            onClose()
                        }
                    ) {
                        Text(
                            "Toque aqui se quiser assistir fora deste app",
                            color = TechCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                            fontFamily = TechMonospace
                        )
                    }
                }
            }
        }
    }
}
*/

private fun getExerciseNameFromUrl(url: String): String {
    return when {
         url.contains("sqOw2Y6u9G4") -> "Supino Reto"
         url.contains("m9Z88FALHeg") -> "Agachamento Livre"
         url.contains("EPK_99XidgI") -> "Levantamento Terra"
         url.contains("uF02M89A-0g") -> "Puxada Aberta na Polia"
         url.contains("aMAd_m_4Cxs") -> "Elevação Lateral"
         url.contains("T4c_uG94Pko") -> "Rosca Direta"
         url.contains("Osnk6p7g1t4") -> "Tríceps Testa"
         url.contains("S_r1K_eL_T4") -> "Leg Press 45º"
         url.contains("search_query=") -> {
             val q = url.substringAfter("search_query=").substringBefore("&").replace("+", " ")
             if (q.isNotEmpty()) q.replace("%20", " ").uppercase() else "Exercício"
         }
         else -> "Execução do Exercício"
    }
}


private fun getInAppYouTubeEmbedUrl(originalUrl: String): String {
    if (originalUrl.contains("youtu.be/")) {
        val videoId = originalUrl.substringAfter("youtu.be/").substringBefore("?").substringBefore("&")
        return "https://www.youtube.com/embed/$videoId?autoplay=1"
    } else if (originalUrl.contains("youtube.com/watch")) {
        val videoId = originalUrl.substringAfter("v=").substringBefore("&")
        return "https://www.youtube.com/embed/$videoId?autoplay=1"
    }
    return originalUrl
}

private fun getExerciseTelemetry(exercise: String): ExerciseTelemetryData {
    return when (exercise) {
        "Supino Reto" -> ExerciseTelemetryData(
            targetMuscles = "Peitoral Maior, Deltoide Anterior, Tríceps Braquial",
            rom = "95º - 110º (Flexão do Cotovelo)",
            safetyTactics = "Mantenha as escápulas retraídas e aduzidas no banco. Não estenda os cotovelos abruptamente de forma híper-estendida de uma só vez.",
            velocity = "0.35 - 0.55 m/s (Fase Concêntrica Controlada)"
        )
        "Agachamento Livre" -> ExerciseTelemetryData(
            targetMuscles = "Quadríceps Femoral, Glúteo Máximo, Isquiotibiais",
            rom = "120º - 135º (Flexão do Joelho)",
            safetyTactics = "Mantenha a coluna vertebral neutra e a pressão centralizada nos calcanhares. Evite o colapso medial dos joelhos (valgo dinâmico).",
            velocity = "0.28 - 0.48 m/s"
        )
        "Levantamento Terra" -> ExerciseTelemetryData(
            targetMuscles = "Cadeia Posterior, Eretores da Espinha, Glúteos",
            rom = "Total (Extensão Completa de Quadril/Joelho)",
            safetyTactics = "Inicie com a barra próxima às tíbias. Ative fortemente o grande dorsal anterior à saída para travar a coluna torácica.",
            velocity = "0.20 - 0.40 m/s"
        )
        "Puxada Aberta na Polia" -> ExerciseTelemetryData(
            targetMuscles = "Latíssimo do Dorso, Redondo Maior, Bíceps",
            rom = "Completa (Puxada até altura da clavícula)",
            safetyTactics = "Foque em puxar o peso com os cotovelos, e não com as mãos. Evite usar excesso de balanço do tronco anterior/posterior.",
            velocity = "0.45 - 0.65 m/s"
        )
        "Elevação Lateral" -> ExerciseTelemetryData(
            targetMuscles = "Deltoide Lateral (Porção Acromial)",
            rom = "80º - 90º (Abdução de Ombro)",
            safetyTactics = "Mantenha uma leve inclinação do tronco para frente e cotovelos discretamente fletidos. Não eleve acima da linha dos ombros.",
            velocity = "0.50 - 0.70 m/s"
        )
        "Rosca Direta" -> ExerciseTelemetryData(
            targetMuscles = "Bíceps Braquial, Braquial, Braquiorradial",
            rom = "Flexão máxima mantendo a tensão de pico constante",
            safetyTactics = "Mantenha os cotovelos fixos ao lado do tronco. Evite a projeção dos ombros para frente durante a flexão.",
            velocity = "0.40 - 0.60 m/s"
        )
        "Tríceps Testa" -> ExerciseTelemetryData(
            targetMuscles = "Tríceps (Enfase na Cabeça Longa)",
            rom = "90º - 120º (Flexão de Cotovelo)",
            safetyTactics = "Mantenha os cotovelos paralelos e apontados rigidamente para cima. Descubra a barra próxima à linha do topo da testa.",
            velocity = "0.38 - 0.58 m/s"
        )
        "Leg Press 45º" -> ExerciseTelemetryData(
            targetMuscles = "Quadríceps, Glúteo Máximo, Adutores",
            rom = "90º (Ângulo do Joelho)",
            safetyTactics = "Garanta que o quadril permaneça firmemente em contato com o encosto. Não realize retroversão pélvica.",
            velocity = "0.30 - 0.50 m/s"
        )
        else -> ExerciseTelemetryData(
            targetMuscles = "Sinergia Muscular Dinâmica Múltipla",
            rom = "Fisiológica Adaptada ao Movimento",
            safetyTactics = "Mantenha a cadência uniforme, ativação central do Core e postura neutra durante a fase ativa do exercício.",
            velocity = "Cadência de 2:2 segundos"
        )
    }
}

data class ExerciseTelemetryData(
    val targetMuscles: String,
    val rom: String,
    val safetyTactics: String,
    val velocity: String
)

@Composable
fun StrongChickenMascot(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Detailed, high-fidelity Canvas drawing of the muscular chicken and shield
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas
            
            // 1. Dark gray outer gear / cog wheel shape background
            val gearPath = androidx.compose.ui.graphics.Path().apply {
                val cx = w / 2f
                val cy = h * 0.44f
                val outerRadius = w * 0.4f
                val innerRadius = w * 0.35f
                val numTeeth = 10
                
                // Draw rotating cog teeth
                for (i in 0 until numTeeth) {
                    val angle1 = (i * 2 * Math.PI / numTeeth).toFloat()
                    val angle2 = ((i + 0.5) * 2 * Math.PI / numTeeth).toFloat()
                    val angle3 = ((i + 1) * 2 * Math.PI / numTeeth).toFloat()
                    
                    // Tooth tip
                    val x1 = cx + outerRadius * Math.cos(angle1.toDouble()).toFloat()
                    val y1 = cy + outerRadius * Math.sin(angle1.toDouble()).toFloat()
                    val x2 = cx + outerRadius * Math.cos(angle2.toDouble()).toFloat()
                    val y2 = cy + outerRadius * Math.sin(angle2.toDouble()).toFloat()
                    
                    // Base of tooth
                    val x3 = cx + innerRadius * Math.cos(angle2.toDouble()).toFloat()
                    val y3 = cy + innerRadius * Math.sin(angle2.toDouble()).toFloat()
                    val x4 = cx + innerRadius * Math.cos(angle3.toDouble()).toFloat()
                    val y4 = cy + innerRadius * Math.sin(angle3.toDouble()).toFloat()
                    
                    if (i == 0) {
                        moveTo(x1, y1)
                    } else {
                        lineTo(x1, y1)
                    }
                    lineTo(x2, y2)
                    lineTo(x3, y3)
                    lineTo(x4, y4)
                }
                close()
            }
            
            // Shadow behind the logo
            drawPath(
                path = gearPath,
                color = Color.Black.copy(alpha = 0.5f)
            )
            
            drawPath(
                path = gearPath,
                color = Color(0xFF1E1E1E)
            )
            
            drawPath(
                path = gearPath,
                color = Color(0xFF2C2C2C),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
            )
            
            // 2. Red inner base shield
            val redShieldPath = androidx.compose.ui.graphics.Path().apply {
                val cx = w / 2f
                val cy = h * 0.44f
                val r = w * 0.33f
                moveTo(cx - r, cy - r * 0.3f)
                lineTo(cx + r, cy - r * 0.3f)
                quadraticTo(cx + r * 1.1f, cy + r * 0.5f, cx, cy + r * 1.1f)
                quadraticTo(cx - r * 1.1f, cy + r * 0.5f, cx - r, cy - r * 0.3f)
                close()
            }
            
            drawPath(
                path = redShieldPath,
                color = Color(0xFF9E1B1B) // Deep red shield background
            )
            
            // Red shield white contour
            drawPath(
                path = redShieldPath,
                color = Color(0xFFFFFFFF).copy(alpha = 0.7f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )
            
            // Red shield inner highlights
            drawPath(
                path = redShieldPath,
                color = Color(0xFFFF2E2E),
                alpha = 0.15f
            )
            
            // Decorative small golden stars inside the shield
            // Left star
            val star1Path = androidx.compose.ui.graphics.Path().apply {
                val scx = w * 0.28f
                val scy = h * 0.28f
                val size = w * 0.03f
                moveTo(scx, scy - size)
                lineTo(scx + size * 0.3f, scy - size * 0.3f)
                lineTo(scx + size, scy - size * 0.3f)
                lineTo(scx + size * 0.5f, scy + size * 0.2f)
                lineTo(scx + size * 0.7f, scy + size)
                lineTo(scx, scy + size * 0.5f)
                lineTo(scx - size * 0.7f, scy + size)
                lineTo(scx - size * 0.5f, scy + size * 0.2f)
                lineTo(scx - size, scy - size * 0.3f)
                lineTo(scx - size * 0.3f, scy - size * 0.3f)
                close()
            }
            drawPath(star1Path, color = Color(0xFFFFD700))

            // Right star
            val star2Path = androidx.compose.ui.graphics.Path().apply {
                val scx = w * 0.72f
                val scy = h * 0.28f
                val size = w * 0.03f
                moveTo(scx, scy - size)
                lineTo(scx + size * 0.3f, scy - size * 0.3f)
                lineTo(scx + size, scy - size * 0.3f)
                lineTo(scx + size * 0.5f, scy + size * 0.2f)
                lineTo(scx + size * 0.7f, scy + size)
                lineTo(scx, scy + size * 0.5f)
                lineTo(scx - size * 0.7f, scy + size)
                lineTo(scx - size * 0.5f, scy + size * 0.2f)
                lineTo(scx - size, scy - size * 0.3f)
                lineTo(scx - size * 0.3f, scy - size * 0.3f)
                close()
            }
            drawPath(star2Path, color = Color(0xFFFFD700))
            
            // 3. Muscle chicken body structures
            // Shoulders & Chest (Tanned Muscle Orange/Amber)
            val leftDeltoidPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.34f, h * 0.46f)
                quadraticTo(w * 0.32f, h * 0.38f, w * 0.42f, h * 0.40f)
                quadraticTo(w * 0.44f, h * 0.48f, w * 0.34f, h * 0.46f)
                close()
            }
            drawPath(path = leftDeltoidPath, color = Color(0xFFE65100))
            drawPath(path = leftDeltoidPath, color = Color(0xFFFFA726).copy(alpha = 0.5f))
            
            val rightDeltoidPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.66f, h * 0.46f)
                quadraticTo(w * 0.68f, h * 0.38f, w * 0.58f, h * 0.40f)
                quadraticTo(w * 0.56f, h * 0.48f, w * 0.66f, h * 0.46f)
                close()
            }
            drawPath(path = rightDeltoidPath, color = Color(0xFFE65100))
            drawPath(path = rightDeltoidPath, color = Color(0xFFFFA726).copy(alpha = 0.5f))

            // Body V-Taper Torso & Abs
            val torsoPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.4f, h * 0.45f)
                lineTo(w * 0.6f, h * 0.45f)
                lineTo(w * 0.55f, h * 0.66f)
                lineTo(w * 0.45f, h * 0.66f)
                close()
            }
            drawPath(path = torsoPath, color = Color(0xFFE65100))
            
            // Red Hot Pecs
            drawOval(
                color = Color(0xFFFF7043),
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.41f, h * 0.42f),
                size = androidx.compose.ui.geometry.Size(w * 0.09f, h * 0.08f)
            )
            drawOval(
                color = Color(0xFFFF7043),
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.42f),
                size = androidx.compose.ui.geometry.Size(w * 0.09f, h * 0.08f)
            )
            // Pac borders
            drawOval(
                color = Color(0xFF5D1200),
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.41f, h * 0.42f),
                size = androidx.compose.ui.geometry.Size(w * 0.09f, h * 0.08f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
            )
            drawOval(
                color = Color(0xFF5D1200),
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.42f),
                size = androidx.compose.ui.geometry.Size(w * 0.09f, h * 0.08f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
            )

            // Muscle definition for Abs
            val absY1 = h * 0.50f
            val absY2 = h * 0.55f
            val absY3 = h * 0.60f
            listOf(absY1, absY2, absY3).forEach { absY ->
                drawLine(
                    color = Color(0xFF5D1200),
                    start = androidx.compose.ui.geometry.Offset(w * 0.46f, absY),
                    end = androidx.compose.ui.geometry.Offset(w * 0.54f, absY),
                    strokeWidth = 3f
                )
            }
            drawLine(
                color = Color(0xFF5D1200),
                start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.45f),
                end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.63f),
                strokeWidth = 2.5f
            )

            // 4. Flexing Arms
            // Left Arm Lifting Heavy Dumbbell (Viewer's Left)
            val leftBicepPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.4f, h * 0.45f)
                quadraticTo(w * 0.28f, h * 0.32f, w * 0.18f, h * 0.38f) // Bicep peak & forearm curve
                quadraticTo(w * 0.22f, h * 0.48f, w * 0.32f, h * 0.48f) // Bicep lower
                close()
            }
            drawPath(path = leftBicepPath, color = Color(0xFFE65100))
            drawPath(path = leftBicepPath, color = Color(0xFFFFB74D).copy(alpha = 0.3f))
            drawPath(
                path = leftBicepPath,
                color = Color(0xFF5D1200),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )

            // Heavy Black Dumbbell
            // Left plates
            drawRoundRect(
                color = Color(0xFF151515),
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.10f, h * 0.30f),
                size = androidx.compose.ui.geometry.Size(w * 0.04f, h * 0.16f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
            )
            drawRoundRect(
                color = Color(0xFF2E2E2E),
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.12f, h * 0.32f),
                size = androidx.compose.ui.geometry.Size(w * 0.03f, h * 0.12f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
            )
            // Bar connection
            drawLine(
                color = Color(0xFF888888),
                start = androidx.compose.ui.geometry.Offset(w * 0.14f, h * 0.38f),
                end = androidx.compose.ui.geometry.Offset(w * 0.27f, h * 0.38f),
                strokeWidth = 8f
            )
            // Right plates of the dumbbell
            drawRoundRect(
                color = Color(0xFF2E2E2E),
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.24f, h * 0.32f),
                size = androidx.compose.ui.geometry.Size(w * 0.03f, h * 0.12f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
            )
            drawRoundRect(
                color = Color(0xFF151515),
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.26f, h * 0.30f),
                size = androidx.compose.ui.geometry.Size(w * 0.04f, h * 0.16f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
            )
            // Golden stars / sparkles around dumbbell
            drawCircle(color = Color(0xFFFFD700), radius = 3f, center = androidx.compose.ui.geometry.Offset(w * 0.08f, h * 0.28f))
            drawCircle(color = Color(0xFFFFD700), radius = 2f, center = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.29f))

            // Hand flexing on bar (Gold gripping fist)
            drawCircle(
                color = Color(0xFFFFB74D),
                radius = w * 0.035f,
                center = androidx.compose.ui.geometry.Offset(w * 0.20f, h * 0.38f)
            )
            drawCircle(
                color = Color(0xFF1E1E1E),
                radius = w * 0.035f,
                center = androidx.compose.ui.geometry.Offset(w * 0.20f, h * 0.38f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )

            // Right Arm Flexing (Viewer's Right)
            val rightBicepPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.6f, h * 0.45f)
                quadraticTo(w * 0.72f, h * 0.32f, w * 0.82f, h * 0.38f) // Bicep peak
                quadraticTo(w * 0.78f, h * 0.48f, w * 0.68f, h * 0.48f) // Bicep lower
                close()
            }
            drawPath(path = rightBicepPath, color = Color(0xFFE65100))
            drawPath(path = rightBicepPath, color = Color(0xFFFFB74D).copy(alpha = 0.3f))
            drawPath(
                path = rightBicepPath,
                color = Color(0xFF5D1200),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )
            // Big flexing fist on viewer's right arm
            drawCircle(
                color = Color(0xFFE65100),
                radius = w * 0.04f,
                center = androidx.compose.ui.geometry.Offset(w * 0.80f, h * 0.39f)
            )
            drawCircle(
                color = Color(0xFF5D1200),
                radius = w * 0.04f,
                center = androidx.compose.ui.geometry.Offset(w * 0.80f, h * 0.39f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
            )

            // Wristbands (Fitas Pro de Treino Vermelho)
            drawCircle(
                color = Color(0xFFFF1E1E),
                radius = w * 0.016f,
                center = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.42f)
            )
            drawCircle(
                color = Color(0xFFFF1E1E),
                radius = w * 0.016f,
                center = androidx.compose.ui.geometry.Offset(w * 0.75f, h * 0.42f)
            )

            // 5. Rooster Head
            // Neck
            val neckPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.44f, h * 0.42f)
                lineTo(w * 0.56f, h * 0.42f)
                lineTo(w * 0.54f, h * 0.32f)
                lineTo(w * 0.46f, h * 0.32f)
                close()
            }
            drawPath(path = neckPath, color = Color(0xFFFFB300))
            drawPath(
                path = neckPath,
                color = Color(0xFF5D1200),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )

            // Rooster Head Circle (White feathers core / Orange highlights)
            drawCircle(
                color = Color(0xFFFFA000), // Golden rooster face base
                radius = w * 0.09f,
                center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.30f)
            )
            drawCircle(
                color = Color(0xFF5D1200),
                radius = w * 0.09f,
                center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.30f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )

            // 6. Rooster Comb (Crista de Galo Vermelho Atlético)
            val combPath = androidx.compose.ui.graphics.Path().apply {
                val headX = w * 0.5f
                val headY = h * 0.30f
                val r = w * 0.09f
                
                // Three big peaks of the comb
                moveTo(headX - r * 0.8f, headY - r * 0.5f)
                quadraticTo(headX - r * 1.5f, headY - r * 2.2f, headX - r * 0.4f, headY - r * 1.2f)
                quadraticTo(headX, headY - r * 2.5f, headX + r * 0.4f, headY - r * 1.2f)
                quadraticTo(headX + r * 1.4f, headY - r * 2.1f, headX + r * 0.8f, headY - r * 0.4f)
                close()
            }
            drawPath(path = combPath, color = Color(0xFFE91E63)) // Deep hot athletic pink/red
            drawPath(path = combPath, color = Color(0xFFFF1744)) // Hot red overlay
            drawPath(
                path = combPath,
                color = Color(0xFF5D1200),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )

            // 7. Rooster Beak (Bico de Galo de Aço)
            val beakPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.46f, h * 0.30f)
                lineTo(w * 0.54f, h * 0.30f)
                lineTo(w * 0.5f, h * 0.38f)
                close()
            }
            drawPath(path = beakPath, color = Color(0xFFFFC107)) // Golden yellow beak
            drawPath(
                path = beakPath,
                color = Color(0xFF5D1200),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
            )

            // Red Wattle under beak (Gogó)
            val wattlePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.47f, h * 0.35f)
                quadraticTo(w * 0.46f, h * 0.41f, w * 0.50f, h * 0.41f)
                quadraticTo(w * 0.54f, h * 0.41f, w * 0.53f, h * 0.35f)
                close()
            }
            drawPath(path = wattlePath, color = Color(0xFFFF1744))
            drawPath(
                path = wattlePath,
                color = Color(0xFF5D1200),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )

            // Angry Badass Gym Sunglasses (Óculos de Sol Fodão)
            val leftShade = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.41f, h * 0.27f)
                lineTo(w * 0.49f, h * 0.27f)
                lineTo(w * 0.48f, h * 0.32f)
                lineTo(w * 0.43f, h * 0.32f)
                close()
            }
            drawPath(path = leftShade, color = Color(0xFF151515))
            drawPath(path = leftShade, color = Color(0xFFFFFFFF).copy(alpha = 0.2f))
            drawPath(
                path = leftShade,
                color = Color(0xFF5D1200),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
            )

            val rightShade = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.51f, h * 0.27f)
                lineTo(w * 0.59f, h * 0.27f)
                lineTo(w * 0.57f, h * 0.32f)
                lineTo(w * 0.52f, h * 0.32f)
                close()
            }
            drawPath(path = rightShade, color = Color(0xFF151515))
            drawPath(path = rightShade, color = Color(0xFFFFFFFF).copy(alpha = 0.2f))
            drawPath(
                path = rightShade,
                color = Color(0xFF5D1200),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
            )

            // Bridge of shades
            drawLine(
                color = Color(0xFF151515),
                start = androidx.compose.ui.geometry.Offset(w * 0.49f, h * 0.28f),
                end = androidx.compose.ui.geometry.Offset(w * 0.51f, h * 0.28f),
                strokeWidth = 3f
            )

            // Sparkle reflections on the sunglasses
            drawCircle(color = Color.White, radius = 2.5f, center = androidx.compose.ui.geometry.Offset(w * 0.44f, h * 0.29f))
            drawCircle(color = Color.White, radius = 2.5f, center = androidx.compose.ui.geometry.Offset(w * 0.54f, h * 0.29f))

            // 8. Broken Eggshell Popping Out (Ovo Quebrado)
            val eggPath = androidx.compose.ui.graphics.Path().apply {
                val eggWStart = w * 0.33f
                val eggWEnd = w * 0.67f
                val eggYTop = h * 0.58f
                val eggYBottom = h * 0.70f
                
                // Egg zig-zag cracks
                moveTo(eggWStart, eggYTop)
                lineTo(w * 0.37f, eggYTop - h * 0.04f)
                lineTo(w * 0.42f, eggYTop)
                lineTo(w * 0.47f, eggYTop - h * 0.03f)
                lineTo(w * 0.53f, eggYTop + h * 0.01f)
                lineTo(w * 0.58f, eggYTop - h * 0.04f)
                lineTo(w * 0.63f, eggYTop)
                lineTo(eggWEnd, eggYTop - h * 0.03f)
                
                // Curve to bottom
                quadraticTo(eggWEnd + w * 0.05f, eggYBottom, w * 0.5f, eggYBottom + h * 0.08f)
                quadraticTo(eggWStart - w * 0.05f, eggYBottom, eggWStart, eggYTop)
                close()
            }
            
            drawPath(
                path = eggPath,
                color = Color(0xFFFFFDF0) // Creamy egg white color
            )
            drawPath(
                path = eggPath,
                color = Color(0xFFDCD6BD),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
            )
            
            // Little cracked egg details/lines on the shell
            drawLine(
                color = Color(0xFFDCD6BD),
                start = androidx.compose.ui.geometry.Offset(w * 0.44f, h * 0.67f),
                end = androidx.compose.ui.geometry.Offset(w * 0.42f, h * 0.73f),
                strokeWidth = 2.5f
            )
            drawLine(
                color = Color(0xFFDCD6BD),
                start = androidx.compose.ui.geometry.Offset(w * 0.56f, h * 0.69f),
                end = androidx.compose.ui.geometry.Offset(w * 0.59f, h * 0.74f),
                strokeWidth = 2.5f
            )
        }
    }
}

@Composable
fun GoogleGLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.20f
        val r = (w - stroke) / 2f
        val cx = w / 2f
        val cy = h / 2f
        
        val rect = androidx.compose.ui.geometry.Rect(cx - r, cy - r, cx + r, cy + r)
        
        // Red segment (top)
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 180f,
            sweepAngle = 100f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round),
            topLeft = rect.topLeft,
            size = rect.size
        )
        // Yellow segment (left)
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 100f,
            sweepAngle = 100f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round),
            topLeft = rect.topLeft,
            size = rect.size
        )
        // Green segment (bottom-right)
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 0f,
            sweepAngle = 100f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round),
            topLeft = rect.topLeft,
            size = rect.size
        )
        // Blue segment (right-top)
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -80f,
            sweepAngle = 80f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round),
            topLeft = rect.topLeft,
            size = rect.size
        )
        
        // Horizontal bar of G in Blue
        drawLine(
            color = Color(0xFF4285F4),
            start = androidx.compose.ui.geometry.Offset(cx, cy),
            end = androidx.compose.ui.geometry.Offset(cx + r, cy),
            strokeWidth = stroke,
            cap = androidx.compose.ui.graphics.StrokeCap.Square
        )
    }
}

@Composable
fun LoginEntranceScreen(
    viewModel: WorkoutViewModel,
    onBypass: () -> Unit
) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    var showLocalAccountChooser by remember { mutableStateOf(false) }
    var isAddingCustomAccount by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("Rodrigo") }
    var customEmail by remember { mutableStateOf("Rodrigocg2@gmail.com") }

    val googleLoginError by viewModel.googleLoginError.collectAsState()
    val cachedAccounts = remember(showLocalAccountChooser, isAddingCustomAccount) { viewModel.getSavedGoogleAccounts() }

    // If official Google Login starts and fails (e.g. on emulator), open the custom chooser
    androidx.compose.runtime.LaunchedEffect(googleLoginError) {
        if (googleLoginError != null) {
            showLocalAccountChooser = true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.bg_desfrangando_imagem),
            contentDescription = "Background",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Leve dark overlay transparente para melhorar leitura
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
        
        // Glow roxo leve + Gradiente escuro suave nas bordas (fundo bottom fade)
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF9E00FF).copy(alpha = 0.15f), // Glow roxo leve no topo
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.8f),
                        Color.Black
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "DESFRANGANDO", 
                color = Color.White, 
                fontSize = 28.sp, 
                fontWeight = FontWeight.ExtraBold, 
                fontFamily = RoundedFontFamily, 
                letterSpacing = 2.sp, 
                style = TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color(0xFFA020F0), 
                        blurRadius = 16f
                    )
                )
            )
            Text(
                "TREINO & EVOLUÇÃO", 
                color = Color(0xFFB388FF), 
                fontSize = 12.sp, 
                fontWeight = FontWeight.Bold, 
                letterSpacing = 4.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                "Transforme seu Corpo.\nTransforme sua Vida.", 
                color = Color.White, 
                fontSize = 32.sp, 
                fontWeight = FontWeight.Bold, 
                textAlign = TextAlign.Center, 
                lineHeight = 36.sp,
                style = TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha=0.5f), 
                        blurRadius = 8f
                    )
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Treinos personalizados, evolução real e resultados de verdade.", 
                color = Color.LightGray, 
                fontSize = 14.sp, 
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Entrar com Google can trigger official Sign-In with elegant automatic selector fallback
            Button(
                onClick = { viewModel.triggerOfficialGoogleLogin() }, 
                modifier = Modifier.fillMaxWidth().height(52.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black), 
                shape = RoundedCornerShape(12.dp)
            ) { 
                Row(verticalAlignment = Alignment.CenterVertically) { 
                    GoogleGLogo(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Entrar com Google", fontWeight = FontWeight.Bold) 
                } 
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { 
                    isAddingCustomAccount = true
                    showLocalAccountChooser = true 
                }, 
                modifier = Modifier.fillMaxWidth().height(52.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA020F0), contentColor = Color.White), 
                shape = RoundedCornerShape(12.dp)
            ) { 
                Text("Entrar com E-mail", fontWeight = FontWeight.Bold) 
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = { 
                    isAddingCustomAccount = true
                    showLocalAccountChooser = true 
                }, 
                modifier = Modifier.fillMaxWidth().height(52.dp)
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)), // subtle glass
                border = BorderStroke(1.dp, Color(0xFFA020F0).copy(alpha=0.5f)), 
                shape = RoundedCornerShape(12.dp), 
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) { 
                Text("Criar nova conta", fontWeight = FontWeight.Bold) 
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onBypass) { 
                Text("Entrar como Visitante", color = Color.Gray, fontSize = 12.sp) 
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showLocalAccountChooser) {
        Dialog(
            onDismissRequest = { 
                showLocalAccountChooser = false
                isAddingCustomAccount = false
                viewModel.clearGoogleLoginError()
            }
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                border = BorderStroke(1.dp, BorderDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Contas Google no Dispositivo",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        IconButton(
                            onClick = { 
                                showLocalAccountChooser = false
                                isAddingCustomAccount = false
                                viewModel.clearGoogleLoginError()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted)
                        }
                    }
                    
                    if (!isAddingCustomAccount && cachedAccounts.isNotEmpty()) {
                        Text(
                            text = "Selecione uma conta Google do seu dispositivo para entrar automaticamente:",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp)
                        ) {
                            items(cachedAccounts) { acc ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(CarbonBg, RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.loginWithGoogle(acc.first, acc.second, acc.third)
                                            showLocalAccountChooser = false
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(ToxicGreen.copy(alpha = 0.2f), RoundedCornerShape(18.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            acc.first.take(1).uppercase(),
                                            color = ToxicGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(acc.first, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(acc.second, color = TextMuted, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        TextButton(
                            onClick = { isAddingCustomAccount = true },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = ToxicGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Usar outra conta", color = ToxicGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // User enters a custom account details
                        Text(
                            text = "Por favor, digite os detalhes da conta do aparelho:",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("SEU NOME", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                            OutlinedTextField(
                                value = customName,
                                onValueChange = { customName = it },
                                placeholder = { Text("Ex: João Silva", color = TextMuted) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = CarbonBg,
                                    unfocusedContainerColor = CarbonBg,
                                    focusedBorderColor = ToxicGreen,
                                    unfocusedBorderColor = BorderDark,
                                    cursorColor = ToxicGreen
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("custom_login_name_input_entrance")
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("GMAIL / EMAIL", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                            OutlinedTextField(
                                value = customEmail,
                                onValueChange = { customEmail = it },
                                placeholder = { Text("Ex: joao@gmail.com", color = TextMuted) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = CarbonBg,
                                    unfocusedContainerColor = CarbonBg,
                                    focusedBorderColor = ToxicGreen,
                                    unfocusedBorderColor = BorderDark,
                                    cursorColor = ToxicGreen
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("custom_login_email_input_entrance")
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (cachedAccounts.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = { isAddingCustomAccount = false },
                                    border = BorderStroke(1.dp, BorderDark),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("VOLTAR", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { 
                                        showLocalAccountChooser = false
                                        viewModel.clearGoogleLoginError()
                                    },
                                    border = BorderStroke(1.dp, BorderDark),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("CANCELAR", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                                }
                            }
                            
                            Button(
                                onClick = {
                                    val finalName = customName.trim().ifEmpty { "Atleta Desfrangando" }
                                    val finalEmail = customEmail.trim().ifEmpty { "atleta@gmail.com" }
                                    viewModel.loginWithGoogle(finalName, finalEmail, "")
                                    isAddingCustomAccount = false
                                    showLocalAccountChooser = false
                                    viewModel.clearGoogleLoginError()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen, contentColor = Color.Black),
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("ENTRAR", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = TechMonospace)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun parseHexColor(colorString: String): Color {
    try {
        if (colorString.startsWith("#")) {
            val colorStrWithoutHash = colorString.substring(1)
            if (colorStrWithoutHash.length == 6) {
                return Color(android.graphics.Color.parseColor("#FF$colorStrWithoutHash"))
            } else if (colorStrWithoutHash.length == 8) {
                return Color(android.graphics.Color.parseColor(colorString))
            }
        }
    } catch (e: Exception) {
        // Fallback
    }
    return Color(0xFF8B5CF6)
}

@Composable
fun ShareWorkoutDialog(workout: com.example.model.SavedWorkout, onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonSurface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Thumbnail
                Box(
                    modifier = Modifier.size(80.dp).background(parseHexColor(workout.colorHex).copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(workout.emoji, fontSize = 40.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Compartilhar Treino", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(workout.title, color = TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        val sb = StringBuilder()
                        sb.append("Meu Treino: ${workout.emoji} ${workout.title}\n\n")
                        workout.getExercises().forEach { ex ->
                            sb.append("  - ${ex.name} (${ex.sets}x${ex.repsRange})\n")
                        }
                        
                        val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            putExtra(android.content.Intent.EXTRA_TEXT, sb.toString().trim())
                            type = "text/plain"
                        }
                        val shareIntent = android.content.Intent.createChooser(sendIntent, "Compartilhar Treino")
                        context.startActivity(shareIntent)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TechCyan, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Compartilhar Lista de Exercícios", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ShareRoutineDialog(tab: com.example.model.RoutineCategory, workoutCount: Int, savedWorkouts: List<com.example.model.SavedWorkout>, onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonSurface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(80.dp).background(parseHexColor(tab.colorHex).copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(tab.emoji, fontSize = 40.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Compartilhar Rotina", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("${tab.name} ($workoutCount treinos)", color = TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        val sb = StringBuilder()
                        sb.append("Minha Rotina: ${tab.emoji} ${tab.name}\n\n")
                        savedWorkouts.forEach { wk ->
                            sb.append("${wk.emoji} ${wk.title}:\n")
                            wk.getExercises().forEach { ex ->
                                sb.append("  - ${ex.name} (${ex.sets}x${ex.repsRange})\n")
                            }
                            sb.append("\n")
                        }
                        
                        val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            putExtra(android.content.Intent.EXTRA_TEXT, sb.toString().trim())
                            type = "text/plain"
                        }
                        val shareIntent = android.content.Intent.createChooser(sendIntent, "Compartilhar Rotina")
                        context.startActivity(shareIntent)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TechCyan, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Compartilhar Lista de Treinos", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ShareOptionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(48.dp).background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, color = TextMuted, fontSize = 11.sp)
    }
}

@Composable
fun EditWorkoutDialog(
    workout: com.example.model.SavedWorkout,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var title by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(workout.title) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Treino") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nome do Treino") }
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = {
                onSave(title, workout.category, workout.emoji, workout.colorHex)
            }) { Text("SALVAR") }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text("CANCELAR") }
        }
    )
}

@Composable
fun ManualWorkoutCreatorScreen(
    initialWorkout: com.example.model.SavedWorkout?,
    availableExercises: List<com.example.model.ExerciseExecutionReference>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, List<com.example.model.WorkoutExercise>) -> Unit
) {
    var title by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(initialWorkout?.title ?: "") }
    
    var exercises by androidx.compose.runtime.remember { 
        androidx.compose.runtime.mutableStateOf<List<com.example.model.WorkoutExercise>>(initialWorkout?.getExercises()?.toList() ?: emptyList())
    }
    
    var showAddExerciseDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    var exerciseToEdit by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.example.model.WorkoutExercise?>(null) }
    var exerciseToEditIndex by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(-1) }

    Box(modifier = Modifier.fillMaxSize().background(CarbonSurface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(if (initialWorkout == null) "Criar Treino Manualmente" else "Editar Treino", color = Color.White, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                 value = title,
                 onValueChange = { title = it },
                 label = { Text("Nome do Treino (ex: Treino A - Peito)") },
                 modifier = Modifier.fillMaxWidth(),
                 colors = OutlinedTextFieldDefaults.colors(
                     focusedTextColor = Color.White,
                     unfocusedTextColor = Color.White
                 )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Exercícios", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Button(onClick = { showAddExerciseDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen)) {
                    Text("Adicionar", color = Color.Black)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(exercises.size) { index ->
                    val ex = exercises[index]
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.DarkGray)) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f).clickable { 
                                    exerciseToEdit = ex
                                    exerciseToEditIndex = index
                                }) {
                                    Text(ex.name, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("${ex.sets} séries x ${ex.repsRange} reps | ${ex.restSeconds}s", color = Color.LightGray, fontSize = 12.sp)
                                    if (ex.notes.isNotBlank()) {
                                        Text("Obs: ${ex.notes}", color = TechCyan, fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    if (index > 0) {
                                        IconButton(onClick = { 
                                            val newList = exercises.toMutableList()
                                            java.util.Collections.swap(newList, index, index - 1)
                                            exercises = newList
                                        }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Subir", tint = Color.LightGray)
                                        }
                                    }
                                    if (index < exercises.size - 1) {
                                        IconButton(onClick = { 
                                            val newList = exercises.toMutableList()
                                            java.util.Collections.swap(newList, index, index + 1)
                                            exercises = newList
                                        }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Descer", tint = Color.LightGray)
                                        }
                                    }
                                    IconButton(onClick = { 
                                        val newList = exercises.toMutableList()
                                        newList.add(index + 1, ex.copy(exerciseId = java.util.UUID.randomUUID().toString()))
                                        exercises = newList
                                    }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicar", tint = TechCyan, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { 
                                        val newList = exercises.toMutableList()
                                        newList.removeAt(index)
                                        exercises = newList
                                    }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remover", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                     onClick = onDismiss,
                     modifier = Modifier.weight(1f),
                     colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("CANCELAR", color = Color.White)
                }
                Button(
                     onClick = {
                         onSave(title, initialWorkout?.category ?: "Custom", initialWorkout?.emoji ?: "💪", initialWorkout?.colorHex ?: "#2A7CF6", initialWorkout?.splitType ?: "Manual", initialWorkout?.focus ?: "Geral", exercises)
                     },
                     modifier = Modifier.weight(1f),
                     colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen),
                     enabled = title.isNotBlank() && exercises.isNotEmpty()
                ) {
                    Text("SALVAR", color = Color.Black)
                }
            }
        }
    }
    
    if (showAddExerciseDialog) {
        var searchQuery by remember { mutableStateOf("") }
        var selectedRef by remember { mutableStateOf<com.example.model.ExerciseExecutionReference?>(null) }
        var sets by remember { mutableStateOf("3") }
        var reps by remember { mutableStateOf("10") }
        
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddExerciseDialog = false },
            title = { Text("Adicionar Exercício") },
            text = {
                Column {
                    if (selectedRef == null) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Buscar exercício...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val filtered = remember(searchQuery, availableExercises) {
                            if (searchQuery.isBlank()) {
                                availableExercises.take(30)
                            } else {
                                availableExercises.filter { 
                                    it.name.contains(searchQuery, ignoreCase = true) || 
                                    it.primaryMuscleName.contains(searchQuery, ignoreCase = true) ||
                                    it.equipment.contains(searchQuery, ignoreCase = true) ||
                                    it.type.contains(searchQuery, ignoreCase = true)
                                }.take(30)
                            }
                        }
                        LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                            items(filtered, key = { it.id }) { ref ->
                                Column(modifier = Modifier.fillMaxWidth().clickable { selectedRef = ref }.padding(8.dp)) {
                                    Text(ref.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("${ref.primaryMuscleName} • ${ref.equipment} • ${ref.type} • ${ref.difficulty}", color = Color.Gray, fontSize = 12.sp)
                                }
                                androidx.compose.material3.HorizontalDivider(color = Color.DarkGray)
                            }
                        }
                    } else {
                        Text("Exercício: ${selectedRef!!.name}", color = ToxicGreen, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = sets,
                            onValueChange = { sets = it },
                            label = { Text("Séries") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = reps,
                            onValueChange = { reps = it },
                            label = { Text("Repetições") }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { selectedRef = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("Trocar Exercício", color = Color.White) }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    if (selectedRef != null) {
                        val newEx = com.example.model.WorkoutExercise(
                            exerciseId = java.util.UUID.randomUUID().toString(),
                            name = selectedRef!!.name,
                            muscleGroup = selectedRef!!.primaryMuscleName,
                            targetMuscleDetail = selectedRef!!.primaryMuscleName,
                            sets = sets.toIntOrNull() ?: 3,
                            repsRange = reps,
                            tempo = selectedRef!!.biomechanicalTempo,
                            restSeconds = 60,
                            advancedTechnique = "None",
                            intensityRPE = 8,
                            notes = ""
                        )
                        val newList = exercises.toMutableList()
                        newList.add(newEx)
                        exercises = newList
                        showAddExerciseDialog = false
                    }
                }, enabled = selectedRef != null) {
                    Text("ADICIONAR", color = ToxicGreen)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showAddExerciseDialog = false }) { Text("CANCELAR", color = Color.Gray) }
            },
            containerColor = CarbonCard
        )
    }

    val currentExercise = exerciseToEdit
    if (currentExercise != null) {
        var editSets by remember(currentExercise.exerciseId) { mutableStateOf(currentExercise.sets.toString()) }
        var editReps by remember(currentExercise.exerciseId) { mutableStateOf(currentExercise.repsRange) }
        var editRest by remember(currentExercise.exerciseId) { mutableStateOf(currentExercise.restSeconds.toString()) }
        var editNotes by remember(currentExercise.exerciseId) { mutableStateOf(currentExercise.notes) }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { exerciseToEdit = null },
            title = { Text("Editar Execução", color = Color.White) },
            text = {
                Column {
                    Text(currentExercise.name, color = ToxicGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = editSets,
                        onValueChange = { editSets = it },
                        label = { Text("Séries") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editReps,
                        onValueChange = { editReps = it },
                        label = { Text("Repetições (ex: 8-12, 10)") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editRest,
                        onValueChange = { editRest = it },
                        label = { Text("Descanso (segundos)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text("Observações") },
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    val updated = currentExercise.copy(
                        sets = editSets.toIntOrNull() ?: 3,
                        repsRange = editReps,
                        restSeconds = editRest.toIntOrNull() ?: 60,
                        notes = editNotes
                    )
                    val newList = exercises.toMutableList()
                    newList[exerciseToEditIndex] = updated
                    exercises = newList
                    exerciseToEdit = null
                }) {
                    Text("SALVAR", color = ToxicGreen)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { exerciseToEdit = null }) { Text("CANCELAR", color = Color.Gray) }
            },
            containerColor = CarbonCard
        )
    }
}