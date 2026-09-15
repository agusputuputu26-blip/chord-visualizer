package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChordItem
import com.example.model.GuitarChord
import com.example.model.SongSection
import com.example.model.TabMeasure
import com.example.viewmodel.MusicPracticeViewModel

@Composable
fun ChordTabDisplay(
    activeSection: SongSection?,
    activeChordInfo: MusicPracticeViewModel.ActiveChordInfo?,
    onChordClick: (ChordItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("chord_tab_display_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF131B2E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Section name + active chord summary
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
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(activeSection?.colorHex ?: 0xFFF59E0B))
                    )
                    Text(
                        text = activeSection?.let { "Kord & Tab: ${it.name}" } ?: "Kord & Tab Latihan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (activeChordInfo != null) {
                    // Beat indicator dots
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Ketukan: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray
                        )
                        for (beat in 1..4) {
                            val isActiveBeat = beat == activeChordInfo.beatNumber
                            Box(
                                modifier = Modifier
                                    .size(if (isActiveBeat) 10.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isActiveBeat) Color(0xFFF59E0B) else Color(0xFF475569)
                                    )
                            )
                        }
                    }
                }
            }

            if (activeSection == null || activeSection.chords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada kord untuk bagian ini. Silakan atur di editor.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )
                }
            } else {
                // Interactive Chord Progression Ribbon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    activeSection.chords.forEachIndexed { index, chordItem ->
                        val isCurrent = activeChordInfo?.chordIndex == index
                        ChordChip(
                            chord = chordItem,
                            isSelected = isCurrent,
                            progress = if (isCurrent) activeChordInfo.chordProgress else 0f,
                            onClick = { onChordClick(chordItem) }
                        )
                    }
                }

                // Middle section: Guitar chord diagram alongside interactive Tablature
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Guitar Chord Fretboard Diagram Card
                    val currentFingering = activeChordInfo?.chord?.fingering
                        ?: activeSection.chords.firstOrNull()?.fingering
                    val chordDisplayName = activeChordInfo?.chord?.name
                        ?: activeSection.chords.firstOrNull()?.name ?: "G"

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(110.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = chordDisplayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFF59E0B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (currentFingering != null) {
                            GuitarFretboardDiagram(
                                chord = currentFingering,
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(115.dp)
                            )
                        } else {
                            Text(
                                text = "Kord Terbuka",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }

                    // Interactive Tablature View
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0B0F19))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tabulasi Gitar (6-String)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF38BDF8)
                            )
                            Text(
                                text = "Sync Real-Time",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF10B981)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val activeTabMeasure = activeSection.tabNotation.getOrNull(
                            activeChordInfo?.chordIndex ?: 0
                        ) ?: activeSection.tabNotation.firstOrNull()

                        InteractiveGuitarTab(
                            measure = activeTabMeasure,
                            beatProgress = activeChordInfo?.chordProgress ?: 0f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChordChip(
    chord: ChordItem,
    isSelected: Boolean,
    progress: Float,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFF59E0B) else Color(0xFF334155),
        label = "chordBorder"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF2E2413) else Color(0xFF1E293B),
        label = "chordBg"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = chord.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFFF59E0B) else Color.White
            )
            // Progress micro-bar
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF334155))
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(3.dp)
                            .background(Color(0xFFF59E0B))
                    )
                }
            }
        }
    }
}

/**
 * Canvas-based guitar chord diagram renderer.
 * Visualizes 6 strings and 5 frets with nut, string markers (X/O), and finger dots.
 */
