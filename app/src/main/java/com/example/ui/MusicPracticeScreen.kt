package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
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
import com.example.ui.components.ChordTabDisplay
import com.example.ui.components.SectionEditorDialog
import com.example.ui.components.SongSectionsPanel
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

    val snackbarHostState = remember { SnackbarHostState() }
    var showSongMenu by remember { mutableStateOf(false) }

    // Editor state
    var isEditorOpen by remember { mutableStateOf(false) }
    var sectionToEdit by remember { mutableStateOf<SongSection?>(null) }

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
                                .size(34.dp)
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
                                text = "Latihan Musik Interaktif",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${currentSong.bpm} BPM  •  ${currentSong.sections.size} Bagian",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                },
                actions = {
                    // Song Preset Selector
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
                            onDismissRequest = { showSongMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Acoustic Pop Jam (G - Em - C - D)") },
                                onClick = {
                                    viewModel.selectPresetSong(DefaultSongs.getPopBalladSong())
                                    showSongMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Midnight Blues Jam (12-Bar in A)") },
                                onClick = {
                                    viewModel.selectPresetSong(DefaultSongs.getBluesRockSong())
                                    showSongMenu = false
                                }
                            )
                        }
                    }

                    // Reset button
                    IconButton(onClick = { viewModel.resetToDefault() }) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset",
                            tint = Color(0xFF64748B)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Active Section Header Banner
            item {
                ActiveSectionBanner(
                    activeSection = activeSection,
                    currentPositionSec = currentPositionSec,
                    isPlaying = isPlaying
                )
            }

            // Interactive Tab / Chord Display Card
            item {
                ChordTabDisplay(
                    activeSection = activeSection,
                    activeChordInfo = activeChordInfo,
                    onChordClick = { chordItem ->
                        // Quick feedback
                    }
                )
            }

            // Song Structure (Patokan Latihan) Panel
            item {
                SongSectionsPanel(
                    sections = currentSong.sections,
                    activeSection = activeSection,
                    currentPositionSec = currentPositionSec,
                    isLoopingCurrentSection = isLooping,
                    onSectionClick = { section ->
                        viewModel.jumpToSection(section)
                    },
                    onLoopClick = { section ->
                        viewModel.toggleLoopSection(section)
                    },
                    onEditClick = { section ->
                        sectionToEdit = section
                        isEditorOpen = true
                    },
                    onAddSectionClick = {
                        sectionToEdit = null
                        isEditorOpen = true
                    }
                )
            }
        }
    }

    // Section Editor Dialog
    if (isEditorOpen) {
        SectionEditorDialog(
            sectionToEdit = sectionToEdit,
            currentAudioTimeSec = currentPositionSec,
            onDismiss = { isEditorOpen = false },
            onSaveNewSection = { name, startSec, chords ->
                viewModel.addSection(name, startSec, chords)
            },
            onUpdateSection = { updated ->
                viewModel.updateSection(updated)
            },
            onDeleteSection = { id ->
                viewModel.deleteSection(id)
            }
        )
    }
}

@Composable
private fun ActiveSectionBanner(
    activeSection: SongSection?,
    currentPositionSec: Float,
    isPlaying: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_section_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF131B2E)
        )
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
                        .background(
                            if (activeSection != null) Color(activeSection.colorHex) else Color.Gray
                        )
                )
                Column {
                    Text(
                        text = activeSection?.name ?: "Menunggu Pemutaran...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (activeSection != null) {
                            "Bagian Aktif • ${activeSection.startTimeSec.toInt()}s - ${activeSection.endTimeSec.toInt()}s"
                        } else {
                            "Tekan Putar untuk memulai sinkronisasi"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isPlaying) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF334155))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isPlaying) "SINKRONISASI AKTIF" else "JEDA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isPlaying) Color(0xFF10B981) else Color.LightGray,
                    fontSize = 10.sp
                )
            }
        }
    }
}
