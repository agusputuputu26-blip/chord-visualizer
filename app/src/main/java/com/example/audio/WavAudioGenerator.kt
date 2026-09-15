package com.example.audio

import android.content.Context
import com.example.model.PracticeSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object WavAudioGenerator {
    private const val SAMPLE_RATE = 44100
    private const val NUM_CHANNELS = 2 // Stereo

    // Standard open guitar string frequencies (Hz): Low E, A, D, G, B, High e
    private val STRING_BASE_FREQS = doubleArrayOf(82.41, 110.0, 146.83, 196.0, 246.94, 329.63)

    /**
     * Generates or retrieves a pre-synthesized backing WAV audio file for the given practice song.
     */
    suspend fun getOrCreateBackingTrack(context: Context, song: PracticeSong): File = withContext(Dispatchers.IO) {
        val cacheFile = File(context.cacheDir, "backing_${song.id}_v2.wav")
        if (cacheFile.exists() && cacheFile.length() > 44) {
            return@withContext cacheFile
        }

        val totalDurationSec = song.totalDurationSec.coerceAtLeast(10f)
        val totalSamples = (totalDurationSec * SAMPLE_RATE).toInt()

        // Write WAV
        val fos = FileOutputStream(cacheFile)
        // Write placeholder header (44 bytes)
        fos.write(ByteArray(44))

        val bufferSize = 8192
        val byteBuffer = ByteArray(bufferSize * 4) // 2 channels * 2 bytes/sample
        var sampleIndex = 0

        val secondsPerBeat = 60.0 / song.bpm
        var currentSectionIndex = 0

        while (sampleIndex < totalSamples) {
            val chunkLength = minOf(bufferSize, totalSamples - sampleIndex)
            var bytePos = 0

            for (i in 0 until chunkLength) {
                val currentSec = (sampleIndex + i).toDouble() / SAMPLE_RATE

                // Determine active section
                if (currentSectionIndex < song.sections.size - 1 &&
                    currentSec >= song.sections[currentSectionIndex + 1].startTimeSec
                ) {
                    currentSectionIndex++
                }
                val section = song.sections.getOrNull(currentSectionIndex) ?: song.sections.last()
                val secOffset = currentSec - section.startTimeSec
                val chordList = section.chords

                var mixedSample = 0.0

                if (chordList.isNotEmpty()) {
                    val chordDuration = section.durationSec / chordList.size
                    val chordIdx = ((secOffset / chordDuration).toInt()).coerceIn(0, chordList.size - 1)
                    val activeChord = chordList[chordIdx]
                    val chordSecOffset = secOffset - (chordIdx * chordDuration)

                    // Guitar arpeggio / strum synthesis
                    val fingering = activeChord.fingering
                    if (fingering != null) {
                        // 4 beats per measure arpeggiation
                        val beatInMeasure = (chordSecOffset / secondsPerBeat) % 4.0
                        val currentBeatInt = beatInMeasure.toInt()
                        val beatFraction = beatInMeasure - currentBeatInt

                        // Trigger strings based on beat
                        val stringToPluck = when (currentBeatInt) {
                            0 -> if (fingering.frets[0] >= 0) 0 else if (fingering.frets[1] >= 0) 1 else 2
                            1 -> 2
                            2 -> 3
                            3 -> 4
                            else -> 5
                        }

                        val fret = fingering.frets.getOrElse(stringToPluck) { 0 }
                        if (fret >= 0) {
                            val baseFreq = STRING_BASE_FREQS.getOrElse(stringToPluck) { 110.0 }
                            val noteFreq = baseFreq * Math.pow(2.0, fret.toDouble() / 12.0)
                            val noteAgeSec = beatFraction * secondsPerBeat

                            // Acoustic string synthesis with 3 harmonics and smooth exponential decay
                            val decay = exp(-noteAgeSec * 3.5)
                            val fundamental = sin(2.0 * PI * noteFreq * noteAgeSec)
                            val harmonic2 = 0.5 * sin(2.0 * PI * noteFreq * 2.0 * noteAgeSec)
                            val harmonic3 = 0.25 * sin(2.0 * PI * noteFreq * 3.0 * noteAgeSec)
                            mixedSample += (fundamental + harmonic2 + harmonic3) * decay * 0.45
                        }

                        // Warm pad bass resonance on chord start
                        val bassAge = chordSecOffset
                        val bassFret = fingering.frets.firstOrNull { it >= 0 } ?: 0
                        val bassFreq = STRING_BASE_FREQS[0] * Math.pow(2.0, bassFret.toDouble() / 12.0)
                        val bassDecay = exp(-bassAge * 1.2)
                        mixedSample += sin(2.0 * PI * bassFreq * bassAge) * bassDecay * 0.25
                    }
                }

                // Subtle rhythmic hi-hat & metronome tick on each beat
                val beatProgress = (currentSec / secondsPerBeat) % 1.0
                val tickAge = beatProgress * secondsPerBeat
                if (tickAge < 0.05) {
                    val clickDecay = exp(-tickAge * 100.0)
                    val click = sin(2.0 * PI * 2200.0 * tickAge) * clickDecay * 0.12
                    mixedSample += click
                }

                // Master gain and limiter
                val clamped = (mixedSample * 0.75).coerceIn(-0.95, 0.95)
                val shortVal = (clamped * 32767.0).toInt().toShort()

                // Left channel
                byteBuffer[bytePos++] = (shortVal.toInt() and 0xFF).toByte()
                byteBuffer[bytePos++] = ((shortVal.toInt() shr 8) and 0xFF).toByte()
                // Right channel
                byteBuffer[bytePos++] = (shortVal.toInt() and 0xFF).toByte()
                byteBuffer[bytePos++] = ((shortVal.toInt() shr 8) and 0xFF).toByte()
            }

            fos.write(byteBuffer, 0, bytePos)
            sampleIndex += chunkLength
        }

        fos.flush()
        fos.close()

        // Fix header with accurate lengths
        val audioDataLength = totalSamples * NUM_CHANNELS * 2
        val overallSize = audioDataLength + 36

        val raf = RandomAccessFile(cacheFile, "rw")
        val header = createWavHeader(overallSize, audioDataLength, SAMPLE_RATE, NUM_CHANNELS, 16)
        raf.seek(0)
        raf.write(header)
        raf.close()

        return@withContext cacheFile
    }

    private fun createWavHeader(
        totalDataLen: Int,
        audioDataLen: Int,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int
    ): ByteArray {
        val byteRate = sampleRate * channels * (bitsPerSample / 8)
        val blockAlign = channels * (bitsPerSample / 8)
        val header = ByteArray(44)

        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // Sub-chunk 1 size (16 for PCM)
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // Audio format (1 for PCM)
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = blockAlign.toByte()
        header[33] = 0
        header[34] = bitsPerSample.toByte()
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (audioDataLen and 0xff).toByte()
        header[41] = ((audioDataLen shr 8) and 0xff).toByte()
        header[42] = ((audioDataLen shr 16) and 0xff).toByte()
        header[43] = ((audioDataLen shr 24) and 0xff).toByte()

        return header
    }
}
