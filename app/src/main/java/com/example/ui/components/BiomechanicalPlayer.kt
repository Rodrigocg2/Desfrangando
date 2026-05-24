package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.sin

@OptIn(ExperimentalTextApi::class)
@Composable
fun BiomechanicalPlayer(
    jointPathType: String,
    isPlaying: Boolean,
    playbackSpeed: Float,
    viewMode: String, // "ANATOMY", "FRONTCUT", "SIDECUT"
    modifier: Modifier = Modifier
) {
    // Progress loop for skeletal motion
    val infiniteTransition = rememberInfiniteTransition(label = "biomechanics")
    
    // Scale duration based on playback speed (standard base is 4 seconds per rep loop)
    val baseDuration = 4000
    val duration = (baseDuration / playbackSpeed.coerceAtLeast(0.1f)).toInt().coerceIn(200, 20000)
    
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "motion"
    )

    val animationProgress = if (isPlaying) animatedProgress else 0.25f

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(CarbonBg, RoundedCornerShape(16.dp))
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2f
            val centerY = canvasHeight / 2f

            // Clean sports analyzer grid lines
            drawGrid(canvasWidth, canvasHeight)

            // Dynamic joint angle mapping & draw calls matching trajectory types
            when (jointPathType.uppercase()) {
                "CHEST_PRESS" -> {
                    // Supino reto lateral view
                    // Progress mapping: 0 -> bar high, 0.4 -> touch chest, 0.5 -> pause, 0.9 -> touch ceiling
                    val cycle = sin(animationProgress * 2 * PI.toFloat()).coerceIn(-1.0f, 1.0f)
                    // relative chest displacement
                    val depth = (cycle + 1f) / 2f // 0 to 1
                    
                    val benchY = centerY + 40f
                    val headX = centerX - 80f
                    val hipX = centerX + 80f
                    val shoulderX = centerX - 20f
                    val shoulderY = centerY - 10f
                    
                    // Barbell positions
                    val barX = shoulderX
                    val barYStart = shoulderY - 80f
                    val barYEnd = shoulderY + 5f // touches chest
                    val barY = barYStart + (barYEnd - barYStart) * (1f - depth)
                    
                    // Elbow point calculation (joints move symmetrically)
                    // elbow bends down and out as bar descends
                    val elbowX = centerX - 45f
                    val elbowY = shoulderY + 45f * (1f - depth) + 15f
                    
                    // Draw bench
                    drawLine(Color(0xFF2D2D35), Offset(headX - 40f, benchY), Offset(hipX + 80f, benchY), strokeWidth = 14f, cap = StrokeCap.Round)
                    drawLine(Color(0xFF1E1E24), Offset(centerX - 10f, benchY), Offset(centerX - 10f, benchY + 80f), strokeWidth = 10f)
                    drawLine(Color(0xFF1E1E24), Offset(centerX + 40f, benchY), Offset(centerX + 40f, benchY + 80f), strokeWidth = 10f)

                    // Draw muscles (Pectoral chest group glows)
                    val chestGlowIntensity = 1f - depth
                    val chestColor = lerpColor(Color(0xFF4A4D53), ToxicGreen, chestGlowIntensity)
                    drawCircle(chestColor, radius = 22f + 8f * chestGlowIntensity, center = Offset(shoulderX + 15f, shoulderY + 10f))
                    
                    // Draw torso skeleton
                    drawLine(TextPrimary, Offset(headX, shoulderY), Offset(shoulderX, shoulderY), strokeWidth = 6f) // neck
                    drawLine(TextPrimary, Offset(shoulderX, shoulderY), Offset(hipX, benchY - 10f), strokeWidth = 8f) // trunk
                    
                    // Draw arm joints (shoulder -> elbow -> wrist/bar)
                    drawLine(TextPrimary, Offset(shoulderX, shoulderY), Offset(elbowX, elbowY), strokeWidth = 5f, cap = StrokeCap.Round)
                    drawLine(TextPrimary, Offset(elbowX, elbowY), Offset(barX, barY), strokeWidth = 5f, cap = StrokeCap.Round)
                    
                    // Draw Joint nodes
                    drawCircle(Color.LightGray, radius = 6f, center = Offset(shoulderX, shoulderY))
                    drawCircle(ToxicGreen, radius = 5f, center = Offset(elbowX, elbowY))
                    
                    // Draw barbell bar + weights
                    drawLine(Color(0xFFA1A3AC), Offset(barX - 120f, barY), Offset(barX + 120f, barY), strokeWidth = 4f)
                    // plates
                    drawRoundRect(ElectricOrange, Offset(barX - 125f, barY - 25f), size = Size(10f, 50f), cornerRadius = CornerRadius(4f))
                    drawRoundRect(ElectricOrange, Offset(barX - 110f, barY - 30f), size = Size(12f, 60f), cornerRadius = CornerRadius(4f))
                    drawRoundRect(ElectricOrange, Offset(barX + 100f, barY - 30f), size = Size(12f, 60f), cornerRadius = CornerRadius(4f))
                    drawRoundRect(ElectricOrange, Offset(barX + 115f, barY - 25f), size = Size(10f, 50f), cornerRadius = CornerRadius(4f))

                    // Angle indicators + technical telemetry text
                    val flexAngle = (75 + (depth * 95)).toInt()
                    drawTelemetryText(textMeasurer, "FLEX: $flexAngle° RPE: 9", Offset(10f, 10f))
                    drawTelemetryText(textMeasurer, "PEITORAL MAIOR [CONTRATADO: ${(chestGlowIntensity*100).toInt()}%]", Offset(10f, 32f))
                    drawCircle(Color(0x2200FFCC), radius = 30f, center = Offset(shoulderX + 15f, shoulderY + 10f))
                }
                
                "SQUAT" -> {
                    // Agachamento Livre profile
                    val cycle = sin(animationProgress * 2 * PI.toFloat()).coerceIn(-1.0f, 1.0f)
                    val depth = (cycle + 1f) / 2f // 0 to 1 represents high standard squat
                    
                    val groundY = centerY + 80f
                    val ankleX = centerX + 10f
                    val ankleY = groundY - 10f
                    
                    // hip slides down and back
                    val hipXStart = centerX - 10f
                    val hipYStart = centerY - 10f
                    val hipXEnd = centerX - 60f
                    val hipYEnd = groundY - 60f
                    val hipX = hipXStart + (hipXEnd - hipXStart) * (1f - depth)
                    val hipY = hipYStart + (hipYEnd - hipYStart) * (1f - depth)
                    
                    // knee moves slightly forward
                    val kneeXStart = centerX + 40f
                    val kneeYStart = centerY + 40f
                    val kneeXEnd = centerX + 30f
                    val kneeYEnd = groundY - 55f
                    val kneeX = kneeXStart + (kneeXEnd - kneeXStart) * (1f - depth)
                    val kneeY = kneeYStart + (kneeYEnd - kneeYStart) * (1f - depth)

                    val shoulderX = hipX + 15f
                    val shoulderY = hipY - 60f
                    
                    // Glow quadriceps
                    val quadIntensity = 1f - depth
                    val quadColor = lerpColor(Color(0xFF4A4D53), ToxicGreen, quadIntensity)
                    drawLine(quadColor, Offset(hipX, hipY), Offset(kneeX, kneeY), strokeWidth = 14f, cap = StrokeCap.Round)
                    
                    // Bones outline
                    drawLine(TextPrimary, Offset(ankleX, ankleY), Offset(kneeX, kneeY), strokeWidth = 6f) // calf
                    drawLine(TextPrimary, Offset(hipX, hipY), Offset(shoulderX, shoulderY), strokeWidth = 8f) // back spine
                    
                    // Bar on back shoulder
                    drawCircle(Color.LightGray, radius = 5f, center = Offset(shoulderX, shoulderY))
                    // weights
                    drawLine(Color(0xFFA1A3AC), Offset(shoulderX - 40f, shoulderY), Offset(shoulderX + 40f, shoulderY), strokeWidth = 5f)
                    drawCircle(ElectricOrange, radius = 25f, center = Offset(shoulderX - 40f, shoulderY))
                    drawCircle(ElectricOrange, radius = 25f, center = Offset(shoulderX + 40f, shoulderY))
                    
                    // Ground
                    drawLine(Color(0xFF2D2D35), Offset(centerX - 120f, groundY), Offset(centerX + 120f, groundY), strokeWidth = 6f)
                    
                    val kneeAngle = (65 + (depth * 110)).toInt()
                    drawTelemetryText(textMeasurer, "ANG JOELHO: $kneeAngle°", Offset(10f, 10f))
                    drawTelemetryText(textMeasurer, "TENSÃO QUADRÍCEPS: ${(quadIntensity*100).toInt()}%", Offset(10f, 32f))
                }
                
                "DEAD_LIFT" -> {
                    // Levantamento terra lateral
                    val cycle = sin(animationProgress * 2 * PI.toFloat()).coerceIn(-1.0f, 1.0f)
                    val depth = (cycle + 1f) / 2f // 0 -> loaded floor, 1 -> upright stand
                    
                    val groundY = centerY + 80f
                    val footX = centerX - 10f
                    val footY = groundY - 5f
                    
                    // Upright stand (depth = 1) vs Crouched start (depth = 0)
                    val hipX = centerX - 40f - 20f * (1f - depth)
                    val hipY = (groundY - 100f) + 40f * (1f - depth)
                    
                    val shoulderX = centerX + 10f - 40f * (1f - depth)
                    val shoulderY = (centerY - 40f) + 60f * (1f - depth)
                    
                    val handX = shoulderX
                    val handY = shoulderY + 65f
                    
                    // Barbell path
                    val barX = footX + 15f
                    val barY = groundY - 15f - (groundY - 145f) * depth

                    // Back strain & posterior chain glow
                    val postIntensity = 1f - depth
                    val backColor = lerpColor(Color(0xFF4A4D53), ElectricOrange, postIntensity)
                    drawLine(backColor, Offset(hipX, hipY), Offset(shoulderX, shoulderY), strokeWidth = 12f)
                    
                    // Skeleton legs
                    drawLine(TextPrimary, Offset(footX, footY), Offset(hipX, hipY), strokeWidth = 6f) // combined leg joint
                    // arm
                    drawLine(TextPrimary, Offset(shoulderX, shoulderY), Offset(handX, handY), strokeWidth = 5f)
                    
                    // Floor lines
                    drawLine(Color(0xFF2D2D35), Offset(centerX - 130f, groundY), Offset(centerX + 130f, groundY), strokeWidth = 4f)
                    
                    // Big bumper plates
                    drawLine(Color(0xFFA1A3AC), Offset(barX - 80f, barY), Offset(barX + 80f, barY), strokeWidth = 4f)
                    drawRoundRect(Color.Black, Offset(barX - 85f, barY - 40f), size = Size(12f, 80f), cornerRadius = CornerRadius(6f))
                    drawRoundRect(Color.DarkGray, Offset(barX - 70f, barY - 35f), size = Size(12f, 70f), cornerRadius = CornerRadius(6f))
                    drawRoundRect(Color.DarkGray, Offset(barX + 58f, barY - 35f), size = Size(12f, 70f), cornerRadius = CornerRadius(6f))
                    drawRoundRect(Color.Black, Offset(barX + 73f, barY - 40f), size = Size(12f, 80f), cornerRadius = CornerRadius(6f))

                    drawTelemetryText(textMeasurer, "MECÂNICA: HIP HINGE", Offset(10f, 10f))
                    drawTelemetryText(textMeasurer, "CADEIA POSTERIOR: ${(postIntensity*100).toInt()}% ON", Offset(10f, 32f))
                }

                "LAT_PULLDOWN" -> {
                    // Puxada dorsal posterior view
                    val cycle = sin(animationProgress * 2 * PI.toFloat()).coerceIn(-1.0f, 1.0f)
                    val depth = (cycle + 1f) / 2f // 1 -> bar low, 0 -> arms high extension
                    
                    val spineX = centerX
                    val hipY = centerY + 80f
                    val shoulderY = centerY - 10f
                    
                    val leftShoulderX = centerX - 45f
                    val rightShoulderX = centerX + 45f
                    
                    // hands holding the lat pull bar
                    val barWidth = 140f
                    val barYStart = centerY - 90f
                    val barYEnd = centerY + 15f
                    val barY = barYStart + (barYEnd - barYStart) * depth
                    
                    val leftElbowX = leftShoulderX - 25f + 10f * depth
                    val leftElbowY = shoulderY + 30f + 40f * depth
                    
                    val rightElbowX = rightShoulderX + 25f - 10f * depth
                    val rightElbowY = shoulderY + 30f + 40f * depth

                    // Dorsal (lats) glow
                    val dorsalIntensity = depth
                    val dorsalColor = lerpColor(Color(0xFF4A4D53), ToxicGreen, dorsalIntensity)
                    drawCircle(dorsalColor, radius = 25f + 5f * dorsalIntensity, center = Offset(leftShoulderX - 5f, shoulderY + 25f))
                    drawCircle(dorsalColor, radius = 25f + 5f * dorsalIntensity, center = Offset(rightShoulderX + 5f, shoulderY + 25f))

                    // Spine & shoulders outline
                    drawLine(TextPrimary, Offset(spineX, hipY), Offset(spineX, shoulderY), strokeWidth = 8f)
                    drawLine(TextPrimary, Offset(leftShoulderX, shoulderY), Offset(rightShoulderX, shoulderY), strokeWidth = 6f)
                    
                    // Left Arm joints
                    drawLine(TextPrimary, Offset(leftShoulderX, shoulderY), Offset(leftElbowX, leftElbowY), strokeWidth = 5f)
                    drawLine(TextPrimary, Offset(leftElbowX, leftElbowY), Offset(centerX - barWidth/2, barY), strokeWidth = 5f)
                    
                    // Right Arm joints
                    drawLine(TextPrimary, Offset(rightShoulderX, shoulderY), Offset(rightElbowX, rightElbowY), strokeWidth = 5f)
                    drawLine(TextPrimary, Offset(rightElbowX, rightElbowY), Offset(centerX + barWidth/2, barY), strokeWidth = 5f)
                    
                    // Cable pulling line
                    drawLine(Color.Gray, Offset(centerX, centerY - 100f), Offset(centerX, barY), strokeWidth = 2f)
                    // The bar
                    drawLine(Color.LightGray, Offset(centerX - barWidth/2, barY), Offset(centerX + barWidth/2, barY), strokeWidth = 5f, cap = StrokeCap.Round)

                    drawTelemetryText(textMeasurer, "ESCÁPULAS: DEPRIMIDAS", Offset(10f, 10f))
                    drawTelemetryText(textMeasurer, "CONTRACÃO DORSAL: ${(dorsalIntensity*100).toInt()}%", Offset(10f, 32f))
                }

                "LATERAL_RAISE" -> {
                    // Elevação lateral frontal cutoff
                    val cycle = sin(animationProgress * 2 * PI.toFloat()).coerceIn(-1.0f, 1.0f)
                    val depth = (cycle + 1f) / 2f // 1 -> wings expanded, 0 -> arms down
                    
                    val spineX = centerX
                    val headY = centerY - 60f
                    val shoulderY = centerY - 20f
                    val leftShoulderX = centerX - 35f
                    val rightShoulderX = centerX + 35f
                    
                    val angleOffsetRad = (depth * 80f) * (PI.toFloat() / 180f)
                    
                    // Trigonometry for hand position rotating outwards from shoulders
                    val lArmLength = 65f
                    val leftHandX = leftShoulderX - lArmLength * sin(angleOffsetRad)
                    val leftHandY = shoulderY + lArmLength * sin(PI.toFloat()/2f - angleOffsetRad)

                    val rightHandX = rightShoulderX + lArmLength * sin(angleOffsetRad)
                    val rightHandY = shoulderY + lArmLength * sin(PI.toFloat()/2f - angleOffsetRad)

                    // Glow lateral delts
                    val deltColor = lerpColor(Color(0xFF4A4D53), ToxicGreen, depth)
                    drawCircle(deltColor, radius = 10f + 5f * depth, center = Offset(leftShoulderX, shoulderY))
                    drawCircle(deltColor, radius = 10f + 5f * depth, center = Offset(rightShoulderX, shoulderY))

                    // Draw trunk + head
                    drawLine(TextPrimary, Offset(spineX, shoulderY), Offset(spineX, centerY + 80f), strokeWidth = 8f)
                    drawCircle(TextPrimary, radius = 14f, center = Offset(spineX, headY))
                    drawLine(TextPrimary, Offset(leftShoulderX, shoulderY), Offset(rightShoulderX, shoulderY), strokeWidth = 6f)
                    
                    // Left Arm / Right Arm representing continuous straight extensions
                    drawLine(TextPrimary, Offset(leftShoulderX, shoulderY), Offset(leftHandX, leftHandY), strokeWidth = 5f, cap = StrokeCap.Round)
                    drawLine(TextPrimary, Offset(rightShoulderX, shoulderY), Offset(rightHandX, rightHandY), strokeWidth = 5f, cap = StrokeCap.Round)

                    // Dumbbells in hand
                    drawCircle(Color.DarkGray, radius = 8f, center = Offset(leftHandX, leftHandY))
                    drawCircle(Color.DarkGray, radius = 8f, center = Offset(rightHandX, rightHandY))

                    drawTelemetryText(textMeasurer, "COTOVELO PRONADO", Offset(10f, 10f))
                    drawTelemetryText(textMeasurer, "STIMULUS DELTOIDE: ${(depth*100).toInt()}%", Offset(10f, 32f))
                }

                "BICEPS_CURL" -> {
                    // Rosca direta na polia side cutout
                    // 0 -> arm extended down, 1 -> full contraction
                    val cycle = sin(animationProgress * 2 * PI.toFloat()).coerceIn(-1.0f, 1.0f)
                    val depth = (cycle + 1f) / 2f
                    
                    val shoulderX = centerX - 20f
                    val shoulderY = centerY - 45f
                    val elbowX = shoulderX
                    val elbowY = shoulderY + 50f // pinned elbow
                    
                    val curlAngle = (depth * 135f) * (PI.toFloat() / 180f)
                    val handLength = 45f
                    val handX = elbowX + handLength * sin(curlAngle)
                    val handY = elbowY + handLength * sin(PI.toFloat()/2f - curlAngle)

                    // Biceps muscle belly contraction glow
                    val bicepsColor = lerpColor(Color(0xFF4A4D53), ToxicGreen, depth)
                    drawCircle(bicepsColor, radius = 8f + 8f * depth, center = Offset(shoulderX + 12f * depth, shoulderY + 22f))

                    // Tronk + head outline
                    drawLine(TextPrimary, Offset(shoulderX, shoulderY), Offset(shoulderX, centerY + 80f), strokeWidth = 7f)
                    drawCircle(TextPrimary, radius = 12f, center = Offset(shoulderX, shoulderY - 30f))

                    // Upper arm
                    drawLine(TextPrimary, Offset(shoulderX, shoulderY), Offset(elbowX, elbowY), strokeWidth = 6f)
                    // Forearm
                    drawLine(TextPrimary, Offset(elbowX, elbowY), Offset(handX, handY), strokeWidth = 5f, cap = StrokeCap.Round)

                    // Pulley cable from ground
                    val pulleyAnchorX = centerX + 50f
                    val pulleyAnchorY = centerY + 80f
                    drawLine(TechCyan, Offset(pulleyAnchorX, pulleyAnchorY), Offset(handX, handY), strokeWidth = 1.5f)
                    
                    // Grip bar
                    drawLine(Color.LightGray, Offset(handX - 10f, handY - 10f), Offset(handX + 10f, handY + 10f), strokeWidth = 4f)

                    drawTelemetryText(textMeasurer, "ISOLAMENTO BÍC_P: MAX", Offset(10f, 10f))
                    drawTelemetryText(textMeasurer, "VOLUME BÍCEPS MÁX: ${(depth*100).toInt()}% ESCON", Offset(10f, 32f))
                }

                "TRICEPS_EXTENSION" -> {
                    // Triceps testa side view
                    val cycle = sin(animationProgress * 2 * PI.toFloat()).coerceIn(-1.0f, 1.0f)
                    val depth = (cycle + 1f) / 2f // 1 -> forearm extended up, 0 -> flexed back behind head

                    val benchY = centerY + 40f
                    val anchorBackX = centerX - 70f
                    val hipX = centerX + 80f
                    val shoulderX = centerX - 15f
                    val shoulderY = centerY - 15f

                    // incline upper arm back 15-20 degrees
                    val upperArmAngle = (105f) * (PI.toFloat() / 180f)
                    val elbowLength = 42f
                    val elbowX = shoulderX - elbowLength * sin(upperArmAngle)
                    val elbowY = shoulderY - elbowLength * sin(PI.toFloat()/2f - upperArmAngle)

                    // forearm flexing backwards
                    val forearmAngle = ((105f) - (110f * depth)) * (PI.toFloat() / 180f)
                    val forearmLength = 42f
                    val handX = elbowX + forearmLength * sin(forearmAngle)
                    val handY = elbowY + forearmLength * sin(PI.toFloat()/2f - forearmAngle)

                    // Glow triceps area on the back of upper arm
                    val tricepsColor = lerpColor(Color(0xFF4A4D53), ToxicGreen, 1f - depth)
                    drawCircle(tricepsColor, radius = 6f + 7f * (1f - depth), center = Offset(shoulderX - 15f, shoulderY - 20f))

                    // Bench
                    drawLine(Color(0xFF2D2D35), Offset(anchorBackX, benchY), Offset(hipX, benchY), strokeWidth = 10f, cap = StrokeCap.Round)

                    // Torso layout lying down
                    drawLine(TextPrimary, Offset(anchorBackX + 20f, shoulderY), Offset(shoulderX, shoulderY), strokeWidth = 6f)
                    drawLine(TextPrimary, Offset(shoulderX, shoulderY), Offset(hipX - 10f, benchY - 10f), strokeWidth = 7f)

                    // Upper Arm
                    drawLine(TextPrimary, Offset(shoulderX, shoulderY), Offset(elbowX, elbowY), strokeWidth = 5f, cap = StrokeCap.Round)
                    // Forearm
                    drawLine(TextPrimary, Offset(elbowX, elbowY), Offset(handX, handY), strokeWidth = 5f, cap = StrokeCap.Round)

                    // Dumbbells in hands
                    drawCircle(Color.DarkGray, radius = 10f, center = Offset(handX, handY))

                    drawTelemetryText(textMeasurer, "CONTROLE EXC_NTRICO", Offset(10f, 10f))
                    drawTelemetryText(textMeasurer, "TENSÃO TRÍCEPS: ${((1f - depth)*100).toInt()}%", Offset(10f, 32f))
                }

                "LEG_PRESS" -> {
                    // Leg press 45 lateral outline
                    val cycle = sin(animationProgress * 2 * PI.toFloat()).coerceIn(-1.0f, 1.0f)
                    val depth = (cycle + 1f) / 2f // 1 -> legs fully extended, 0 -> 90deg knee compression

                    val seatAngleRad = 35f * (PI.toFloat() / 180f)
                    val baseSeatX = centerX - 70f
                    val baseSeatY = centerY + 70f

                    val hipX = baseSeatX + 30f
                    val hipY = baseSeatY - 20f

                    // Knees flexing downwards in 45 plane
                    val kneeXStart = hipX + 70f
                    val kneeYStart = hipY - 60f
                    val kneeXEnd = hipX + 35f
                    val kneeYEnd = hipY - 20f
                    val kneeX = kneeXEnd + (kneeXStart - kneeXEnd) * depth
                    val kneeY = kneeYEnd + (kneeYStart - kneeYEnd) * depth

                    // Foot platforms sliding on diagonal axis (45 degrees)
                    val footXStart = hipX + 110f
                    val footYStart = hipY - 110f
                    val footXEnd = hipX + 70f
                    val footYEnd = hipY - 70f
                    val footX = footXEnd + (footXStart - footXEnd) * depth
                    val footY = footYEnd + (footYStart - footYEnd) * depth

                    // Glow quad / hip muscles
                    val quadColor = lerpColor(Color(0xFF4A4D53), ToxicGreen, 1f - depth)
                    drawLine(quadColor, Offset(hipX, hipY), Offset(kneeX, kneeY), strokeWidth = 12f, cap = StrokeCap.Round)

                    // Seat structure
                    drawLine(Color(0xFF2D2D35), Offset(baseSeatX, baseSeatY), Offset(hipX - 15f, hipY - 50f), strokeWidth = 10f, cap = StrokeCap.Round)
                    
                    // Skeleton profile
                    drawLine(TextPrimary, Offset(hipX, hipY), Offset(kneeX, kneeY), strokeWidth = 6f)
                    drawLine(TextPrimary, Offset(kneeX, kneeY), Offset(footX, footY), strokeWidth = 5f)

                    // Diagonal rails
                    drawLine(Color(0xFF1E1E24), Offset(hipX + 10f, hipY - 10f), Offset(hipX + 140f, hipY - 140f), strokeWidth = 4f)
                    // Moving Platform carriage
                    drawLine(ElectricOrange, Offset(footX - 15f, footY + 15f), Offset(footX + 15f, footY - 15f), strokeWidth = 12f)

                    drawTelemetryText(textMeasurer, "ANG COMPRESSÃO: ${(80 + (depth*65)).toInt()}°", Offset(10f, 10f))
                    drawTelemetryText(textMeasurer, "SEGURANÇA LOMBAR: 100%", Offset(10f, 32f))
                }

                else -> {
                    // Default fallback visual representation
                    drawTelemetryText(textMeasurer, "ANÁLISE CINESIOLÓGICA DIRETAR", Offset(10f, 10f))
                }
            }
        }

        // Technical Badge HUD
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(Color(0x880B0B0E), RoundedCornerShape(topStart = 8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    Icons.Default.Info, 
                    contentDescription = null, 
                    tint = TechCyan, 
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "BIOMECÂNICA $viewMode SPEED ${playbackSpeed}x", 
                    color = TextPrimary, 
                    fontSize = 10.sp, 
                    fontFamily = TechMonospace
                )
            }
        }
    }
}

