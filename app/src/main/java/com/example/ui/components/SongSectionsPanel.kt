package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Timeline
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SongSection

@Composable
fun SongSectionsPanel(
    sections: List<SongSection>,
    activeSection: SongSection?,
    currentPositionSec: Float,
    isLoopingCurrentSection: Boolean,
    onSectionClick: (SongSection) -> Unit,
    onLoopClick: (SongSection) -> Unit,
    onEditClick: (SongSection) -> Unit,
    onAddSectionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll to keep active section in view
    LaunchedEffect(activeSection?.id) {
        val index = sections.indexOfFirst { it.id == activeSection?.id }
        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("song_sections_panel"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF131B2E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Struktur Lagu (Patokan Latihan)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                FilledTonalButton(
                    onClick = onAddSectionClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color(0xFF38BDF8)
                    ),
                    modifier = Modifier.testTag("add_section_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Bagian",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tambah",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // Quick horizontal breadcrumb trail of song structure (Intro -> Verse 1 -> Chorus -> ...)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sections.forEachIndexed { idx, sec ->
                    val isActive = sec.id == activeSection?.id
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (isActive) Color(sec.colorHex) else Color(sec.colorHex).copy(alpha = 0.3f)
                            )
                    )
                }
            }

            // List of sections
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sections.forEach { section ->
                    val isActive = section.id == activeSection?.id
                    val isLooping = isActive && isLoopingCurrentSection

                    val sectionProgress = if (isActive) {
                        ((currentPositionSec - section.startTimeSec) / section.durationSec).coerceIn(0f, 1f)
                    } else 0f

                    SectionItemCard(
                        section = section,
                        isActive = isActive,
                        isLooping = isLooping,
                        progress = sectionProgress,
                        onClick = { onSectionClick(section) },
                        onLoopClick = { onLoopClick(section) },
                        onEditClick = { onEditClick(section) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionItemCard(
    section: SongSection,
    isActive: Boolean,
    isLooping: Boolean,
    progress: Float,
    onClick: () -> Unit,
    onLoopClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isActive) Color(section.colorHex) else Color(0xFF1E293B),
        label = "borderAnim"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFF1E293B) else Color(0xFF0F172A),
        label = "bgAnim"
    )

    // Pulsing animation for active section indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(if (isActive) 1.8.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("section_item_${section.id}"),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Section Title + Timing Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Color pillar
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height(28.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(section.colorHex))
                    )

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = section.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) Color.White else Color(0xFFE2E8F0)
                            )
                            if (isActive) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(section.colorHex).copy(alpha = pulseAlpha * 0.25f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "SEDANG AKTIF",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 9.sp,
                                        color = Color(section.colorHex)
                                    )
                                }
                            }
                        }

                        // Timestamps format: mm:ss - mm:ss (Durasi: X detik)
                        val startStr = formatTime(section.startTimeSec)
                        val endStr = formatTime(section.endTimeSec)
                        Text(
                            text = "$startStr - $endStr  •  ${section.durationSec.toInt()}s  •  ${section.chords.size} kord",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Action icons: Loop, Edit
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Loop button
                    IconButton(
                        onClick = onLoopClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Loop Bagian",
                            tint = if (isLooping) Color(0xFFF59E0B) else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Edit button
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Bagian",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Real-Time Progress Bar within active section
            if (isActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Color(0xFF0B0F19))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(3.dp)
                            .background(Color(section.colorHex))
                    )
                }
            }
        }
    }
}

private fun formatTime(seconds: Float): String {
    val totalSec = seconds.toInt().coerceAtLeast(0)
    val mins = totalSec / 60
    val secs = totalSec % 60
    return String.format("%02d:%02d", mins, secs)
}
