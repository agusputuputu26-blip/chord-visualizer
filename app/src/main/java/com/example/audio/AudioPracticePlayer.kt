package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class AudioPracticePlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var tickerJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionSec = MutableStateFlow(0f)
    val currentPositionSec: StateFlow<Float> = _currentPositionSec.asStateFlow()

    private val _durationSec = MutableStateFlow(0f)
    val durationSec: StateFlow<Float> = _durationSec.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _isLoopingSection = MutableStateFlow(false)
    val isLoopingSection: StateFlow<Boolean> = _isLoopingSection.asStateFlow()

    private var activeLoopStartSec: Float = 0f
    private var activeLoopEndSec: Float = Float.MAX_VALUE

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    fun loadFromFile(file: File) {
        stopTicker()
        mediaPlayer?.release()
        mediaPlayer = null
        _isReady.value = false

        try {
            val player = MediaPlayer()
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            player.setDataSource(file.absolutePath)
            player.prepare()
            setupPlayerCallbacks(player)
            mediaPlayer = player
            _durationSec.value = player.duration / 1000f
            _currentPositionSec.value = 0f
            _isReady.value = true
        } catch (e: Exception) {
            Log.e("AudioPracticePlayer", "Error loading file: ${file.name}", e)
        }
    }

    fun loadFromUri(uri: Uri) {
        stopTicker()
        mediaPlayer?.release()
        mediaPlayer = null
        _isReady.value = false

        try {
            val player = MediaPlayer()
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            player.setDataSource(context, uri)
            player.prepare()
            setupPlayerCallbacks(player)
            mediaPlayer = player
            _durationSec.value = player.duration / 1000f
            _currentPositionSec.value = 0f
            _isReady.value = true
        } catch (e: Exception) {
            Log.e("AudioPracticePlayer", "Error loading URI: $uri", e)
        }
    }

    private fun setupPlayerCallbacks(player: MediaPlayer) {
        player.setOnCompletionListener {
            if (_isLoopingSection.value && activeLoopEndSec > activeLoopStartSec) {
                seekTo(activeLoopStartSec)
                player.start()
                _isPlaying.value = true
            } else {
                _isPlaying.value = false
                _currentPositionSec.value = 0f
            }
        }
        player.setOnErrorListener { _, what, extra ->
            Log.e("AudioPracticePlayer", "MediaPlayer error: what=$what extra=$extra")
            _isPlaying.value = false
            true
        }
    }

    fun play() {
        val player = mediaPlayer ?: return
        try {
            applySpeed()
            player.start()
            _isPlaying.value = true
            startTicker()
        } catch (e: Exception) {
            Log.e("AudioPracticePlayer", "Failed to start playback", e)
        }
    }

    fun pause() {
        val player = mediaPlayer ?: return
        try {
            if (player.isPlaying) {
                player.pause()
            }
            _isPlaying.value = false
        } catch (e: Exception) {
            Log.e("AudioPracticePlayer", "Failed to pause playback", e)
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun seekTo(seconds: Float) {
        val player = mediaPlayer ?: return
        try {
            val clamped = seconds.coerceIn(0f, _durationSec.value)
            player.seekTo((clamped * 1000).toInt())
            _currentPositionSec.value = clamped
        } catch (e: Exception) {
            Log.e("AudioPracticePlayer", "Failed to seek", e)
        }
    }

    fun skipBy(deltaSeconds: Float) {
        seekTo(_currentPositionSec.value + deltaSeconds)
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        applySpeed()
    }

    private fun applySpeed() {
        val player = mediaPlayer ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val params = player.playbackParams
                params.speed = _playbackSpeed.value
                player.playbackParams = params
            } catch (e: Exception) {
                Log.e("AudioPracticePlayer", "Error setting playback params", e)
            }
        }
    }

    fun setLoopRange(startSec: Float, endSec: Float, enabled: Boolean) {
        activeLoopStartSec = startSec
        activeLoopEndSec = endSec
        _isLoopingSection.value = enabled
    }

    fun toggleLoop(startSec: Float, endSec: Float) {
        val newState = !_isLoopingSection.value
        setLoopRange(startSec, endSec, newState)
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                val player = mediaPlayer
                if (player != null && player.isPlaying) {
                    val pos = player.currentPosition / 1000f
                    _currentPositionSec.value = pos

                    // Section looping check
                    if (_isLoopingSection.value && activeLoopEndSec > activeLoopStartSec) {
                        if (pos >= activeLoopEndSec) {
                            seekTo(activeLoopStartSec)
                        }
                    }
                }
                delay(30)
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    fun release() {
        stopTicker()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignored
        }
        mediaPlayer = null
        _isPlaying.value = false
    }
}
