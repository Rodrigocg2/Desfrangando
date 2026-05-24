package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExerciseExecutionReference
import com.example.model.SavedWorkout
import com.example.model.WorkoutExercise
import com.example.ui.theme.*
import java.util.UUID

data class DraftRoutineWorkout(
    var id: String = UUID.randomUUID().toString(),
    var dayLabel: String = "A",
    var title: String = "",
    var exercises: MutableList<WorkoutExercise> = mutableListOf()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiDayRoutineCreatorScreen(
    availableExercises: List<ExerciseExecutionReference>,
    activeTabName: String,
    onDismiss: () -> Unit,
    onSaveRoutine: (List<SavedWorkout>) -> Unit
) {
    var step by remember { mutableStateOf(1) }

    // Step 1 States
    var routineDays by remember { mutableStateOf("4") }
    var splitType by remember { mutableStateOf("ABCD") }
    var routineName by remember { mutableStateOf("Nova Rotina ABCD") }
    var focusGoal by remember { mutableStateOf("Hipertrofia") }

    // Step 2 States (Generated empty workouts)
    var drafts by remember { mutableStateOf<List<DraftRoutineWorkout>>(emptyList()) }
    var activeTabIndex by remember { mutableStateOf(0) }

    // Exercise Editor States
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var exerciseToEdit by remember { mutableStateOf<WorkoutExercise?>(null) }
    var exerciseToEditIndex by remember { mutableStateOf(-1) }

    // Copy from Tab State
    var showCopyDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(CarbonSurface)) {
        if (step == 1) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Etapa 1 - Configurar Rotina", color = Color.White, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = routineDays,
                    onValueChange = { routineDays = it },
                    label = { Text("Quantos dias de treino?") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = splitType,
                    onValueChange = { splitType = it },
                    label = { Text("Divisão (ex: ABC, Push/Pull/Legs)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = routineName,
                    onValueChange = { routineName = it },
                    label = { Text("Nome da Rotina (ex: Projeto Shape)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = focusGoal,
                    onValueChange = { focusGoal = it },
                    label = { Text("Objetivo (ex: Hipertrofia)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) { Text("CANCELAR", color = Color.White) }
                    
                    Button(
                        onClick = {
                            val days = routineDays.toIntOrNull() ?: 1
                            val newDrafts = mutableListOf<DraftRoutineWorkout>()
                            val labels = listOf("A", "B", "C", "D", "E", "F", "G")
                            for (i in 0 until days) {
                                val label = labels.getOrElse(i) { "Dia ${i+1}" }
                                newDrafts.add(DraftRoutineWorkout(dayLabel = label, title = "Treino $label"))
                            }
                            drafts = newDrafts
                            step = 2
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen)
                    ) { Text("PRÓXIMO", color = Color.Black) }
                }
            }
        } else if (step == 2) {
            Column(modifier = Modifier.fillMaxSize()) {
                ScrollableTabRow(
                    selectedTabIndex = activeTabIndex,
                    containerColor = CarbonCard,
                    contentColor = ToxicGreen,
                    edgePadding = 8.dp
                ) {
                    drafts.forEachIndexed { index, draft ->
                        Tab(
                            selected = activeTabIndex == index,
                            onClick = { activeTabIndex = index },
                            text = { Text("Treino ${draft.dayLabel}", fontWeight = if (activeTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
                
                val currentDraft = drafts[activeTabIndex]
                
                Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                    OutlinedTextField(
                        value = currentDraft.title,
                        onValueChange = {
                            val newList = drafts.toMutableList()
                            newList[activeTabIndex] = currentDraft.copy(title = it)
                            drafts = newList
                        },
                        label = { Text("Nome específico do treino (ex: Peito + Tríceps)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Exercícios", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (drafts.size > 1 && drafts.any { it.exercises.isNotEmpty() }) {
                                Button(onClick = { showCopyDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copiar de...", modifier = Modifier.size(16.dp), tint = Color.White)
                                }
                            }
                            Button(onClick = { showAddExerciseDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen)) {
                                Text("Adicionar", color = Color.Black)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(currentDraft.exercises.size) { index ->
                            val ex = currentDraft.exercises[index]
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
                                                    val newList = drafts.toMutableList()
                                                    val exList = newList[activeTabIndex].exercises.toMutableList()
                                                    java.util.Collections.swap(exList, index, index - 1)
                                                    newList[activeTabIndex] = newList[activeTabIndex].copy(exercises = exList)
                                                    drafts = newList
                                                }, modifier = Modifier.size(32.dp)) {
                                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Subir", tint = Color.LightGray)
                                                }
                                            }
                                            if (index < currentDraft.exercises.size - 1) {
                                                IconButton(onClick = { 
                                                    val newList = drafts.toMutableList()
                                                    val exList = newList[activeTabIndex].exercises.toMutableList()
                                                    java.util.Collections.swap(exList, index, index + 1)
                                                    newList[activeTabIndex] = newList[activeTabIndex].copy(exercises = exList)
                                                    drafts = newList
                                                }, modifier = Modifier.size(32.dp)) {
                                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Descer", tint = Color.LightGray)
                                                }
                                            }
                                            IconButton(onClick = { 
                                                val newList = drafts.toMutableList()
                                                val exList = newList[activeTabIndex].exercises.toMutableList()
                                                exList.add(index + 1, ex.copy(exerciseId = java.util.UUID.randomUUID().toString()))
                                                newList[activeTabIndex] = newList[activeTabIndex].copy(exercises = exList)
                                                drafts = newList
                                            }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Duplicar", tint = TechCyan, modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(onClick = { 
                                                val newList = drafts.toMutableList()
                                                val exList = newList[activeTabIndex].exercises.toMutableList()
                                                exList.removeAt(index)
                                                newList[activeTabIndex] = newList[activeTabIndex].copy(exercises = exList)
                                                drafts = newList
                                            }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Default.Delete, contentDescription = "Remover", tint = Color.Red, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { step = 1 },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) { Text("VOLTAR", color = Color.White) }
                    
                    Button(
                        onClick = {
                            val savedWorkouts = drafts.map { draft ->
                                val moshi = com.squareup.moshi.Moshi.Builder().build()
                                val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, WorkoutExercise::class.java)
                                val adapter = moshi.adapter<List<WorkoutExercise>>(listType)
                                val json = adapter.toJson(draft.exercises) ?: "[]"
                                
                                SavedWorkout(
                                    id = UUID.randomUUID().toString(),
                                    title = draft.title,
                                    splitType = splitType,
                                    focus = focusGoal,
                                    isFavorite = false,
                                    category = routineName,
                                    emoji = "⚙️",
                                    colorHex = "#BB86FC", // Premium Purple
                                    exercisesJson = json
                                )
                            }
                            onSaveRoutine(savedWorkouts)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen)
                    ) { Text("SALVAR ROTINA", color = Color.Black) }
                }
            }
        }
    }
    
    // Dialogs for Adding/Editing exercises
    if (showAddExerciseDialog) {
        var searchQuery by remember { mutableStateOf("") }
        var selectedRef by remember { mutableStateOf<ExerciseExecutionReference?>(null) }
        var sets by remember { mutableStateOf("3") }
        var reps by remember { mutableStateOf("10") }
        var rest by remember { mutableStateOf("60") }

        AlertDialog(
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
                        LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                            items(availableExercises.filter { it.name.contains(searchQuery, ignoreCase = true) || it.primaryMuscleName.contains(searchQuery, ignoreCase = true) }) { ref ->
                                Text("${ref.name} (${ref.primaryMuscleName})", modifier = Modifier.fillMaxWidth().clickable { selectedRef = ref }.padding(8.dp), color = Color.White, fontSize = 14.sp)
                                HorizontalDivider(color = Color.DarkGray)
                            }
                        }
                    } else {
                        Text("Exercício: ${selectedRef!!.name}", color = ToxicGreen, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = sets, onValueChange = { sets = it }, label = { Text("Séries") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                            OutlinedTextField(value = reps, onValueChange = { reps = it }, label = { Text("Reps") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = rest, onValueChange = { rest = it }, label = { Text("Descanso(s)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { selectedRef = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("Trocar") }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (selectedRef != null) {
                        val newEx = WorkoutExercise(
                            exerciseId = UUID.randomUUID().toString(),
                            name = selectedRef!!.name,
                            muscleGroup = selectedRef!!.primaryMuscleName,
                            targetMuscleDetail = selectedRef!!.primaryMuscleName,
                            sets = sets.toIntOrNull() ?: 3,
                            repsRange = reps,
                            tempo = selectedRef!!.biomechanicalTempo,
                            restSeconds = rest.toIntOrNull() ?: 60,
                            advancedTechnique = "Nenhuma",
                            intensityRPE = 8,
                            notes = ""
                        )
                        val newList = drafts.toMutableList()
                        val exList = newList[activeTabIndex].exercises.toMutableList()
                        exList.add(newEx)
                        newList[activeTabIndex] = newList[activeTabIndex].copy(exercises = exList)
                        drafts = newList
                        showAddExerciseDialog = false
                    }
                }) {
                    Text("ADICIONAR", color = ToxicGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExerciseDialog = false }) { Text("CANCELAR", color = Color.Gray) }
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

        AlertDialog(
            onDismissRequest = { exerciseToEdit = null },
            title = { Text("Editar Execução", color = Color.White) },
            text = {
                Column {
                    Text(currentExercise.name, color = ToxicGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = editSets,
                        onValueChange = { editSets = it },
                        label = { Text("Séries") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                TextButton(onClick = {
                    val updated = currentExercise.copy(
                        sets = editSets.toIntOrNull() ?: 3,
                        repsRange = editReps,
                        restSeconds = editRest.toIntOrNull() ?: 60,
                        notes = editNotes
                    )
                    val newList = drafts.toMutableList()
                    val exList = newList[activeTabIndex].exercises.toMutableList()
                    exList[exerciseToEditIndex] = updated
                    newList[activeTabIndex] = newList[activeTabIndex].copy(exercises = exList)
                    drafts = newList
                    exerciseToEdit = null
                }) {
                    Text("SALVAR", color = ToxicGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { exerciseToEdit = null }) { Text("CANCELAR", color = Color.Gray) }
            },
            containerColor = CarbonCard
        )
    }

    if (showCopyDialog) {
        AlertDialog(
            onDismissRequest = { showCopyDialog = false },
            title = { Text("Copiar exercícios de:") },
            text = {
                Column {
                    drafts.forEachIndexed { i, draft ->
                        if (i != activeTabIndex && draft.exercises.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    val newList = drafts.toMutableList()
                                    val exList = newList[activeTabIndex].exercises.toMutableList()
                                    // Deep copy to prevent sharing IDs
                                    val copies = draft.exercises.map { it.copy(exerciseId = UUID.randomUUID().toString()) }
                                    exList.addAll(copies)
                                    newList[activeTabIndex] = newList[activeTabIndex].copy(exercises = exList)
                                    drafts = newList
                                    showCopyDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Treino ${draft.dayLabel} (${draft.exercises.size} exerc.)", color = Color.White)
                            }
                            HorizontalDivider(color = Color.DarkGray)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCopyDialog = false }) { Text("FECHAR", color = Color.Gray) }
            },
            containerColor = CarbonCard
        )
    }
}