@Composable
fun GuitarFretboardDiagram(
    chord: GuitarChord,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val topPadding = 20f
        val bottomPadding = 12f
        val horizontalPadding = 14f

        val fretAreaHeight = height - topPadding - bottomPadding
        val fretAreaWidth = width - (horizontalPadding * 2)

        val numFrets = 4
        val numStrings = 6

        val stringSpacing = fretAreaWidth / (numStrings - 1)
        val fretSpacing = fretAreaHeight / numFrets

        // Draw Nut (fret 0 line)
        val isNut = chord.baseFret == 1
        drawLine(
            color = if (isNut) Color(0xFFE2E8F0) else Color(0xFF64748B),
            start = Offset(horizontalPadding, topPadding),
            end = Offset(width - horizontalPadding, topPadding),
            strokeWidth = if (isNut) 5f else 2f
        )

        // Draw Fret wires (horizontal)
        for (f in 1..numFrets) {
            val y = topPadding + (f * fretSpacing)
            drawLine(
                color = Color(0xFF475569),
                start = Offset(horizontalPadding, y),
                end = Offset(width - horizontalPadding, y),
                strokeWidth = 2f
            )
        }

        // Draw Strings (vertical): 6 strings from low E (left) to high e (right)
        for (s in 0 until numStrings) {
            val x = horizontalPadding + (s * stringSpacing)
            // Low strings are thicker
            val stringThickness = when (s) {
                0, 1 -> 2.5f
                2, 3 -> 2.0f
                else -> 1.5f
            }
            drawLine(
                color = Color(0xFF94A3B8),
                start = Offset(x, topPadding),
                end = Offset(x, topPadding + fretAreaHeight),
                strokeWidth = stringThickness
            )

            // Draw string status at top (X = muted, O = open)
            val fret = chord.frets.getOrElse(s) { 0 }
            if (fret == -1) {
                // Draw 'X'
                val cx = x
                val cy = topPadding - 10f
                val crossSize = 4f
                drawLine(
                    color = Color(0xFFEF4444),
                    start = Offset(cx - crossSize, cy - crossSize),
                    end = Offset(cx + crossSize, cy + crossSize),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0xFFEF4444),
                    start = Offset(cx + crossSize, cy - crossSize),
                    end = Offset(cx - crossSize, cy + crossSize),
                    strokeWidth = 2f
                )
            } else if (fret == 0) {
                // Draw 'O'
                drawCircle(
                    color = Color(0xFF10B981),
                    radius = 4f,
                    center = Offset(x, topPadding - 10f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                )
            }
        }

        // Draw Finger dots on fretted positions
        for (s in 0 until numStrings) {
            val fret = chord.frets.getOrElse(s) { 0 }
            if (fret > 0) {
                val relativeFret = fret - (chord.baseFret - 1)
                if (relativeFret in 1..numFrets) {
                    val x = horizontalPadding + (s * stringSpacing)
                    val y = topPadding + (relativeFret - 0.5f) * fretSpacing
                    // Glow background
                    drawCircle(
                        color = Color(0x66F59E0B),
                        radius = 8f,
                        center = Offset(x, y)
                    )
                    // Solid dot
                    drawCircle(
                        color = Color(0xFFF59E0B),
                        radius = 6f,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}

/**
 * Interactive Guitar Tablature View:
 * Draws 6 horizontal string lines (e, B, G, D, A, E), fret numbers, and a moving real-time sync playhead.
 */
@Composable
fun InteractiveGuitarTab(
    measure: TabMeasure?,
    beatProgress: Float,
    modifier: Modifier = Modifier
) {
    val stringNames = listOf("e", "B", "G", "D", "A", "E")

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val leftHeaderWidth = 24f
        val tabAreaWidth = width - leftHeaderWidth - 10f
        val lineSpacing = height / 7f

        // Draw 6 guitar lines
        for (i in 0 until 6) {
            val y = lineSpacing * (i + 1)
            drawLine(
                color = Color(0xFF334155),
                start = Offset(leftHeaderWidth, y),
                end = Offset(width - 10f, y),
                strokeWidth = 1.5f
            )
        }

        // Draw notes from active TabMeasure
        if (measure != null) {
            for (col in measure.notes) {
                val normX = (col.beatOffset / 4.0f).coerceIn(0f, 1f)
                val noteX = leftHeaderWidth + (normX * tabAreaWidth) + 16f

                for ((strIdx, fretNum) in col.strings) {
                    if (strIdx in 0..5) {
                        val noteY = lineSpacing * (strIdx + 1)
                        // Background mask for fret number
                        drawCircle(
                            color = Color(0xFF0B0F19),
                            radius = 7f,
                            center = Offset(noteX, noteY)
                        )
                        // Fret number circle badge
                        drawCircle(
                            color = Color(0xFF0284C7),
                            radius = 6f,
                            center = Offset(noteX, noteY)
                        )
                    }
                }
            }
        }

        // Real-Time Sync Playhead line moving across the measure
        val playheadX = leftHeaderWidth + (beatProgress.coerceIn(0f, 1f) * tabAreaWidth)
        drawLine(
            brush = Brush.verticalGradient(
                listOf(Color(0xFFF59E0B), Color(0xFFEF4444))
            ),
            start = Offset(playheadX, lineSpacing * 0.8f),
            end = Offset(playheadX, lineSpacing * 6.2f),
            strokeWidth = 3f
        )
        drawCircle(
            color = Color(0xFFF59E0B),
            radius = 4f,
            center = Offset(playheadX, lineSpacing * 0.8f)
        )
    }
}
