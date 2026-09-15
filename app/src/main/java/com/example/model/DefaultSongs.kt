package com.example.model

object ChordLibrary {
    // String order: [low E, A, D, G, B, high e]
    val G = GuitarChord(
        chordName = "G",
        frets = listOf(3, 2, 0, 0, 3, 3),
        fingers = listOf(2, 1, 0, 0, 3, 4),
        baseFret = 1
    )

    val Em = GuitarChord(
        chordName = "Em",
        frets = listOf(0, 2, 2, 0, 0, 0),
        fingers = listOf(0, 2, 3, 0, 0, 0),
        baseFret = 1
    )

    val C = GuitarChord(
        chordName = "C",
        frets = listOf(-1, 3, 2, 0, 1, 0),
        fingers = listOf(0, 3, 2, 0, 1, 0),
        baseFret = 1
    )

    val D = GuitarChord(
        chordName = "D",
        frets = listOf(-1, -1, 0, 2, 3, 2),
        fingers = listOf(0, 0, 0, 1, 3, 2),
        baseFret = 1
    )

    val Am = GuitarChord(
        chordName = "Am",
        frets = listOf(-1, 0, 2, 2, 1, 0),
        fingers = listOf(0, 0, 2, 3, 1, 0),
        baseFret = 1
    )

    val F = GuitarChord(
        chordName = "F",
        frets = listOf(1, 3, 3, 2, 1, 1),
        fingers = listOf(1, 3, 4, 2, 1, 1),
        baseFret = 1
    )

    val A7 = GuitarChord(
        chordName = "A7",
        frets = listOf(-1, 0, 2, 0, 2, 0),
        fingers = listOf(0, 0, 2, 0, 3, 0),
        baseFret = 1
    )

    val D7 = GuitarChord(
        chordName = "D7",
        frets = listOf(-1, -1, 0, 2, 1, 2),
        fingers = listOf(0, 0, 0, 2, 1, 3),
        baseFret = 1
    )

    val E7 = GuitarChord(
        chordName = "E7",
        frets = listOf(0, 2, 0, 1, 0, 0),
        fingers = listOf(0, 2, 0, 1, 0, 0),
        baseFret = 1
    )

    val Bm = GuitarChord(
        chordName = "Bm",
        frets = listOf(-1, 2, 4, 4, 3, 2),
        fingers = listOf(0, 1, 3, 4, 2, 1),
        baseFret = 2
    )

    fun getChord(name: String): GuitarChord {
        return when (name.trim().uppercase()) {
            "G" -> G
            "EM" -> Em
            "C" -> C
            "D" -> D
            "AM" -> Am
            "F" -> F
            "A7" -> A7
            "D7" -> D7
            "E7" -> E7
            "BM" -> Bm
            else -> GuitarChord(name, listOf(0, 0, 0, 0, 0, 0), listOf(0, 0, 0, 0, 0, 0), 1)
        }
    }
}

object DefaultSongs {
    // Helper to generate tabs
    private fun createArpeggioTab(chordName: String): TabMeasure {
        val c = ChordLibrary.getChord(chordName)
        // string indices: 0=e, 1=B, 2=G, 3=D, 4=A, 5=E
        val notes = mutableListOf<TabColumn>()
        // beat 1: bass note
        val bassString = if (c.frets[0] >= 0) 5 else if (c.frets[1] >= 0) 4 else 3
        val bassFret = if (bassString == 5) c.frets[0] else if (bassString == 4) c.frets[1] else c.frets[2]
        notes.add(TabColumn(0.0f, mapOf(bassString to bassFret)))
        // beat 2: D or G string
        notes.add(TabColumn(1.0f, mapOf(3 to c.frets[2].coerceAtLeast(0))))
        // beat 3: B string
        notes.add(TabColumn(2.0f, mapOf(1 to c.frets[4].coerceAtLeast(0))))
        // beat 4: high e string
        notes.add(TabColumn(3.0f, mapOf(0 to c.frets[5].coerceAtLeast(0))))
        return TabMeasure(label = chordName, notes = notes)
    }

