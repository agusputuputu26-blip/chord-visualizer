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

    fun getPopBalladSong(): PracticeSong {
        val s1Intro = SongSection(
            id = "sec_intro",
            name = "Intro",
            startTimeSec = 0f,
            endTimeSec = 12f,
            colorHex = 0xFF6366F1,
            chords = listOf(
                ChordItem("G", 4, ChordLibrary.G),
                ChordItem("Em", 4, ChordLibrary.Em),
                ChordItem("C", 4, ChordLibrary.C),
                ChordItem("D", 4, ChordLibrary.D)
            )
        )

        val s2Verse = SongSection(
            id = "sec_verse1",
            name = "Bait 1 (Verse)",
            startTimeSec = 12f,
            endTimeSec = 28f,
            colorHex = 0xFF10B981,
            chords = listOf(
                ChordItem("G", 4, ChordLibrary.G),
                ChordItem("Em", 4, ChordLibrary.Em),
                ChordItem("C", 4, ChordLibrary.C),
                ChordItem("D", 4, ChordLibrary.D)
            )
        )

        val s3Chorus = SongSection(
            id = "sec_chorus",
            name = "Reff (Chorus)",
            startTimeSec = 28f,
            endTimeSec = 44f,
            colorHex = 0xFFF59E0B,
            chords = listOf(
                ChordItem("C", 4, ChordLibrary.C),
                ChordItem("D", 4, ChordLibrary.D),
                ChordItem("G", 4, ChordLibrary.G),
                ChordItem("Em", 4, ChordLibrary.Em)
            )
        )

        val s4Interlude = SongSection(
            id = "sec_interlude",
            name = "Interlude",
            startTimeSec = 44f,
            endTimeSec = 56f,
            colorHex = 0xFFEC4899,
            chords = listOf(
                ChordItem("Am", 4, ChordLibrary.Am),
                ChordItem("Bm", 4, ChordLibrary.Bm),
                ChordItem("C", 4, ChordLibrary.C),
                ChordItem("D", 4, ChordLibrary.D)
            )
        )

        val s5Outro = SongSection(
            id = "sec_outro",
            name = "Outro",
            startTimeSec = 56f,
            endTimeSec = 68f,
            colorHex = 0xFF8B5CF6,
            chords = listOf(
                ChordItem("C", 4, ChordLibrary.C),
                ChordItem("D", 4, ChordLibrary.D),
                ChordItem("G", 4, ChordLibrary.G)
            )
        )

        val lyrics = listOf(
            LyricLine(
                id = "l_intro",
                startTimeSec = 0f,
                endTimeSec = 12f,
                text = "(Petikan Akustik Harmoni Intro)",
                chordWords = listOf(
                    ChordLyricWord("G", "(Petikan", 0f),
                    ChordLyricWord("Em", "Akustik", 3f),
                    ChordLyricWord("C", "Harmoni", 6f),
                    ChordLyricWord("D", "Intro)", 9f)
                )
            ),
            LyricLine(
                id = "l_verse1_1",
                startTimeSec = 12f,
                endTimeSec = 20f,
                text = "Kau begitu sempurna di mataku",
                chordWords = listOf(
                    ChordLyricWord("G", "Kau", 12f),
                    ChordLyricWord("Em", "sempurna", 16f)
                )
            ),
            LyricLine(
                id = "l_verse1_2",
                startTimeSec = 20f,
                endTimeSec = 28f,
                text = "Kau membuat diriku selalu memujamu",
                chordWords = listOf(
                    ChordLyricWord("C", "Kau", 20f),
                    ChordLyricWord("D", "selalu", 24f)
                )
            ),
            LyricLine(
                id = "l_chorus_1",
                startTimeSec = 28f,
                endTimeSec = 36f,
                text = "Janganlah kau pernah hancurkan rasa ini",
                chordWords = listOf(
                    ChordLyricWord("C", "Janganlah", 28f),
                    ChordLyricWord("D", "hancurkan", 32f)
                )
            ),
            LyricLine(
                id = "l_chorus_2",
                startTimeSec = 36f,
                endTimeSec = 44f,
                text = "Karena kaulah seluruh nafasku dan jiwaku",
                chordWords = listOf(
                    ChordLyricWord("G", "Karena", 36f),
                    ChordLyricWord("Em", "seluruh", 40f)
                )
            ),
            LyricLine(
                id = "l_interlude",
                startTimeSec = 44f,
                endTimeSec = 56f,
                text = "Tuk selamanya di dalam pelukanku",
                chordWords = listOf(
                    ChordLyricWord("Am", "Tuk", 44f),
                    ChordLyricWord("Bm", "selamanya", 47f),
                    ChordLyricWord("C", "dalam", 50f),
                    ChordLyricWord("D", "pelukanku", 53f)
                )
            ),
            LyricLine(
                id = "l_outro",
                startTimeSec = 56f,
                endTimeSec = 68f,
                text = "Sempurna... Kau begitu indah selamanya",
                chordWords = listOf(
                    ChordLyricWord("C", "Sempurna...", 56f),
                    ChordLyricWord("D", "indah", 60f),
                    ChordLyricWord("G", "selamanya", 64f)
                )
            )
        )

        return PracticeSong(
            id = "song_pop_ballad",
            title = "Sempurna (Acoustic Jam)",
            artist = "Latihan Kord & Lirik (G - Em - C - D)",
            bpm = 85,
            totalDurationSec = 68f,
            sections = listOf(s1Intro, s2Verse, s3Chorus, s4Interlude, s5Outro),
            lyrics = lyrics
        )
    }

    fun getBluesRockSong(): PracticeSong {
        val s1Intro = SongSection(
            id = "sec_kemesraan_intro",
            name = "Intro",
            startTimeSec = 0f,
            endTimeSec = 10f,
            colorHex = 0xFF38BDF8,
            chords = listOf(
                ChordItem("C", 4, ChordLibrary.C),
                ChordItem("F", 4, ChordLibrary.F),
                ChordItem("G", 4, ChordLibrary.G),
                ChordItem("C", 4, ChordLibrary.C)
            )
        )

        val s2Verse = SongSection(
            id = "sec_kemesraan_verse",
            name = "Bait 1 (Verse)",
            startTimeSec = 10f,
            endTimeSec = 26f,
            colorHex = 0xFF10B981,
            chords = listOf(
                ChordItem("C", 4, ChordLibrary.C),
                ChordItem("F", 4, ChordLibrary.F),
                ChordItem("G", 4, ChordLibrary.G),
                ChordItem("C", 4, ChordLibrary.C)
            )
        )

        val s3Chorus = SongSection(
            id = "sec_kemesraan_chorus",
            name = "Reff (Chorus)",
            startTimeSec = 26f,
            endTimeSec = 44f,
            colorHex = 0xFFF59E0B,
            chords = listOf(
                ChordItem("F", 4, ChordLibrary.F),
                ChordItem("G", 4, ChordLibrary.G),
                ChordItem("C", 4, ChordLibrary.C),
                ChordItem("Am", 4, ChordLibrary.Am),
                ChordItem("F", 4, ChordLibrary.F),
                ChordItem("G", 4, ChordLibrary.G),
                ChordItem("C", 4, ChordLibrary.C)
            )
        )

        val s4Outro = SongSection(
            id = "sec_kemesraan_outro",
            name = "Outro",
            startTimeSec = 44f,
            endTimeSec = 54f,
            colorHex = 0xFF8B5CF6,
            chords = listOf(
                ChordItem("F", 4, ChordLibrary.F),
                ChordItem("G", 4, ChordLibrary.G),
                ChordItem("C", 4, ChordLibrary.C)
            )
        )

        val lyrics = listOf(
            LyricLine(
                id = "km_intro",
                startTimeSec = 0f,
                endTimeSec = 10f,
                text = "(Alunan Petikan Gitar Akustik C - F - G - C)",
                chordWords = listOf(
                    ChordLyricWord("C", "(Alunan", 0f),
                    ChordLyricWord("F", "Petikan", 2.5f),
                    ChordLyricWord("G", "Gitar", 5f),
                    ChordLyricWord("C", "Akustik)", 7.5f)
                )
            ),
            LyricLine(
                id = "km_verse_1",
                startTimeSec = 10f,
                endTimeSec = 18f,
                text = "Suatu hari di kala kita duduk di tepi pantai",
                chordWords = listOf(
                    ChordLyricWord("C", "Suatu", 10f),
                    ChordLyricWord("F", "duduk", 14f)
                )
            ),
            LyricLine(
                id = "km_verse_2",
                startTimeSec = 18f,
                endTimeSec = 26f,
                text = "Dan memandang ombak di lautan yang kian menepi",
                chordWords = listOf(
                    ChordLyricWord("G", "Dan", 18f),
                    ChordLyricWord("C", "lautan", 22f)
                )
            ),
            LyricLine(
                id = "km_chorus_1",
                startTimeSec = 26f,
                endTimeSec = 35f,
                text = "Kemesraan ini janganlah cepat berlalu",
                chordWords = listOf(
                    ChordLyricWord("F", "Kemesraan", 26f),
                    ChordLyricWord("G", "janganlah", 29f),
                    ChordLyricWord("C", "berlalu", 32f)
                )
            ),
            LyricLine(
                id = "km_chorus_2",
                startTimeSec = 35f,
                endTimeSec = 44f,
                text = "Kemesraan ini ingin kukenang selalu",
                chordWords = listOf(
                    ChordLyricWord("Am", "Kemesraan", 35f),
                    ChordLyricWord("F", "ingin", 38f),
                    ChordLyricWord("G", "kukenang", 40f),
                    ChordLyricWord("C", "selalu", 42f)
                )
            ),
            LyricLine(
                id = "km_outro",
                startTimeSec = 44f,
                endTimeSec = 54f,
                text = "Hatiku damai... Jiwaku tentram bersamamu",
                chordWords = listOf(
                    ChordLyricWord("F", "Hatiku", 44f),
                    ChordLyricWord("G", "Jiwaku", 47f),
                    ChordLyricWord("C", "bersamamu", 50f)
                )
            )
        )

        return PracticeSong(
            id = "song_kemesraan",
            title = "Kemesraan (Folk Ballad)",
            artist = "Latihan Kord & Lirik (C - F - G - Am)",
            bpm = 80,
            totalDurationSec = 54f,
            sections = listOf(s1Intro, s2Verse, s3Chorus, s4Outro),
            lyrics = lyrics
        )
    }
}
