package com.example.audio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import android.util.Log
import com.example.model.GuitarHeroNote
import com.example.model.GuitarHeroSectionMarker
import com.example.model.PracticeSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer

object GuitarHeroVideoExporter {

    private const val TAG = "GuitarHeroVideoExporter"
    private const val VIDEO_MIME = "video/avc"
    private const val FRAME_RATE = 30
    private const val BIT_RATE = 2_500_000 // 2.5 Mbps optimal compact 720p
    private const val I_FRAME_INTERVAL = 1

    data class ExportResult(
        val file: File,
        val durationSec: Float,
        val fileSizeFormatted: String
    )

    /**
     * Exports a 720p MP4 video of the Guitar Hero visualizer for the specified time range.
     */
    suspend fun exportVideo(
        context: Context,
        song: PracticeSong,
        notes: List<GuitarHeroNote>,
        sectionMarkers: List<GuitarHeroSectionMarker>,
        startTimeSec: Float,
        durationSec: Float,
        onProgress: (Int) -> Unit
    ): ExportResult = withContext(Dispatchers.IO) {
        val width = 720
        val height = 1280
        val totalFrames = (durationSec * FRAME_RATE).toInt().coerceAtLeast(30)

        val outputFile = File(
            context.cacheDir,
            "guitar_hero_${System.currentTimeMillis()}.mp4"
        )

        // Color palette for 5 lanes
        val laneColors = intArrayOf(
            android.graphics.Color.parseColor("#22C55E"), // Green
            android.graphics.Color.parseColor("#EF4444"), // Red
            android.graphics.Color.parseColor("#EAB308"), // Yellow
            android.graphics.Color.parseColor("#3B82F6"), // Blue
            android.graphics.Color.parseColor("#F97316")  // Orange
        )

        val format = MediaFormat.createVideoFormat(VIDEO_MIME, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
            setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE)
            setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL)
        }

        var encoder: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var trackIndex = -1
        var muxerStarted = false

        val frameBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(frameBitmap)
        val yuvBuffer = ByteArray(width * height * 3 / 2)

        try {
            encoder = MediaCodec.createEncoderByType(VIDEO_MIME)
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoder.start()

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            val bufferInfo = MediaCodec.BufferInfo()
            val lookAheadSec = 2.0f

            for (frameIdx in 0 until totalFrames) {
                val currentTimestampSec = startTimeSec + (frameIdx.toFloat() / FRAME_RATE)

                // 1. Render Guitar Hero frame onto canvas
                renderFrame(
                    canvas = canvas,
                    width = width,
                    height = height,
                    song = song,
                    notes = notes,
                    sectionMarkers = sectionMarkers,
                    currentTimeSec = currentTimestampSec,
                    lookAheadSec = lookAheadSec,
                    laneColors = laneColors
                )

                // 2. Convert Bitmap to NV12 YUV
                convertBitmapToNV12(frameBitmap, yuvBuffer, width, height)

                // 3. Feed input buffer to MediaCodec
                var inputBufferIndex = encoder.dequeueInputBuffer(10000)
                while (inputBufferIndex < 0) {
                    drainEncoder(encoder, muxer, bufferInfo, trackIndex) { newIndex ->
                        trackIndex = newIndex
                        muxerStarted = true
                    }
                    inputBufferIndex = encoder.dequeueInputBuffer(10000)
                }

                val inputBuffer: ByteBuffer? = encoder.getInputBuffer(inputBufferIndex)
                inputBuffer?.clear()
                inputBuffer?.put(yuvBuffer)

                val presentationTimeUs = (frameIdx * 1_000_000L) / FRAME_RATE
                val flags = if (frameIdx == totalFrames - 1) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0
                encoder.queueInputBuffer(inputBufferIndex, 0, yuvBuffer.size, presentationTimeUs, flags)

                // 4. Drain encoder output to muxer
                drainEncoder(encoder, muxer, bufferInfo, trackIndex) { newIndex ->
                    trackIndex = newIndex
                    muxerStarted = true
                }

                // Progress update
                val progressPercent = ((frameIdx + 1) * 100) / totalFrames
                onProgress(progressPercent)
            }

            // Finish draining until EOS
            var eosReached = false
            var drainTries = 0
            while (!eosReached && drainTries < 50) {
                val outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000)
                if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    drainTries++
                } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    if (!muxerStarted) {
                        trackIndex = muxer.addTrack(encoder.outputFormat)
                        muxer.start()
                        muxerStarted = true
                    }
                } else if (outputBufferIndex >= 0) {
                    val encodedData = encoder.getOutputBuffer(outputBufferIndex)
                    if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                    }
                    encoder.releaseOutputBuffer(outputBufferIndex, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        eosReached = true
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error encoding video", e)
            throw e
        } finally {
            try {
                encoder?.stop()
                encoder?.release()
            } catch (e: Exception) {
                // Ignore
            }
            try {
                if (muxerStarted) {
                    muxer?.stop()
                }
                muxer?.release()
            } catch (e: Exception) {
                // Ignore
            }
            frameBitmap.recycle()
        }

        val sizeMb = String.format("%.1f MB", outputFile.length().toDouble() / (1024 * 1024))
        return@withContext ExportResult(
            file = outputFile,
            durationSec = durationSec,
            fileSizeFormatted = sizeMb
        )
    }

    private fun drainEncoder(
        encoder: MediaCodec,
        muxer: MediaMuxer,
        bufferInfo: MediaCodec.BufferInfo,
        currentTrackIndex: Int,
        onMuxerStart: (Int) -> Unit
    ) {
        var trackIdx = currentTrackIndex
        while (true) {
            val outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, 0)
            if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (trackIdx < 0) {
                    trackIdx = muxer.addTrack(encoder.outputFormat)
                    muxer.start()
                    onMuxerStart(trackIdx)
                }
            } else if (outputBufferIndex >= 0) {
                val encodedData = encoder.getOutputBuffer(outputBufferIndex)
                if (encodedData != null && bufferInfo.size > 0 && trackIdx >= 0) {
                    encodedData.position(bufferInfo.offset)
                    encodedData.limit(bufferInfo.offset + bufferInfo.size)
                    muxer.writeSampleData(trackIdx, encodedData, bufferInfo)
                }
                encoder.releaseOutputBuffer(outputBufferIndex, false)
            }
        }
    }

    private fun renderFrame(
        canvas: Canvas,
        width: Int,
        height: Int,
        song: PracticeSong,
        notes: List<GuitarHeroNote>,
        sectionMarkers: List<GuitarHeroSectionMarker>,
        currentTimeSec: Float,
        lookAheadSec: Float,
        laneColors: IntArray
    ) {
        // Clear background
        canvas.drawColor(android.graphics.Color.parseColor("#090D16"))

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Draw Header HUD
        paint.color = android.graphics.Color.parseColor("#131B2E")
        canvas.drawRect(0f, 0f, width.toFloat(), 130f, paint)

        paint.color = android.graphics.Color.WHITE
        paint.textSize = 34f
        paint.isFakeBoldText = true
        canvas.drawText("GUITAR HERO PRACTICE", 32f, 60f, paint)

        val currentMarker = sectionMarkers.lastOrNull { it.timestampSec <= currentTimeSec }
        val sectionName = currentMarker?.name ?: "Latihan"
        paint.color = android.graphics.Color.parseColor("#F59E0B")
        paint.textSize = 26f
        paint.isFakeBoldText = false
        val timeStr = String.format("%02d:%02d", (currentTimeSec / 60).toInt(), (currentTimeSec % 60).toInt())
        canvas.drawText("$sectionName  •  $timeStr  •  ${song.bpm} BPM", 32f, 105f, paint)

        // Highway parameters
        val highwayTopY = 160f
        val highwayBottomY = height - 90f
        val hitLineY = height - 160f

        val topWidth = width * 0.45f
        val bottomWidth = width * 0.92f

        val topX = (width - topWidth) / 2f
        val bottomX = (width - bottomWidth) / 2f

        // Draw Highway Surface
        val highwayPath = Path().apply {
            moveTo(topX, highwayTopY)
            lineTo(topX + topWidth, highwayTopY)
            lineTo(bottomX + bottomWidth, highwayBottomY)
            lineTo(bottomX, highwayBottomY)
            close()
        }

        paint.shader = LinearGradient(
            0f, highwayTopY, 0f, highwayBottomY,
            android.graphics.Color.parseColor("#0F172A"),
            android.graphics.Color.parseColor("#1E293B"),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(highwayPath, paint)
        paint.shader = null

        // Draw Lane dividers (5 lanes = 6 lines)
        for (i in 0..5) {
            val fraction = i / 5.0f
            val lineTopX = topX + (topWidth * fraction)
            val lineBottomX = bottomX + (bottomWidth * fraction)

            paint.color = if (i == 0 || i == 5) android.graphics.Color.parseColor("#38BDF8") else android.graphics.Color.parseColor("#33475569")
            paint.strokeWidth = if (i == 0 || i == 5) 4f else 2f
            canvas.drawLine(lineTopX, highwayTopY, lineBottomX, highwayBottomY, paint)
        }

        // Draw Scrolling Beat Bars
        val secPerBeat = 60.0f / song.bpm
        val beatOffset = (currentTimeSec % secPerBeat) / secPerBeat
        paint.strokeWidth = 2f
        paint.color = android.graphics.Color.parseColor("#3364748B")
        for (b in 0..6) {
            val barProgress = ((b.toFloat() - beatOffset) / 6.0f).coerceIn(0f, 1f)
            val y = highwayTopY + (hitLineY - highwayTopY) * barProgress
            val currentWidth = topWidth + (bottomWidth - topWidth) * barProgress
            val currentLeft = (width - currentWidth) / 2f
            canvas.drawLine(currentLeft, y, currentLeft + currentWidth, y, paint)
        }

        // Draw Section Marker Banners
        sectionMarkers.forEach { marker ->
            val timeDelta = marker.timestampSec - currentTimeSec
            if (timeDelta in -0.5f..lookAheadSec) {
                val progress = (1.0f - (timeDelta / lookAheadSec)).coerceIn(0f, 1f)
                val y = highwayTopY + (hitLineY - highwayTopY) * progress
                val currentWidth = topWidth + (bottomWidth - topWidth) * progress
                val currentLeft = (width - currentWidth) / 2f

                paint.color = marker.colorHex.toInt()
                paint.style = Paint.Style.FILL
                val rect = RectF(currentLeft + 8f, y - 18f, currentLeft + currentWidth - 8f, y + 18f)
                canvas.drawRoundRect(rect, 14f, 14f, paint)

                paint.color = android.graphics.Color.WHITE
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f
                canvas.drawRoundRect(rect, 14f, 14f, paint)

                paint.style = Paint.Style.FILL
                paint.textSize = 22f
                paint.isFakeBoldText = true
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("★  ${marker.name.uppercase()}  ★", width / 2f, y + 7f, paint)
                paint.textAlign = Paint.Align.LEFT
            }
        }

        // Draw Falling Notes
        val visibleNotes = notes.filter { note ->
            val timeDelta = note.timestampSec - currentTimeSec
            timeDelta in -0.2f..lookAheadSec
        }

        visibleNotes.forEach { note ->
            val timeDelta = note.timestampSec - currentTimeSec
            val progress = (1.0f - (timeDelta / lookAheadSec)).coerceIn(0f, 1.05f)

            val y = highwayTopY + (hitLineY - highwayTopY) * progress
            val currentWidth = topWidth + (bottomWidth - topWidth) * progress
            val currentLeft = (width - currentWidth) / 2f
            val laneWidth = currentWidth / 5.0f

            val noteCenterX = currentLeft + (laneWidth * (note.lane + 0.5f))
            val noteRadius = 14f + (progress * 22f)

            val noteColor = laneColors[note.lane.coerceIn(0, 4)]

            // Note glow
            paint.style = Paint.Style.FILL
            paint.color = (noteColor and 0x00FFFFFF) or 0x66000000
            canvas.drawCircle(noteCenterX, y, noteRadius * 1.4f, paint)

            // 3D Gem Note
            paint.shader = RadialGradient(
                noteCenterX - (noteRadius * 0.3f), y - (noteRadius * 0.3f),
                noteRadius,
                intArrayOf(android.graphics.Color.WHITE, noteColor),
                null,
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(noteCenterX, y, noteRadius, paint)
            paint.shader = null

            // Gem white border
            paint.color = android.graphics.Color.WHITE
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            canvas.drawCircle(noteCenterX, y, noteRadius, paint)

            // Inner dark center
            paint.color = android.graphics.Color.parseColor("#0F172A")
            paint.style = Paint.Style.FILL
            canvas.drawCircle(noteCenterX, y, noteRadius * 0.35f, paint)
        }

        // Draw Hit Line (Garis Sasaran)
        paint.color = android.graphics.Color.parseColor("#F59E0B")
        paint.strokeWidth = 5f
        canvas.drawLine(bottomX, hitLineY, bottomX + bottomWidth, hitLineY, paint)

        // Draw Target Receptors
        val targetLaneWidth = bottomWidth / 5.0f
        for (lane in 0..4) {
            val targetCenterX = bottomX + (targetLaneWidth * (lane + 0.5f))
            val targetRadius = 30f
            val baseColor = laneColors[lane]

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 5f
            paint.color = baseColor
            canvas.drawCircle(targetCenterX, hitLineY, targetRadius, paint)

            paint.style = Paint.Style.FILL
            paint.color = android.graphics.Color.parseColor("#090D16")
            canvas.drawCircle(targetCenterX, hitLineY, targetRadius * 0.7f, paint)
        }
    }

    /**
     * Fast software conversion from ARGB_8888 Bitmap to NV12 (YUV420 semi-planar).
     */
    private fun convertBitmapToNV12(bitmap: Bitmap, nv12: ByteArray, width: Int, height: Int) {
        val argb = IntArray(width * height)
        bitmap.getPixels(argb, 0, width, 0, 0, width, height)

        val frameSize = width * height
        var yIndex = 0
        var uvIndex = frameSize

        for (j in 0 until height) {
            for (i in 0 until width) {
                val pixel = argb[j * width + i]
                val r = (pixel shr 16) and 0xff
                val g = (pixel shr 8) and 0xff
                val b = pixel and 0xff

                // RGB to Y
                var y = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
                y = y.coerceIn(16, 235)
                nv12[yIndex++] = y.toByte()

                // RGB to UV (subsampled 2x2)
                if (j % 2 == 0 && i % 2 == 0) {
                    var u = ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                    var v = ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128
                    u = u.coerceIn(16, 240)
                    v = v.coerceIn(16, 240)
                    nv12[uvIndex++] = u.toByte()
                    nv12[uvIndex++] = v.toByte()
                }
            }
        }
    }
}
