package com.example.viewmodel

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPracticePlayer
import com.example.audio.WavAudioGenerator
import com.example.model.ChordItem
import com.example.model.ChordLibrary
import com.example.model.ChordLyricWord
import com.example.model.DefaultSongs
import com.example.model.LyricLine
import com.example.model.PracticeSong
import com.example.model.SongSection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicPracticeViewModel(application: Application) : AndroidViewModel(application) {

    val player = AudioPracticePlayer(application)

    private val _currentSong = MutableStateFlow(DefaultSongs.getPopBalladSong())
    val currentSong: StateFlow<PracticeSong> = _currentSong.asStateFlow()

    private val _isLoadingAudio = MutableStateFlow(false)
    val isLoadingAudio: StateFlow<Boolean> = _isLoadingAudio.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Real-time active section based on audio playback position
    val activeSection: StateFlow<SongSection?> = combine(
        _currentSong,
        player.currentPositionSec
    ) { song, currentSec ->
        findActiveSection(song.sections, currentSec)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Real-time active chord & next chord based on audio time and song BPM
    data class ActiveChordInfo(
        val chord: ChordItem,
        val chordIndex: Int,
        val totalChords: Int,
        val beatNumber: Int, // 1, 2, 3, 4
        val chordProgress: Float, // 0.0 .. 1.0 within current chord
        val nextChord: ChordItem? = null,
        val beatsRemainingInChord: Int = 1
    )

    val activeChordInfo: StateFlow<ActiveChordInfo?> = combine(
        activeSection,
        player.currentPositionSec,
        _currentSong
    ) { section, currentSec, song ->
        if (section == null || section.chords.isEmpty()) return@combine null

        val secOffset = (currentSec - section.startTimeSec).coerceAtLeast(0f)
        val chordDuration = section.durationSec / section.chords.size
        val chordIdx = (secOffset / chordDuration).toInt().coerceIn(0, section.chords.size - 1)
        val chordItem = section.chords[chordIdx]

        val chordOffset = secOffset - (chordIdx * chordDuration)
        val chordProgress = (chordOffset / chordDuration).coerceIn(0f, 1f)

        // Beat in measure (4/4 time)
        val secPerBeat = (60.0f / song.bpm).coerceAtLeast(0.2f)
        val beatInMeasure = ((chordOffset / secPerBeat) % 4.0f).toInt() + 1
        val beatsRemaining = (4 - beatInMeasure).coerceAtLeast(1)

        // Next chord determination
        val nextChord = if (chordIdx + 1 < section.chords.size) {
            section.chords[chordIdx + 1]
        } else {
            // Check next section's first chord or loop around
            val currentSecIdx = song.sections.indexOf(section)
            val nextSec = song.sections.getOrNull(currentSecIdx + 1) ?: song.sections.firstOrNull()
            nextSec?.chords?.firstOrNull()
        }

        ActiveChordInfo(
            chord = chordItem,
            chordIndex = chordIdx,
            totalChords = section.chords.size,
            beatNumber = beatInMeasure,
            chordProgress = chordProgress,
            nextChord = nextChord,
            beatsRemainingInChord = beatsRemaining
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Synchronized lyrics: currently active lyric line
    val activeLyricLine: StateFlow<LyricLine?> = combine(
        _currentSong,
        player.currentPositionSec
    ) { song, currentSec ->
        song.lyrics.find { currentSec >= it.startTimeSec && currentSec < it.endTimeSec }
            ?: song.lyrics.lastOrNull { it.startTimeSec <= currentSec }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Synchronized lyrics: active word/chord in the current line
    val activeChordWord: StateFlow<ChordLyricWord?> = combine(
        activeLyricLine,
        player.currentPositionSec
    ) { lyricLine, currentSec ->
        lyricLine?.chordWords?.lastOrNull { currentSec >= it.timestampSec }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        loadSongAudio(_currentSong.value)
    }

    fun selectPresetSong(song: PracticeSong) {
        player.pause()
        _currentSong.value = song
        loadSongAudio(song)
    }

    private fun loadSongAudio(song: PracticeSong) {
        viewModelScope.launch {
            _isLoadingAudio.value = true
            try {
                if (song.customAudioUri != null) {
                    val uri = Uri.parse(song.customAudioUri)
                    player.loadFromUri(uri)
                    _statusMessage.value = "Memuat audio: ${song.audioFileName ?: "Audio"}"
                } else {
                    _statusMessage.value = "Menyiapkan harmoni audio ${song.title}..."
                    val audioFile = WavAudioGenerator.getOrCreateBackingTrack(getApplication(), song)
                    player.loadFromFile(audioFile)
                    _statusMessage.value = "Kord & Audio sinkron 100%!"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Gagal memuat audio: ${e.localizedMessage}"
            } finally {
                _isLoadingAudio.value = false
            }
        }
    }

    fun onAudioFileSelected(uri: Uri) {
        viewModelScope.launch {
            _isLoadingAudio.value = true
            try {
                player.pause()
                val context = getApplication<Application>()
                var fileName = "Audio Latihan"

                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx >= 0 && cursor.moveToFirst()) {
                        fileName = cursor.getString(nameIdx)
                    }
                }

                player.loadFromUri(uri)

                val updatedSong = _currentSong.value.copy(
                    title = fileName.substringBeforeLast("."),
                    artist = "File Audio Lokal",
                    customAudioUri = uri.toString(),
                    audioFileName = fileName,
                    totalDurationSec = if (player.durationSec.value > 0f) player.durationSec.value else _currentSong.value.totalDurationSec
                )
                _currentSong.value = updatedSong
                _statusMessage.value = "File audio dimuat: $fileName"
            } catch (e: Exception) {
                _statusMessage.value = "Error memuat audio: ${e.message}"
            } finally {
                _isLoadingAudio.value = false
            }
        }
    }

    fun jumpToLyric(lyric: LyricLine) {
        player.seekTo(lyric.startTimeSec)
        if (!player.isPlaying.value) {
            player.play()
        }
    }

    fun resetToDefault() {
        selectPresetSong(DefaultSongs.getPopBalladSong())
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }

    private fun findActiveSection(sections: List<SongSection>, currentSec: Float): SongSection? {
        if (sections.isEmpty()) return null
        val exact = sections.find { currentSec >= it.startTimeSec && currentSec < it.endTimeSec }
        if (exact != null) return exact
        return sections.lastOrNull { it.startTimeSec <= currentSec } ?: sections.firstOrNull()
    }
}