// Helpers for canvas calculations

private fun drawTelemetryText(textMeasurer: TextMeasurer, label: String, origin: Offset) {
    val textStyle = TextStyle(
        color = ToxicGreen,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = TechMonospace
    )
    val textLayoutResult = textMeasurer.measure(
        text = AnnotatedString(label),
        style = textStyle
    )
    // Draw simple background rectangle back of text
    
    // Continue text drawing
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTelemetryText(
    textMeasurer: TextMeasurer, 
    label: String, 
    offset: Offset
) {
    val textStyle = TextStyle(
        color = ToxicGreen,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = TechMonospace
    )
    val textLayoutResult = textMeasurer.measure(
        text = AnnotatedString(label.uppercase()),
        style = textStyle
    )
    
    // Draw solid back banner behind text for extreme contrast
    drawRect(
        color = Color(0xBB0B0B0E),
        topLeft = Offset(offset.x - 4f, offset.y - 2f),
        size = Size(textLayoutResult.size.width.toFloat() + 8f, textLayoutResult.size.height.toFloat() + 4f)
    )
    
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = offset
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGrid(width: Float, height: Float) {
    val color = Color(0xFF1E1E26)
    val spacing = 35f
    // Vertical grid lines
    var x = spacing
    while (x < width) {
        drawLine(color, Offset(x, 0f), Offset(x, height), strokeWidth = 1f)
        x += spacing
    }
    // Horizontal grid lines
    var y = spacing
    while (y < height) {
        drawLine(color, Offset(0f, y), Offset(width, y), strokeWidth = 1f)
        y += spacing
    }
    
    // Draw outer technical borders
    drawRect(
        color = BorderDark,
        topLeft = Offset(0f, 0f),
        size = Size(width, height),
        style = Stroke(width = 2f)
    )
}

// Lerp color helper for contraction glowing
private fun lerpColor(start: Color, end: Color, fraction: Float): Color {
    val clamped = fraction.coerceIn(0f, 1f)
    val r = start.red + (end.red - start.red) * clamped
    val g = start.green + (end.green - start.green) * clamped
    val b = start.blue + (end.blue - start.blue) * clamped
    val a = start.alpha + (end.alpha - start.alpha) * clamped
    return Color(r, g, b, a)
}
