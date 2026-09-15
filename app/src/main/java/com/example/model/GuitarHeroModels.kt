package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * Data model for a note falling down the Guitar Hero highway.
 * Lanes:
 * 0 = Green
 * 1 = Red
 * 2 = Yellow
 * 3 = Blue
 * 4 = Orange
 */
data class GuitarHeroNote(
    val id: String,
    val timestampSec: Float,
    val lane: Int, // 0..4
    val durationSec: Float = 0f,
    val chordName: String? = null,
    val fretNumber: Int? = null
)

/**
 * Highway section banner that flows down the highway indicating upcoming sections.
 */
data class GuitarHeroSectionMarker(
    val id: String,
    val name: String,
    val timestampSec: Float,
    val colorHex: Long
)

object GuitarHeroLaneColors {
    val Green = Color(0xFF22C55E)
    val Red = Color(0xFFEF4444)
    val Yellow = Color(0xFFEAB308)
    val Blue = Color(0xFF3B82F6)
    val Orange = Color(0xFFF97316)

    val laneColors = listOf(Green, Red, Yellow, Blue, Orange)
    val laneGlowColors = listOf(
        Color(0x8822C55E),
        Color(0x88EF4444),
        Color(0x88EAB308),
        Color(0x883B82F6),
        Color(0x88F97316)
    )

    fun getColor(lane: Int): Color = laneColors.getOrElse(lane.coerceIn(0, 4)) { Yellow }
    fun getGlowColor(lane: Int): Color = laneGlowColors.getOrElse(lane.coerceIn(0, 4)) { Yellow }
}

object GuitarHeroTrackGenerator {
    /**
     * Converts a song's sections, chords, and tabs into a rich Guitar Hero track.
     */
    fun generateTrack(song: PracticeSong): Pair<List<GuitarHeroNote>, List<GuitarHeroSectionMarker>> {
        val notes = mutableListOf<GuitarHeroNote>()
        val markers = mutableListOf<GuitarHeroSectionMarker>()

        val secondsPerBeat = 60.0f / song.bpm

        song.sections.forEach { section ->
            // Add section banner marker
            markers.add(
                GuitarHeroSectionMarker(
                    id = "marker_${section.id}",
                    name = section.name,
                    timestampSec = section.startTimeSec,
                    colorHex = section.colorHex
                )
            )

            val chordCount = section.chords.size
            if (chordCount > 0) {
                val chordDuration = section.durationSec / chordCount
                section.chords.forEachIndexed { chordIdx, chordItem ->
                    val chordStartTime = section.startTimeSec + (chordIdx * chordDuration)

                    // Map chord to iconic lanes
                    val chordLanes = mapChordToLanes(chordItem.name)

                    // Beat 1: Chord block (multiple lanes strike together)
                    chordLanes.forEach { lane ->
                        notes.add(
                            GuitarHeroNote(
                                id = "n_${section.id}_${chordIdx}_0_$lane",
                                timestampSec = chordStartTime,
                                lane = lane,
                                chordName = chordItem.name
                            )
                        )
                    }

                    // Beats 2, 3, 4: Rhythmic arpeggio & strum notes down individual lanes
                    val subBeats = listOf(
                        (chordStartTime + secondsPerBeat) to ((chordLanes.firstOrNull() ?: 0) + 1) % 5,
                        (chordStartTime + secondsPerBeat * 2) to ((chordLanes.lastOrNull() ?: 2) + 2) % 5,
                        (chordStartTime + secondsPerBeat * 3) to (chordLanes.getOrNull(1) ?: 3)
                    )

                    subBeats.forEachIndexed { beatIdx, (time, lane) ->
                        if (time < section.endTimeSec) {
                            notes.add(
                                GuitarHeroNote(
                                    id = "n_${section.id}_${chordIdx}_${beatIdx + 1}_$lane",
                                    timestampSec = time,
                                    lane = lane,
                                    chordName = chordItem.name
                                )
                            )
                        }
                    }
                }
            }
        }

        return Pair(notes.sortedBy { it.timestampSec }, markers.sortedBy { it.timestampSec })
    }

    private fun mapChordToLanes(chordName: String): List<Int> {
        return when (chordName.trim().uppercase()) {
            "G" -> listOf(0, 2, 4) // Green, Yellow, Orange
            "EM" -> listOf(0, 1, 3) // Green, Red, Blue
            "C" -> listOf(1, 2, 4) // Red, Yellow, Orange
            "D" -> listOf(2, 3, 4) // Yellow, Blue, Orange
            "AM" -> listOf(1, 2, 3) // Red, Yellow, Blue
            "F" -> listOf(0, 1, 2, 4) // Green, Red, Yellow, Orange
            "A7" -> listOf(1, 3) // Red, Blue
            "D7" -> listOf(2, 3) // Yellow, Blue
            "E7" -> listOf(0, 2) // Green, Yellow
            "BM" -> listOf(1, 3, 4) // Red, Blue, Orange
            else -> listOf(1, 2)
        }
    }
}
