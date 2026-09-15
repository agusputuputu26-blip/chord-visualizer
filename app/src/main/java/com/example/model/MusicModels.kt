package com.example.model

/**
 * Data model for a song section (e.g. Intro, Verse, Chorus, Solo, Outro).
 */
data class SongSection(
    val id: String,
    val name: String,
    val startTimeSec: Float,
    val endTimeSec: Float,
    val colorHex: Long,
    val chords: List<ChordItem> = emptyList(),
    val tabNotation: List<TabMeasure> = emptyList()
) {
    val durationSec: Float get() = (endTimeSec - startTimeSec).coerceAtLeast(0.1f)
}

/**
 * Single chord in a section progression.
 */
data class ChordItem(
    val name: String,
    val beats: Int = 4,
    val fingering: GuitarChord? = null
)

/**
 * Guitar chord representation for 6 strings (E A D G B e).
 * frets: list of 6 integers. -1 = Muted ('X'), 0 = Open ('O'), 1..24 = Fret number.
 * fingers: list of 6 integers. 0 = none, 1 = index, 2 = middle, 3 = ring, 4 = pinky.
 */
data class GuitarChord(
    val chordName: String,
    val frets: List<Int>, // 6 strings from low E to high e: [E, A, D, G, B, e]
    val fingers: List<Int> = listOf(0, 0, 0, 0, 0, 0),
    val baseFret: Int = 1
)

/**
 * Tablature measure for guitar (6 strings).
 */
data class TabMeasure(
    val label: String,
    val notes: List<TabColumn> // sequential beat/sub-beat columns
)

data class TabColumn(
    val beatOffset: Float, // 0.0, 1.0, 2.0, 3.0
    val strings: Map<Int, Int> // String index (0=high e, 1=B, 2=G, 3=D, 4=A, 5=low E) -> fret number
)

/**
 * Full song model.
 */
data class PracticeSong(
    val id: String,
    val title: String,
    val artist: String,
    val bpm: Int,
    val totalDurationSec: Float,
    val sections: List<SongSection>,
    val customAudioUri: String? = null,
    val audioFileName: String? = null
)
