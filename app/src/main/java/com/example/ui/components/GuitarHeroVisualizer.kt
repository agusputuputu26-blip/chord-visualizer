package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GuitarHeroLaneColors
import com.example.model.GuitarHeroNote
import com.example.model.GuitarHeroSectionMarker
import com.example.model.PracticeSong
import kotlin.math.sin

@Composable
fun GuitarHeroVisualizer(
    notes: List<GuitarHeroNote>,
    sectionMarkers: List<GuitarHeroSectionMarker>,
    currentAudioTimeSec: Float,
    bpm: Int,
    isPlaying: Boolean,
    onExportVideoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Lookahead time in seconds (notes take this duration to travel from top to hit line)
    val lookAheadSec = 2.0f

    // Interactive score and combo tracking
    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var multiplier by remember { mutableIntStateOf(1) }
    var lastProcessedNoteId by remember { mutableStateOf<String?>(null) }

    // Hit particle flash states per lane (0..4)
    val laneHitFlashes = remember { mutableStateMapOf<Int, Long>() }

    // Check notes crossing hit line in real-time
    LaunchedEffect(currentAudioTimeSec) {
        val hitWindow = 0.08f
        notes.forEach { note ->
            val diff = note.timestampSec - currentAudioTimeSec
            if (diff in -hitWindow..hitWindow && note.id != lastProcessedNoteId) {
                lastProcessedNoteId = note.id
                laneHitFlashes[note.lane] = System.currentTimeMillis()
                streak += 1
                multiplier = when {
                    streak >= 30 -> 4
                    streak >= 20 -> 3
                    streak >= 10 -> 2
                    else -> 1
                }
                score += (50 * multiplier)
            }
        }
    }

    // Active Section finder
    val currentMarker = sectionMarkers.lastOrNull { it.timestampSec <= currentAudioTimeSec }
        ?: sectionMarkers.firstOrNull()
    val nextMarker = sectionMarkers.firstOrNull { it.timestampSec > currentAudioTimeSec }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("guitar_hero_visualizer_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Bar: Title, Active Section, Export Video Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Visualizer Guitar Hero",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = currentMarker?.let { "Bagian: ${it.name}" } ?: "Bersiap...",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(currentMarker?.colorHex ?: 0xFF38BDF8),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }
                }

                // Video Export Button (Prompt Feature 3)
                FilledTonalButton(
                    onClick = onExportVideoClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color(0xFFF59E0B)
                    ),
                    modifier = Modifier.testTag("export_video_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Export Video",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Export Video",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // HUD Stats Bar (Score, Multiplier, Rock Meter)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF131B2E))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Multiplier
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "COMBO",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (multiplier) {
                                    4 -> Color(0xFF8B5CF6)
                                    3 -> Color(0xFF3B82F6)
                                    2 -> Color(0xFF10B981)
                                    else -> Color(0xFFF59E0B)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${multiplier}X",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                // Score Counter
                Text(
                    text = String.format("SKOR: %,d", score),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF8FAFC)
                )

                // Upcoming Section Banner
                if (nextMarker != null) {
                    val inSec = (nextMarker.timestampSec - currentAudioTimeSec).toInt().coerceAtLeast(0)
                    Text(
                        text = "Berikutnya: ${nextMarker.name} (${inSec}s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(nextMarker.colorHex),
                        fontSize = 10.sp
                    )
                } else {
                    Text(
                        text = "Bagian Akhir",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }
            }

            // The Guitar Hero 3D Perspective Highway Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF060911))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    // Highway Geometry
                    val topWidth = canvasWidth * 0.44f
                    val bottomWidth = canvasWidth * 0.94f

                    val topX = (canvasWidth - topWidth) / 2f
                    val bottomX = (canvasWidth - bottomWidth) / 2f

                    val highwayTopY = 20f
                    val hitLineY = canvasHeight - 48f
                    val highwayBottomY = canvasHeight - 8f

                    // Draw Highway Surface (Trapezoid) with metallic gradient
                    val highwayPath = Path().apply {
                        moveTo(topX, highwayTopY)
                        lineTo(topX + topWidth, highwayTopY)
                        lineTo(bottomX + bottomWidth, highwayBottomY)
                        lineTo(bottomX, highwayBottomY)
                        close()
                    }

                    drawPath(
                        path = highwayPath,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFF0F172A),
                                Color(0xFF111827),
                                Color(0xFF1E293B)
                            )
                        )
                    )

                    // Draw perspective lane dividers (5 lanes = 6 divider lines)
                    for (i in 0..5) {
                        val fraction = i / 5.0f
                        val lineTopX = topX + (topWidth * fraction)
                        val lineBottomX = bottomX + (bottomWidth * fraction)

                        val isOuter = (i == 0 || i == 5)
                        drawLine(
                            color = if (isOuter) Color(0xFF38BDF8) else Color(0x4494A3B8),
                            start = Offset(lineTopX, highwayTopY),
                            end = Offset(lineBottomX, highwayBottomY),
                            strokeWidth = if (isOuter) 2.5f else 1.2f
                        )
                    }

                    // Draw Scrolling Beat Bars along the highway
                    val secPerBeat = 60.0f / bpm
                    val beatOffset = (currentAudioTimeSec % secPerBeat) / secPerBeat
                    for (b in 0..6) {
                        val barProgress = ((b.toFloat() - beatOffset) / 6.0f).coerceIn(0f, 1f)
                        val y = highwayTopY + (hitLineY - highwayTopY) * barProgress
                        val currentWidth = topWidth + (bottomWidth - topWidth) * barProgress
                        val currentLeft = (canvasWidth - currentWidth) / 2f

                        drawLine(
                            color = Color(0x3364748B),
                            start = Offset(currentLeft, y),
                            end = Offset(currentLeft + currentWidth, y),
                            strokeWidth = 1.2f
                        )
                    }

                    // Draw Section Markers flowing down the highway (Prompt Feature 2)
                    sectionMarkers.forEach { marker ->
                        val timeDelta = marker.timestampSec - currentAudioTimeSec
                        if (timeDelta in -0.5f..lookAheadSec) {
                            val progress = (1.0f - (timeDelta / lookAheadSec)).coerceIn(0f, 1f)
                            val y = highwayTopY + (hitLineY - highwayTopY) * progress
                            val currentWidth = topWidth + (bottomWidth - topWidth) * progress
                            val currentLeft = (canvasWidth - currentWidth) / 2f

                            // Draw glowing section marker bar across highway
                            drawRoundRect(
                                color = Color(marker.colorHex).copy(alpha = 0.75f),
                                topLeft = Offset(currentLeft + 4f, y - 8f),
                                size = Size(currentWidth - 8f, 16f),
                                cornerRadius = CornerRadius(8f, 8f)
                            )
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(currentLeft + 4f, y - 8f),
                                size = Size(currentWidth - 8f, 16f),
                                style = Stroke(width = 1.5f),
                                cornerRadius = CornerRadius(8f, 8f)
                            )
                        }
                    }

                    // Draw Notes / Chord Blocks sliding down (Prompt Feature 1)
                    val currentTime = System.currentTimeMillis()
                    val visibleNotes = notes.filter { note ->
                        val timeDelta = note.timestampSec - currentAudioTimeSec
                        timeDelta in -0.2f..lookAheadSec
                    }

                    // Render sustain connections / chords if any
                    visibleNotes.forEach { note ->
                        val timeDelta = note.timestampSec - currentAudioTimeSec
                        val progress = (1.0f - (timeDelta / lookAheadSec)).coerceIn(0f, 1.05f)

                        val y = highwayTopY + (hitLineY - highwayTopY) * progress
                        val currentWidth = topWidth + (bottomWidth - topWidth) * progress
                        val currentLeft = (canvasWidth - currentWidth) / 2f
                        val laneWidth = currentWidth / 5.0f

                        val noteCenterX = currentLeft + (laneWidth * (note.lane + 0.5f))
                        val noteRadius = 7f + (progress * 11f) // grows as it gets closer

                        val noteColor = GuitarHeroLaneColors.getColor(note.lane)
                        val glowColor = GuitarHeroLaneColors.getGlowColor(note.lane)

                        // Outer Glow
                        drawCircle(
                            color = glowColor,
                            radius = noteRadius * 1.5f,
                            center = Offset(noteCenterX, y)
                        )

                        // 3D Gem / Puck Note
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(Color.White, noteColor),
                                center = Offset(noteCenterX - (noteRadius * 0.3f), y - (noteRadius * 0.3f)),
                                radius = noteRadius
                            ),
                            radius = noteRadius,
                            center = Offset(noteCenterX, y)
                        )

                        // Ring border
                        drawCircle(
                            color = Color.White,
                            radius = noteRadius,
                            center = Offset(noteCenterX, y),
                            style = Stroke(width = 2f)
                        )

                        // Center gem dot
                        drawCircle(
                            color = Color(0xFF0F172A),
                            radius = noteRadius * 0.35f,
                            center = Offset(noteCenterX, y)
                        )
                    }

                    // Draw Hit Line (Garis Sasaran) at bottom
                    val hitLineLeft = bottomX
                    val hitLineWidth = bottomWidth

                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(Color.Transparent, Color(0xFFF59E0B), Color(0xFFEF4444), Color.Transparent)
                        ),
                        start = Offset(hitLineLeft, hitLineY),
                        end = Offset(hitLineLeft + hitLineWidth, hitLineY),
                        strokeWidth = 3.5f
                    )

                    // Draw 5 Receptor Target Rings at hit line
                    val targetLaneWidth = hitLineWidth / 5.0f
                    for (lane in 0..4) {
                        val targetCenterX = hitLineLeft + (targetLaneWidth * (lane + 0.5f))
                        val targetRadius = 18f
                        val baseColor = GuitarHeroLaneColors.getColor(lane)

                        val flashTime = laneHitFlashes[lane] ?: 0L
                        val isHitActive = (currentTime - flashTime) < 180

                        if (isHitActive) {
                            // Flash burst particle halo
                            drawCircle(
                                color = Color.White,
                                radius = targetRadius * 1.8f,
                                center = Offset(targetCenterX, hitLineY)
                            )
                            drawCircle(
                                color = baseColor,
                                radius = targetRadius * 1.5f,
                                center = Offset(targetCenterX, hitLineY)
                            )
                        }

                        // Target Receptor Ring
                        drawCircle(
                            color = if (isHitActive) Color.White else baseColor,
                            radius = targetRadius,
                            center = Offset(targetCenterX, hitLineY),
                            style = Stroke(width = if (isHitActive) 4f else 2.5f)
                        )

                        // Inner dark core
                        drawCircle(
                            color = if (isHitActive) baseColor else Color(0xFF090D16),
                            radius = targetRadius * 0.65f,
                            center = Offset(targetCenterX, hitLineY)
                        )
                    }
                }
            }

            // Interactive Fret Touch Buttons (Lanes 1 to 5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (lane in 0..4) {
                    val color = GuitarHeroLaneColors.getColor(lane)
                    val laneName = when (lane) {
                        0 -> "HIJAU"
                        1 -> "MERAH"
                        2 -> "KUNING"
                        3 -> "BIRU"
                        else -> "JINGGA"
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(color, color.copy(alpha = 0.7f))
                                )
                            )
                            .clickable {
                                laneHitFlashes[lane] = System.currentTimeMillis()
                                score += 25
                            }
                            .testTag("fret_button_$lane"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = laneName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