    fun getPopBalladSong(): PracticeSong {
        val s1Intro = SongSection(
            id = "sec_intro",
            name = "Intro",
            startTimeSec = 0f,
            endTimeSec = 12f,
            colorHex = 0xFF6366F1, // Indigo
            chords = listOf(
                ChordItem("G", 4, ChordLibrary.G),
                ChordItem("Em", 4, ChordLibrary.Em),
                ChordItem("C", 4, ChordLibrary.C),
                ChordItem("D", 4, ChordLibrary.D)
            ),
            tabNotation = listOf(
                createArpeggioTab("G"),
                createArpeggioTab("Em"),
                createArpeggioTab("C"),
                createArpeggioTab("D")
            )
        )

        val s2Verse = SongSection(
            id = "sec_verse1",
            name = "Verse 1",
            startTimeSec = 12f,
            endTimeSec = 28f,
            colorHex = 0xFF10B981, // Emerald
            chords = listOf(
                ChordItem("G", 4, ChordLibrary.G),
                ChordItem("Em", 4, ChordLibrary.Em),
                ChordItem("C", 4, ChordLibrary.C),
                ChordItem("D", 4, ChordLibrary.D)
            ),
            tabNotation = listOf(
                createArpeggioTab("G"),
                createArpeggioTab("Em"),
                createArpeggioTab("C"),
                createArpeggioTab("D")
            )
        )

        val s3Chorus = SongSection(
            id = "sec_chorus",
            name = "Chorus",
            startTimeSec = 28f,
            endTimeSec = 44f,
            colorHex = 0xFFF59E0B, // Amber
            chords = listOf(
                ChordItem("C", 4, ChordLibrary.C),
                ChordItem("D", 4, ChordLibrary.D),
                ChordItem("G", 4, ChordLibrary.G),
                ChordItem("Em", 4, ChordLibrary.Em)
            ),
            tabNotation = listOf(
                createArpeggioTab("C"),
                createArpeggioTab("D"),
                createArpeggioTab("G"),
                createArpeggioTab("Em")
            )
        )

        val s4Interlude = SongSection(
            id = "sec_interlude",
            name = "Interlude / Solo",
            startTimeSec = 44f,
            endTimeSec = 56f,
            colorHex = 0xFFEC4899, // Pink
            chords = listOf(
                ChordItem("Am", 4, ChordLibrary.Am),
                ChordItem("Bm", 4, ChordLibrary.Bm),
                ChordItem("C", 4, ChordLibrary.C),
                ChordItem("D", 4, ChordLibrary.D)
            ),
            tabNotation = listOf(
                createArpeggioTab("Am"),
                createArpeggioTab("Bm"),
                createArpeggioTab("C"),
                createArpeggioTab("D")
            )
        )

        val s5Outro = SongSection(
            id = "sec_outro",
            name = "Outro",
            startTimeSec = 56f,
            endTimeSec = 68f,
            colorHex = 0xFF8B5CF6, // Purple
            chords = listOf(
                ChordItem("C", 4, ChordLibrary.C),
                ChordItem("D", 4, ChordLibrary.D),
                ChordItem("G", 4, ChordLibrary.G)
            ),
            tabNotation = listOf(
                createArpeggioTab("C"),
                createArpeggioTab("D"),
                createArpeggioTab("G")
            )
        )

        return PracticeSong(
            id = "song_pop_ballad",
            title = "Acoustic Pop Jam",
            artist = "Latihan Harmoni (G - Em - C - D)",
            bpm = 85,
            totalDurationSec = 68f,
            sections = listOf(s1Intro, s2Verse, s3Chorus, s4Interlude, s5Outro)
        )
    }

    fun getBluesRockSong(): PracticeSong {
        val s1Intro = SongSection(
            id = "blues_intro",
            name = "Intro",
            startTimeSec = 0f,
            endTimeSec = 8f,
            colorHex = 0xFF3B82F6,
            chords = listOf(ChordItem("A7", 4, ChordLibrary.A7), ChordItem("E7", 4, ChordLibrary.E7)),
            tabNotation = listOf(createArpeggioTab("A7"), createArpeggioTab("E7"))
        )

        val s2Verse = SongSection(
            id = "blues_verse",
            name = "Verse 1 (12-Bar)",
            startTimeSec = 8f,
            endTimeSec = 28f,
            colorHex = 0xFF06B6D4,
            chords = listOf(
                ChordItem("A7", 4, ChordLibrary.A7),
                ChordItem("D7", 4, ChordLibrary.D7),
                ChordItem("A7", 4, ChordLibrary.A7),
                ChordItem("E7", 4, ChordLibrary.E7)
            ),
            tabNotation = listOf(
                createArpeggioTab("A7"),
                createArpeggioTab("D7"),
                createArpeggioTab("A7"),
                createArpeggioTab("E7")
            )
        )

        val s3Chorus = SongSection(
            id = "blues_chorus",
            name = "Chorus",
            startTimeSec = 28f,
            endTimeSec = 44f,
            colorHex = 0xFFF97316,
            chords = listOf(
                ChordItem("D7", 4, ChordLibrary.D7),
                ChordItem("A7", 4, ChordLibrary.A7),
                ChordItem("E7", 4, ChordLibrary.E7),
                ChordItem("A7", 4, ChordLibrary.A7)
            ),
            tabNotation = listOf(
                createArpeggioTab("D7"),
                createArpeggioTab("A7"),
                createArpeggioTab("E7"),
                createArpeggioTab("A7")
            )
        )

        val s4Outro = SongSection(
            id = "blues_outro",
            name = "Outro",
            startTimeSec = 44f,
            endTimeSec = 52f,
            colorHex = 0xFF14B8A6,
            chords = listOf(ChordItem("D7", 4, ChordLibrary.D7), ChordItem("A7", 4, ChordLibrary.A7)),
            tabNotation = listOf(createArpeggioTab("D7"), createArpeggioTab("A7"))
        )

        return PracticeSong(
            id = "song_blues_rock",
            title = "Midnight Blues Jam",
            artist = "12-Bar Blues in A (A7 - D7 - E7)",
            bpm = 100,
            totalDurationSec = 52f,
            sections = listOf(s1Intro, s2Verse, s3Chorus, s4Outro)
        )
    }
}
