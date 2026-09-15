package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.DefaultSongs
import com.example.model.GuitarHeroTrackGenerator
import com.example.model.SongSection
import com.example.ui.components.AudioPlayerBar
import com.example.ui.components.ChordTabDisplay
import com.example.ui.components.GuitarHeroVisualizer
import com.example.ui.components.SectionEditorDialog
import com.example.ui.components.SongSectionsPanel
import com.example.ui.components.VideoExportDialog
import com.example.viewmodel.MusicPracticeViewModel

enum class PracticeViewMode {
    GUITAR_HERO,
    CHORD_TAB,
    SPLIT_BOTH
}

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

    // Active theme / view mode: Guitar Hero vs Tabulasi Standar vs Keduanya
    var activeViewMode by remember { mutableStateOf(PracticeViewMode.CHORD_TAB) }

    // Video Export dialog state
    var isExportVideoDialogOpen by remember { mutableStateOf(false) }

    // Section Editor dialog state
    var isEditorOpen by remember { mutableStateOf(false) }
    var sectionToEdit by remember { mutableStateOf<SongSection?>(null) }

    // Generate Guitar Hero track notes & section markers in sync with current song
    val (guitarHeroNotes, guitarHeroMarkers) = remember(currentSong) {
        GuitarHeroTrackGenerator.generateTrack(currentSong)
    }

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
                                .background(
                                    if (activeViewMode == PracticeViewMode.GUITAR_HERO) Color(0xFFEF4444) else Color(0xFFF59E0B)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (activeViewMode == PracticeViewMode.GUITAR_HERO) Icons.Default.Bolt else Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = if (activeViewMode == PracticeViewMode.GUITAR_HERO) Color.White else Color(0xFF0F172A),
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
                                text = when (activeViewMode) {
                                    PracticeViewMode.GUITAR_HERO -> "Tema: Guitar Hero (${currentSong.bpm} BPM)"
                                    PracticeViewMode.CHORD_TAB -> "Tema: Tabulasi Standar (${currentSong.bpm} BPM)"
                                    PracticeViewMode.SPLIT_BOTH -> "Tema: Keduanya (${currentSong.bpm} BPM)"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = if (activeViewMode == PracticeViewMode.GUITAR_HERO) Color(0xFFF97316) else Color(0xFF38BDF8)
                            )
                        }
                    }
                },
                actions = {
                    // Quick Toggle between Guitar Hero & Tabulasi Standar
                    IconButton(
                        onClick = {
                            activeViewMode = if (activeViewMode == PracticeViewMode.GUITAR_HERO) {
                                PracticeViewMode.CHORD_TAB
                            } else {
                                PracticeViewMode.GUITAR_HERO
                            }
                        },
                        modifier = Modifier.testTag("toggle_theme_button")
                    ) {
                        Icon(
                            imageVector = if (activeViewMode == PracticeViewMode.GUITAR_HERO) Icons.Default.MusicNote else Icons.Default.Bolt,
                            contentDescription = "Ganti Tema",
                            tint = if (activeViewMode == PracticeViewMode.GUITAR_HERO) Color(0xFF38BDF8) else Color(0xFFEF4444)
                        )
                    }

                    // Video Export Direct Button in Top Bar
                    IconButton(
                        onClick = { isExportVideoDialogOpen = true },
                        modifier = Modifier.testTag("top_export_video_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Export Video",
                            tint = Color(0xFFF59E0B)
                        )
                    }

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Theme / Mode Selector Card
            item {
                ThemeSelectionCard(
                    activeMode = activeViewMode,
                    onSelectMode = { activeViewMode = it }
                )
            }

            // Active Section Header Banner
            item {
                ActiveSectionBanner(
                    activeSection = activeSection,
                    currentPositionSec = currentPositionSec,
                    isPlaying = isPlaying
                )
            }

            // Primary Interactive Display based on chosen theme:
            when (activeViewMode) {
                PracticeViewMode.GUITAR_HERO -> {
                    item {
                        GuitarHeroVisualizer(
                            notes = guitarHeroNotes,
                            sectionMarkers = guitarHeroMarkers,
                            currentAudioTimeSec = currentPositionSec,
                            bpm = currentSong.bpm,
                            isPlaying = isPlaying,
                            onExportVideoClick = { isExportVideoDialogOpen = true }
                        )
                    }
                }

                PracticeViewMode.CHORD_TAB -> {
                    item {
                        ChordTabDisplay(
                            activeSection = activeSection,
                            activeChordInfo = activeChordInfo,
                            onChordClick = {
                                // Quick feedback
                            }
                        )
                    }
                }

                PracticeViewMode.SPLIT_BOTH -> {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            GuitarHeroVisualizer(
                                notes = guitarHeroNotes,
                                sectionMarkers = guitarHeroMarkers,
                                currentAudioTimeSec = currentPositionSec,
                                bpm = currentSong.bpm,
                                isPlaying = isPlaying,
                                onExportVideoClick = { isExportVideoDialogOpen = true }
                            )

                            ChordTabDisplay(
                                activeSection = activeSection,
                                activeChordInfo = activeChordInfo,
                                onChordClick = {}
                            )
                        }
                    }
                }
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

    // Video Export Dialog
    if (isExportVideoDialogOpen) {
        VideoExportDialog(
            song = currentSong,
            activeSection = activeSection,
            notes = guitarHeroNotes,
            sectionMarkers = guitarHeroMarkers,
            currentAudioTimeSec = currentPositionSec,
            onDismiss = { isExportVideoDialogOpen = false }
        )
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

/**
 * Prominent Theme / View Mode Selector Card
 */
@Composable
private fun ThemeSelectionCard(
    activeMode: PracticeViewMode,
    onSelectMode: (PracticeViewMode) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("theme_selection_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DashboardCustomize,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "PILIH TEMA TAMPILAN",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = when (activeMode) {
                        PracticeViewMode.GUITAR_HERO -> "Tema Guitar Hero"
                        PracticeViewMode.CHORD_TAB -> "Tema Tabulasi Standar"
                        PracticeViewMode.SPLIT_BOTH -> "Tema Gabungan"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = when (activeMode) {
                        PracticeViewMode.GUITAR_HERO -> Color(0xFFEF4444)
                        PracticeViewMode.CHORD_TAB -> Color(0xFF38BDF8)
                        PracticeViewMode.SPLIT_BOTH -> Color(0xFFF59E0B)
                    }
                )
            }

            // 3 Theme Option Buttons: Tab Standar, Guitar Hero, Keduanya
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Option 1: Tabulasi Standar (Awal)
                val isStandardSelected = activeMode == PracticeViewMode.CHORD_TAB
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isStandardSelected) Color(0xFF0369A1) else Color(0xFF0F172A)
                        )
                        .border(
                            width = if (isStandardSelected) 1.5.dp else 1.dp,
                            color = if (isStandardSelected) Color(0xFF38BDF8) else Color(0xFF334155),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectMode(PracticeViewMode.CHORD_TAB) }
                        .padding(vertical = 10.dp, horizontal = 8.dp)
                        .testTag("theme_button_tab_standard"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = if (isStandardSelected) Color.White else Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Tab Standar",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Fretboard & Tab",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = if (isStandardSelected) Color(0xFFE0F2FE) else Color(0xFF94A3B8)
                        )
                    }
                }

                // Option 2: Guitar Hero Mode
                val isHeroSelected = activeMode == PracticeViewMode.GUITAR_HERO
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isHeroSelected) Color(0xFFB91C1C) else Color(0xFF0F172A)
                        )
                        .border(
                            width = if (isHeroSelected) 1.5.dp else 1.dp,
                            color = if (isHeroSelected) Color(0xFFEF4444) else Color(0xFF334155),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectMode(PracticeViewMode.GUITAR_HERO) }
                        .padding(vertical = 10.dp, horizontal = 8.dp)
                        .testTag("theme_button_guitar_hero"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = if (isHeroSelected) Color.White else Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Guitar Hero",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Balok Meluncur",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = if (isHeroSelected) Color(0xFFFEE2E2) else Color(0xFF94A3B8)
                        )
                    }
                }

                // Option 3: Split Both
                val isBothSelected = activeMode == PracticeViewMode.SPLIT_BOTH
                Box(
                    modifier = Modifier
                        .weight(0.9f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isBothSelected) Color(0xFFB45309) else Color(0xFF0F172A)
                        )
                        .border(
                            width = if (isBothSelected) 1.5.dp else 1.dp,
                            color = if (isBothSelected) Color(0xFFF59E0B) else Color(0xFF334155),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectMode(PracticeViewMode.SPLIT_BOTH) }
                        .padding(vertical = 10.dp, horizontal = 6.dp)
                        .testTag("theme_button_split_both"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🔀",
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Keduanya",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Split View",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = if (isBothSelected) Color(0xFFFEF3C7) else Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
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
