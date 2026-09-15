package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.DefaultSongs
import com.example.model.SongSection
import com.example.ui.components.AudioPlayerBar
import com.example.ui.components.RealtimeChordDisplay
import com.example.ui.components.RealtimeLyricsView
import com.example.viewmodel.MusicPracticeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPracticeScreen(
    viewModel: MusicPracticeViewModel = viewModel()
) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.player.isPlaying.collectAsState()
    val currentPositionSec by viewModel.player.currentPositionSec.collectAsState()
    val durationSec by viewModel.player.durationSec.collectAsState()
    val playbackSpeed by viewModel.player.playbackSpeed.collectAsState()
    val isLooping by viewModel.player.isLoopingSection.collectAsState()
    val isLoadingAudio by viewModel.isLoadingAudio.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    val activeSection by viewModel.activeSection.collectAsState()
    val activeChordInfo by viewModel.activeChordInfo.collectAsState()
    val activeLyricLine by viewModel.activeLyricLine.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showSongMenu by remember { mutableStateOf(false) }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0A0E1A),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Latihan Musik & Kord",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${currentSong.title} • ${currentSong.bpm} BPM",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                },
                actions = {
                    // Song Selector Dropdown
                    Box {
                        IconButton(
                            onClick = { showSongMenu = true },
                            modifier = Modifier.testTag("song_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LibraryMusic,
                                contentDescription = "Pilih Lagu",
                                tint = Color(0xFF38BDF8)
                            )
                        }
                        DropdownMenu(
                            expanded = showSongMenu,
                            onDismissRequest = { showSongMenu = false },
                            modifier = Modifier.background(Color(0xFF1E293B))
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            "Sempurna (Acoustic Jam)",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            "Kord: G - Em - C - D • 85 BPM",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.selectPresetSong(DefaultSongs.getPopBalladSong())
                                    showSongMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            "Kemesraan (Folk Ballad)",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            "Kord: C - F - G - Am • 80 BPM",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.selectPresetSong(DefaultSongs.getBluesRockSong())
                                    showSongMenu = false
                                }
                            )
                        }
                    }

                    // Reset Button
                    IconButton(
                        onClick = { viewModel.resetToDefault() },
                        modifier = Modifier.testTag("reset_song_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Lagu",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0E1A)
                )
            )
        },
        bottomBar = {
            AudioPlayerBar(
                isPlaying = isPlaying,
                currentPositionSec = currentPositionSec,
                durationSec = durationSec,
                playbackSpeed = playbackSpeed,
                isLooping = isLooping,
                songTitle = currentSong.title,
                audioFileName = currentSong.audioFileName,
                isLoading = isLoadingAudio,
                onTogglePlayPause = { viewModel.player.togglePlayPause() },
                onSeek = { viewModel.player.seekTo(it) },
                onSkipBackward = { viewModel.player.skipBy(-5f) },
                onSkipForward = { viewModel.player.skipBy(5f) },
                onSpeedChange = { viewModel.player.setPlaybackSpeed(it) },
                onAudioFilePicked = { uri -> viewModel.onAudioFileSelected(uri) },
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section Status Banner
            item {
                ActiveSectionHeader(
                    activeSection = activeSection,
                    currentPositionSec = currentPositionSec,
                    bpm = currentSong.bpm,
                    isPlaying = isPlaying
                )
            }

            // 1. Realtime Chord Display (Beat Pulse + Current Chord + Next Chord + Finger String Guide)
            item {
                RealtimeChordDisplay(
                    activeSection = activeSection,
                    activeChordInfo = activeChordInfo,
                    bpm = currentSong.bpm,
                    isPlaying = isPlaying,
                    onChordClick = { }
                )
            }

            // 2. Realtime Synchronized Lyrics & Chords View
            item {
                RealtimeLyricsView(
                    lyrics = currentSong.lyrics,
                    activeLyricLine = activeLyricLine,
                    currentAudioTimeSec = currentPositionSec,
                    onLyricClick = { line ->
                        viewModel.jumpToLyric(line)
                    }
                )
            }
        }
    }
}

/**
 * Clean status header indicating current song part, timing, and tempo.
 */
@Composable
private fun ActiveSectionHeader(
    activeSection: SongSection?,
    currentPositionSec: Float,
    bpm: Int,
    isPlaying: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(activeSection?.colorHex ?: 0xFFF59E0B))
                )
                Column {
                    Text(
                        text = activeSection?.name ?: "Intro Latihan",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Waktu: ${formatTime(currentPositionSec)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "$bpm BPM • 4/4 Birama",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

private fun formatTime(sec: Float): String {
    val totalSec = sec.coerceAtLeast(0f).toInt()
    val m = totalSec / 60
    val s = totalSec % 60
    return String.format("%02d:%02d", m, s)
}
