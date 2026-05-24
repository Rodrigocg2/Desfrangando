package com.example.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.viewmodel.WorkoutViewModel
import com.example.model.*
import com.example.ui.theme.*
import java.util.UUID

data class MockCommunityWorkout(
    val id: String,
    val title: String,
    val creatorName: String,
    val creatorHandle: String,
    val likes: Int,
    val uses: Int,
    val emoji: String,
    val colorHex: String,
    val tags: List<String>,
    val description: String,
    val workoutData: SavedWorkout
)

val mockCommunityWorkouts = listOf(
    MockCommunityWorkout(
        id = UUID.randomUUID().toString(),
        title = "PPL - Push (Hipertrofia Max)",
        creatorName = "Cbum Official",
        creatorHandle = "@cbum",
        likes = 12400,
        uses = 85300,
        emoji = "🦖",
        colorHex = "#FF5555",
        tags = listOf("Hipertrofia", "Avançado", "Academia"),
        description = "Treino Push focado em volume e densidade para peitoral, ombros e tríceps.",
        workoutData = SavedWorkout(
            id = UUID.randomUUID().toString(),
            title = "PPL - Push",
            splitType = "Push",
            focus = "Hipertrofia",
            emoji = "🦖",
            colorHex = "#FF5555",
            exercisesJson = "[]"
        )
    ),
    MockCommunityWorkout(
        id = UUID.randomUUID().toString(),
        title = "Seca Barriga 20 min",
        creatorName = "Carol Borba",
        creatorHandle = "@carolborba",
        likes = 8900,
        uses = 45000,
        emoji = "🔥",
        colorHex = "#FF9900",
        tags = listOf("Emagrecimento", "Iniciante", "Casa"),
        description = "Treino HIIT rápido para fazer em casa e acelerar o metabolismo.",
        workoutData = SavedWorkout(
            id = UUID.randomUUID().toString(),
            title = "HIIT Casa",
            splitType = "Full Body",
            focus = "Emagrecimento",
            emoji = "🔥",
            colorHex = "#FF9900",
            exercisesJson = "[]"
        )
    ),
    MockCommunityWorkout(
        id = UUID.randomUUID().toString(),
        title = "Glúteos de Aço",
        creatorName = "Francielle Mattos",
        creatorHandle = "@franciellemattos",
        likes = 15200,
        uses = 62000,
        emoji = "🍑",
        colorHex = "#FF4499",
        tags = listOf("Feminino", "Inferiores", "Avançado"),
        description = "Foco total em construção de glúteos com cargas altas e técnicas avançadas.",
        workoutData = SavedWorkout(
            id = UUID.randomUUID().toString(),
            title = "Glúteos de Aço",
            splitType = "Lower",
            focus = "Hipertrofia",
            emoji = "🍑",
            colorHex = "#FF4499",
            exercisesJson = "[]"
        )
    )
)

@Composable
fun CommunityExploreScreen(viewModel: WorkoutViewModel) {
    var selectedWorkout by remember { mutableStateOf<MockCommunityWorkout?>(null) }
    
    Box(modifier = Modifier.fillMaxSize().background(CarbonBg)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 90.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Comunidade & Explorar", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Descubra os treinos mais populares do momento", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            Spacer(modifier = Modifier.height(24.dp))
            
            // Search / Filter Mock
            Row(modifier = Modifier.fillMaxWidth().height(50.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Buscar treinos, criadores...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CarbonSurface,
                        unfocusedContainerColor = CarbonSurface,
                        focusedBorderColor = ToxicGreen,
                        unfocusedBorderColor = Color.DarkGray
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Categories
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("🔥 Em Alta", "💪 Hipertrofia", "🏃 Emagrecimento", "🏠 Em Casa").forEach { tag ->
                    Box(modifier = Modifier.background(CarbonSurface, RoundedCornerShape(16.dp)).border(1.dp, Color.DarkGray, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(tag, color = Color.White, fontSize = 13.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Treinos Virais", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))
            
            mockCommunityWorkouts.forEach { workout ->
                CommunityWorkoutCard(workout) {
                    selectedWorkout = workout
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
    
    selectedWorkout?.let { workout ->
        SharedWorkoutPreviewDialog(workout = workout, onDismiss = { selectedWorkout = null }, viewModel = viewModel)
    }
}

@Composable
fun CommunityWorkoutCard(workout: MockCommunityWorkout, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(parseHexColor(workout.colorHex).copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                    Text(workout.emoji, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(workout.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(workout.creatorName, color = ToxicGreen, fontSize = 12.sp)
                }
                Icon(Icons.Default.Download, contentDescription = "Salvar", tint = TextMuted)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(workout.description, color = TextMuted, fontSize = 12.sp, maxLines = 2)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, contentDescription = "Likes", tint = Color.Red, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${workout.likes}", color = TextMuted, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Group, contentDescription = "Usuários", tint = TechCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${workout.uses} usando", color = TextMuted, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SharedWorkoutPreviewDialog(workout: MockCommunityWorkout, onDismiss: () -> Unit, viewModel: WorkoutViewModel) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonSurface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Header Thumbnail
                Box(
                    modifier = Modifier.size(80.dp).background(parseHexColor(workout.colorHex).copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(workout.emoji, fontSize = 40.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(workout.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Criado por ${workout.creatorName} (${workout.creatorHandle})", color = ToxicGreen, fontSize = 13.sp)
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(workout.description, color = TextMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Info badges
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Objetivo", color = TextMuted, fontSize = 10.sp)
                        Text(workout.workoutData.focus, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Nível", color = TextMuted, fontSize = 10.sp)
                        Text("Avançado", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Duração", color = TextMuted, fontSize = 10.sp)
                        Text("~55 min", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Compatibility check mockup
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFF2E3A24), RoundedCornerShape(12.dp)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Compatível", tint = Color(0xFF90EE90), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Treino Compatível", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Sua academia possui os equipamentos necessários.", color = Color(0xFF90EE90), fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { 
                        // Simulate saving to profile
                        val newWorkout = workout.workoutData.copy(
                            id = UUID.randomUUID().toString(),
                            title = workout.title + " (Comunidade)"
                        )
                        viewModel.saveImportedWorkout(newWorkout)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Salvar no Meu Perfil", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onDismiss, // and start now
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, ToxicGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Editar e Iniciar")
                }
            }
        }
    }
}
