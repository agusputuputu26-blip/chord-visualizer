package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RadioWave
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChordItem
import com.example.model.GuitarChord
import com.example.model.LyricLine
import com.example.model.SongSection
import com.example.viewmodel.MusicPracticeViewModel

@Composable
fun RealtimeChordDisplay(
    activeSection: SongSection?,
    activeChordInfo: MusicPracticeViewModel.ActiveChordInfo?,
    bpm: Int,
    isPlaying: Boolean,
    onChordClick: (ChordItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activeChord = activeChordInfo?.chord
    val activeFingering = activeChord?.fingering
    val beatNumber = activeChordInfo?.beatNumber ?: 1
    val chordProgress = activeChordInfo?.chordProgress ?: 0f

    // Pulse transition for the active beat
    val infiniteTransition = rememberInfiniteTransition(label = "beat_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isPlaying) 1.15f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (60000 / bpm).coerceIn(200, 1000), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("realtime_chord_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFF59E0B), Color(0xFF38BDF8))
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Beat Indicators (1, 2, 3, 4) in sync with audio beat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) Color(0xFF10B981) else Color(0xFF94A3B8))
                    )
                    Text(
                        text = if (isPlaying) "SINKRON AUDIO (100%)" else "AUDIO JEDA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isPlaying) Color(0xFF10B981) else Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                }

                // 4-Beat Meter
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ketukan: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                    for (beat in 1..4) {
                        val isCurrentBeat = beat == beatNumber && isPlaying
                        val beatColor = when {
                            isCurrentBeat -> Color(0xFFF59E0B)
                            beat < beatNumber -> Color(0xFF38BDF8).copy(alpha = 0.6f)
                            else -> Color(0xFF334155)
                        }

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .scale(if (isCurrentBeat) pulseScale else 1.0f)
                                .clip(CircleShape)
                                .background(beatColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$beat",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = if (isCurrentBeat) Color(0xFF0F172A) else Color.White
                            )
                        }
                    }
                }
            }

            // Big Prominent Current Chord Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Main Chord Card
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "KORD SEKARANG",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFF59E0B),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    AnimatedContent(
                        targetState = activeChord?.name ?: "-",
                        transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
                        label = "chord_name_anim"
                    ) { chordName ->
                        Text(
                            text = chordName,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFBBF24),
                            fontFamily = FontFamily.SansSerif,
                            modifier = Modifier.testTag("active_chord_text")
                        )
                    }

                    Text(
                        text = "${activeSection?.name ?: "Intro"} • 4/4 Birama",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                // Next Chord Preview Card
                activeChordInfo?.nextChord?.let { next ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Berikutnya",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = next.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${activeChordInfo.beatsRemainingInChord} ketukan lagi",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Chord measure duration progress bar
            LinearProgressIndicator(
                progress = { chordProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = Color(0xFFF59E0B),
                trackColor = Color(0xFF1E293B)
            )

            // Finger positions & String guide for the active chord
            if (activeFingering != null) {
                GuitarStringFretboardBar(
                    chord = activeFingering,
                    activeBeat = beatNumber
                )
            }

            // Progression Ribbon for current section
            if (activeSection != null && activeSection.chords.isNotEmpty()) {
                ChordProgressionRibbon(
                    chords = activeSection.chords,
                    activeIndex = activeChordInfo?.chordIndex ?: 0,
                    onChordClick = onChordClick
                )
            }
        }
    }
}

/**
 * Clean, modern 6-string guitar finger position bar:
 * Shows strings [Low E, A, D, G, B, High e] with fret numbers and finger recommendations.
 */
@Composable
private fun GuitarStringFretboardBar(
    chord: GuitarChord,
    activeBeat: Int
) {
    val stringNames = listOf("E", "A", "D", "G", "B", "e")

    Surface(
        color = Color(0xFF0D1322),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Posisi Jari & Senar Gitar (${chord.chordName})",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Fret Dasar: ${chord.baseFret}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                chord.frets.forEachIndexed { idx, fret ->
                    val stringName = stringNames.getOrElse(idx) { "?" }
                    val finger = chord.fingers.getOrElse(idx) { 0 }
                    val isMuted = fret < 0
                    val isOpen = fret == 0

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // String name badge
                        Text(
                            text = stringName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )

                        // Fret badge
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isMuted -> Color(0xFF334155)
                                        isOpen -> Color(0xFF059669)
                                        else -> Color(0xFFF59E0B)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when {
                                    isMuted -> "X"
                                    isOpen -> "0"
                                    else -> "$fret"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = if (isMuted) Color(0xFF94A3B8) else Color(0xFF0F172A)
                            )
                        }

                        // Finger recommendation
                        Text(
                            text = if (finger > 0) "Jari $finger" else if (isOpen) "Buka" else "Mati",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8),
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Horizontal ribbon showing the flow of chords in the section.
 */
@Composable
private fun ChordProgressionRibbon(
    chords: List<ChordItem>,
    activeIndex: Int,
    onChordClick: (ChordItem) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        chords.forEachIndexed { index, chordItem ->
            val isActive = index == activeIndex

            val bg = if (isActive) Color(0xFFF59E0B) else Color(0xFF1E293B)
            val textColor = if (isActive) Color(0xFF0F172A) else Color.White
            val border = if (isActive) Color(0xFFFDE68A) else Color(0xFF334155)

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(bg)
                    .border(1.dp, border, RoundedCornerShape(12.dp))
                    .clickable { onChordClick(chordItem) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = chordItem.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold,
                        color = textColor
                    )
                    if (isActive) {
                        Text(
                            text = "•",
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

/**
 * Real-time Synchronized Lyrics & Chords scrolling view:
 * Displays chords above the words and scrolls automatically as audio progresses.
 */
@Composable
fun RealtimeLyricsView(
    lyrics: List<LyricLine>,
    activeLyricLine: LyricLine?,
    currentAudioTimeSec: Float,
    onLyricClick: (LyricLine) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll list to active lyric line
    LaunchedEffect(activeLyricLine?.id) {
        if (activeLyricLine != null) {
            val idx = lyrics.indexOfFirst { it.id == activeLyricLine.id }
            if (idx >= 0) {
                listState.animateScrollToItem(idx)
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("realtime_lyrics_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101626)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
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
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Lirik & Kord Berjalan Waktu Nyata",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Surface(
                    color = Color(0xFF0F2A3F),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Karaoke Kord",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (lyrics.isEmpty()) {
                Text(
                    text = "Lirik untuk lagu ini belum tersedia.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(lyrics, key = { _, item -> item.id }) { _, line ->
                        val isActive = line.id == activeLyricLine?.id

                        val cardBg by animateColorAsState(
                            targetValue = if (isActive) Color(0xFF1E293B) else Color.Transparent,
                            label = "lyric_bg"
                        )
                        val borderColor = if (isActive) Color(0xFFF59E0B).copy(alpha = 0.7f) else Color.Transparent

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = cardBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLyricClick(line) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Display chords aligned above words
                                if (line.chordWords.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        line.chordWords.forEach { cw ->
                                            val isWordActive = isActive &&
                                                    currentAudioTimeSec >= cw.timestampSec

                                            Surface(
                                                color = if (isWordActive) Color(0xFFF59E0B) else Color(0xFF334155),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = cw.chord,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (isWordActive) Color(0xFF0F172A) else Color(0xFF38BDF8),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                // Lyrics line text
                                Text(
                                    text = line.text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isActive) Color.White else Color(0xFF94A3B8),
                                    fontSize = if (isActive) 16.sp else 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
