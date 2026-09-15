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
import com.example.model.DefaultSongs
import com.example.model.GuitarChord
import com.example.model.PracticeSong
import com.example.model.SongSection
import com.example.model.TabColumn
import com.example.model.TabMeasure
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

    // Real-time active section based on current audio playback position
    val activeSection: StateFlow<SongSection?> = combine(
        _currentSong,
        player.currentPositionSec
    ) { song, currentSec ->
        findActiveSection(song.sections, currentSec)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Real-time active chord based on active section and time
    data class ActiveChordInfo(
        val chord: ChordItem,
        val chordIndex: Int,
        val totalChords: Int,
        val beatNumber: Int, // 1, 2, 3, 4
        val chordProgress: Float // 0.0 .. 1.0 within current chord
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
        val secPerBeat = 60.0f / song.bpm
        val beatInMeasure = ((chordOffset / secPerBeat) % 4.0f).toInt() + 1

        ActiveChordInfo(
            chord = chordItem,
            chordIndex = chordIdx,
            totalChords = section.chords.size,
            beatNumber = beatInMeasure,
            chordProgress = chordProgress
        )
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
                    _statusMessage.value = "Memuat audio lokal: ${song.audioFileName ?: "Audio"}"
                } else {
                    _statusMessage.value = "Menyiapkan backing track ${song.title}..."
                    val audioFile = WavAudioGenerator.getOrCreateBackingTrack(getApplication(), song)
                    player.loadFromFile(audioFile)
                    _statusMessage.value = "Siap untuk latihan!"
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

                // Update current song to incorporate custom audio
                val updatedSong = _currentSong.value.copy(
                    title = fileName.substringBeforeLast("."),
                    artist = "File Audio Lokal",
                    customAudioUri = uri.toString(),
                    audioFileName = fileName,
                    totalDurationSec = if (player.durationSec.value > 0f) player.durationSec.value else _currentSong.value.totalDurationSec
                )
                _currentSong.value = updatedSong
                _statusMessage.value = "File audio berhasil diunggah: $fileName"
            } catch (e: Exception) {
                _statusMessage.value = "Error memuat file audio: ${e.message}"
            } finally {
                _isLoadingAudio.value = false
            }
        }
    }

    fun jumpToSection(section: SongSection) {
        player.seekTo(section.startTimeSec)
        if (player.isLoopingSection.value) {
            player.setLoopRange(section.startTimeSec, section.endTimeSec, true)
        }
    }

    fun toggleLoopSection(section: SongSection) {
        val isCurrentlyLoopingThis = player.isLoopingSection.value &&
                activeSection.value?.id == section.id

        if (isCurrentlyLoopingThis) {
            player.setLoopRange(0f, Float.MAX_VALUE, false)
        } else {
            player.seekTo(section.startTimeSec)
            player.setLoopRange(section.startTimeSec, section.endTimeSec, true)
            if (!player.isPlaying.value) {
                player.play()
            }
        }
    }

    fun updateSection(updated: SongSection) {
        val currentSections = _currentSong.value.sections.toMutableList()
        val index = currentSections.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            currentSections[index] = updated
            // Sort by start time
            currentSections.sortBy { it.startTimeSec }
            _currentSong.value = _currentSong.value.copy(sections = currentSections)
            _statusMessage.value = "Bagian '${updated.name}' diperbarui"
        }
    }

    fun addSection(name: String, startTimeSec: Float, chordNames: List<String>) {
        val chords = chordNames.map { name ->
            val chord = ChordLibrary.getChord(name)
            ChordItem(name = chord.chordName, beats = 4, fingering = chord)
        }
        val tabMeasures = chordNames.map { chordName ->
            createArpeggioTab(chordName)
        }

        val estimatedEndTime = startTimeSec + (chordNames.size * 4f)
        val newSection = SongSection(
            id = "sec_${System.currentTimeMillis()}",
            name = name,
            startTimeSec = startTimeSec,
            endTimeSec = estimatedEndTime,
            colorHex = 0xFF06B6D4,
            chords = chords,
            tabNotation = tabMeasures
        )

        val updatedSections = (_currentSong.value.sections + newSection).sortedBy { it.startTimeSec }
        _currentSong.value = _currentSong.value.copy(sections = updatedSections)
        _statusMessage.value = "Bagian baru '$name' ditambahkan di detik ${startTimeSec.toInt()}s"
    }

    fun deleteSection(sectionId: String) {
        val updatedSections = _currentSong.value.sections.filterNot { it.id == sectionId }
        if (updatedSections.isNotEmpty()) {
            _currentSong.value = _currentSong.value.copy(sections = updatedSections)
            _statusMessage.value = "Bagian dihapus"
        }
    }

    fun setStartTimeToCurrent(sectionId: String) {
        val currentSec = player.currentPositionSec.value
        val section = _currentSong.value.sections.find { it.id == sectionId } ?: return
        val updated = section.copy(
            startTimeSec = currentSec,
            endTimeSec = (currentSec + section.durationSec).coerceAtLeast(currentSec + 2f)
        )
        updateSection(updated)
        _statusMessage.value = "Waktu mulai diatur ke ${String.format("%.1f", currentSec)}s"
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
        // Exact match within [start, end)
        val exact = sections.find { currentSec >= it.startTimeSec && currentSec < it.endTimeSec }
        if (exact != null) return exact

        // Or the latest section whose startTimeSec <= currentSec
        return sections.lastOrNull { it.startTimeSec <= currentSec } ?: sections.firstOrNull()
    }

    private fun createArpeggioTab(chordName: String): TabMeasure {
        val c = ChordLibrary.getChord(chordName)
        val notes = mutableListOf<TabColumn>()
        val bassString = if (c.frets[0] >= 0) 5 else if (c.frets[1] >= 0) 4 else 3
        val bassFret = if (bassString == 5) c.frets[0] else if (bassString == 4) c.frets[1] else c.frets[2]
        notes.add(TabColumn(0.0f, mapOf(bassString to bassFret)))
        notes.add(TabColumn(1.0f, mapOf(3 to c.frets[2].coerceAtLeast(0))))
        notes.add(TabColumn(2.0f, mapOf(1 to c.frets[4].coerceAtLeast(0))))
        notes.add(TabColumn(3.0f, mapOf(0 to c.frets[5].coerceAtLeast(0))))
        return TabMeasure(label = chordName, notes = notes)
    }
}
