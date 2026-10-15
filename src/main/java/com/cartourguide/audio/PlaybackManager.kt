package com.cartourguide.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import com.cartourguide.core.PlaybackQueue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Single queue-based playback manager. One entry point: [playText].
 * - Never overlapping: exactly one current item; queue holds the rest.
 * - Audio-focus loss (navigation prompt, call, duck request) pauses; never auto-resumes
 *   over a manual pause — user taps Tiếp tục.
 * - [onQueueAdvanced] lets the journey layer supply the next story text (DB lookup).
 */
class PlaybackManager(
    private val ctx: Context,
    private val tts: TtsSpeaker,
    /** Called when current story finishes and another item is waiting. */
    var onQueueAdvanced: (() -> Unit)? = null,
) {
    private val audioManager = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val queue = PlaybackQueue()

    data class UiState(
        val currentTitle: String? = null,
        val currentText: String? = null,
        val playing: Boolean = false,
    )
    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    private var focusRequest: AudioFocusRequest? = null
    private var currentText: String? = null
    private var currentRate: Float = 1.0f
    private var manualPause = false

    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
                pause(focusLost = true) // TTS cannot duck; pause instead of talking over nav
            AudioManager.AUDIOFOCUS_GAIN -> { /* stay paused; user resumes explicitly */ }
        }
    }

    fun requestFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= 26) {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(attrs).setOnAudioFocusChangeListener(focusListener).build()
            focusRequest = req
            audioManager.requestAudioFocus(req) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(focusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK) ==
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    /** Single playback entry point, called by the journey layer with resolved story text. */
    fun playText(item: PlaybackQueue.Item, text: String, rate: Float): Boolean {
        if (!queue.enqueue(item)) return false // duplicate of current/queued
        if (queue.current != null) return true // something playing; item waits its turn
        startItem(queue.next() ?: return false, text, rate)
        return true
    }

    private fun startItem(item: PlaybackQueue.Item, text: String, rate: Float) {
        currentText = text; currentRate = rate; manualPause = false
        if (!requestFocus()) return
        _ui.value = UiState(item.title, text, true)
        tts.speak(text, rate, item.storyId) {
            abandonFocus()
            val next = queue.next()
            if (next == null) {
                _ui.value = UiState(null, null, false)
            } else {
                _ui.value = UiState(null, null, false)
                onQueueAdvanced?.invoke() // journey layer calls playText() with next text
            }
        }
    }

    fun pause(focusLost: Boolean = false) {
        if (queue.current == null) return
        tts.stop()
        queue.pause(System.currentTimeMillis())
        if (!focusLost) manualPause = true
        _ui.value = _ui.value.copy(playing = false)
        if (!focusLost) abandonFocus()
    }

    fun resume() {
        val text = currentText ?: return
        val item = queue.current ?: return
        if (!requestFocus()) return
        manualPause = false
        _ui.value = _ui.value.copy(playing = true)
        tts.speak(text, currentRate, item.storyId + "#resume") {
            abandonFocus(); _ui.value = UiState(null, null, false)
        }
    }

    fun skip() {
        tts.stop(); abandonFocus()
        _ui.value = UiState(null, null, false)
        queue.skip()
        onQueueAdvanced?.invoke()
    }

    fun pendingCount(): Int = queue.pending().size
    fun isPlaying(): Boolean = queue.isPlaying()

    private fun abandonFocus() {
        if (Build.VERSION.SDK_INT >= 26) focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        else @Suppress("DEPRECATION") audioManager.abandonAudioFocus(focusListener)
    }
}
