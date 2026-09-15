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
     * Generates or retrieves a backing WAV audio file where guitar chords match 100% with the chords on screen.
     */
    suspend fun getOrCreateBackingTrack(context: Context, song: PracticeSong): File = withContext(Dispatchers.IO) {
        val cacheFile = File(context.cacheDir, "backing_${song.id}_v3.wav")
        if (cacheFile.exists() && cacheFile.length() > 44) {
            return@withContext cacheFile
        }

        val totalDurationSec = song.totalDurationSec.coerceAtLeast(10f)
        val totalSamples = (totalDurationSec * SAMPLE_RATE).toInt()

        val fos = FileOutputStream(cacheFile)
        // Placeholder 44-byte WAV header
        fos.write(ByteArray(44))

        val bufferSize = 8192
        val byteBuffer = ByteArray(bufferSize * 4)
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

                    val fingering = activeChord.fingering
                    if (fingering != null) {
                        // 4 beats per measure arpeggiation / strumming
                        val beatInMeasure = (chordSecOffset / secondsPerBeat) % 4.0
                        val currentBeatInt = beatInMeasure.toInt()
                        val beatFraction = beatInMeasure - currentBeatInt

                        if (currentBeatInt == 0) {
                            // Full acoustic guitar downward strum across all active strings
                            for (str in 0 until 6) {
                                val fret = fingering.frets[str]
                                if (fret >= 0) {
                                    val strumDelaySec = str * 0.012 // 12ms delay per string
                                    val noteAgeSec = (beatFraction * secondsPerBeat) - strumDelaySec
                                    if (noteAgeSec > 0.001) {
                                        val baseFreq = STRING_BASE_FREQS[str]
                                        val noteFreq = baseFreq * Math.pow(2.0, fret.toDouble() / 12.0)
                                        val decay = exp(-noteAgeSec * 2.8)
                                        val tone = (sin(2.0 * PI * noteFreq * noteAgeSec) +
                                                0.35 * sin(4.0 * PI * noteFreq * noteAgeSec) +
                                                0.15 * sin(6.0 * PI * noteFreq * noteAgeSec)) * decay
                                        mixedSample += tone * 0.22
                                    }
                                }
                            }
                        } else {
                            // Arpeggiated rhythm on beats 1, 2, 3
                            val stringToPluck = when (currentBeatInt) {
                                1 -> 2 // D or G string
                                2 -> 3 // G or B string
                                3 -> 4 // B or High e string
                                else -> 5
                            }
                            val fret = fingering.frets.getOrElse(stringToPluck) { 0 }
                            if (fret >= 0) {
                                val baseFreq = STRING_BASE_FREQS.getOrElse(stringToPluck) { 146.83 }
                                val noteFreq = baseFreq * Math.pow(2.0, fret.toDouble() / 12.0)
                                val noteAgeSec = beatFraction * secondsPerBeat

                                val decay = exp(-noteAgeSec * 3.2)
                                val tone = (sin(2.0 * PI * noteFreq * noteAgeSec) +
                                        0.3 * sin(4.0 * PI * noteFreq * noteAgeSec)) * decay
                                mixedSample += tone * 0.38
                            }
                        }

                        // Warm bass foundation for root note
                        val bassFret = fingering.frets.firstOrNull { it >= 0 } ?: 0
                        val bassStrIdx = fingering.frets.indexOfFirst { it >= 0 }.coerceAtLeast(0)
                        val bassFreq = STRING_BASE_FREQS[bassStrIdx] * Math.pow(2.0, bassFret.toDouble() / 12.0)
                        val bassAge = chordSecOffset
                        val bassDecay = exp(-bassAge * 1.5)
                        mixedSample += sin(2.0 * PI * bassFreq * bassAge) * bassDecay * 0.22
                    }
                }

                // Subtle acoustic metronome click on each beat to keep tempo locked
                val beatProgress = (currentSec / secondsPerBeat) % 1.0
                val tickAge = beatProgress * secondsPerBeat
                if (tickAge < 0.04) {
                    val clickDecay = exp(-tickAge * 120.0)
                    val click = sin(2.0 * PI * 2400.0 * tickAge) * clickDecay * 0.08
                    mixedSample += click
                }

                // Master gain limiter
                val clamped = (mixedSample * 0.72).coerceIn(-0.95, 0.95)
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
        raf.seek(0)
        // RIFF chunk
        raf.writeBytes("RIFF")
        raf.write(intToLittleEndianByteArray(overallSize))
        raf.writeBytes("WAVE")
        // fmt chunk
        raf.writeBytes("fmt ")
        raf.write(intToLittleEndianByteArray(16)) // Subchunk1Size
        raf.write(shortToLittleEndianByteArray(1)) // PCM format
        raf.write(shortToLittleEndianByteArray(NUM_CHANNELS.toShort()))
        raf.write(intToLittleEndianByteArray(SAMPLE_RATE))
        val byteRate = SAMPLE_RATE * NUM_CHANNELS * 2
        raf.write(intToLittleEndianByteArray(byteRate))
        val blockAlign = (NUM_CHANNELS * 2).toShort()
        raf.write(shortToLittleEndianByteArray(blockAlign))
        raf.write(shortToLittleEndianByteArray(16)) // Bits per sample
        // data chunk
        raf.writeBytes("data")
        raf.write(intToLittleEndianByteArray(audioDataLength))
        raf.close()

        cacheFile
    }

    private fun intToLittleEndianByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }

    private fun shortToLittleEndianByteArray(value: Short): ByteArray {
        return byteArrayOf(
            (value.toInt() and 0xFF).toByte(),
            ((value.toInt() shr 8) and 0xFF).toByte()
        )
    }
}
