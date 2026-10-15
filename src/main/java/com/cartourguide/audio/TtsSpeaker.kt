package com.cartourguide.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.cartourguide.prefs.PreferencesRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*

/** Device TTS wrapper with Vietnamese capability check + voice preview (parked). */
class TtsSpeaker(
    private val ctx: Context,
    private val prefs: PreferencesRepository,
) {
    private var tts: TextToSpeech? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    data class VoiceState(
        val ready: Boolean = false,
        val vietnameseSupported: Boolean = false,
        val engine: String? = null,
        val error: String? = null,
    )
    private val _state = MutableStateFlow(VoiceState())
    val state: StateFlow<VoiceState> = _state.asStateFlow()

    fun init() {
        tts?.shutdown()
        tts = TextToSpeech(ctx) { status ->
            if (status != TextToSpeech.SUCCESS) {
                _state.value = VoiceState(false, false, null, "TTS init failed: $status")
                return@TextToSpeech
            }
            val t = tts ?: return@TextToSpeech
            val vi = t.setLanguage(Locale("vi", "VN"))
            val ok = vi == TextToSpeech.LANG_AVAILABLE || vi == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
                vi == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
            // Do NOT claim Southern accent: stock engines rarely expose it. Report engine truthfully.
            _state.value = VoiceState(true, ok, t.currentEngine, if (ok) null else "Chưa có giọng tiếng Việt. Cài Google TTS + dữ liệu tiếng Việt.")
        }
    }

    suspend fun preview(text: String = "Xin chào! Mình là hướng dẫn viên của bạn. Chúc thượng lộ bình an.", rate: Float = 1.0f) {
        speak(text, rate, utteranceId = "preview")
    }

    /** Installed TTS engines for the voice-selection setting (e.g. com.google.android.tts). */
    fun listEngines(): List<String> = try {
        tts?.engines?.map { it.name } ?: emptyList()
    } catch (_: Exception) { emptyList() }

    fun speak(text: String, rate: Float, utteranceId: String, onDone: (() -> Unit)? = null) {
        val t = tts ?: run { onDone?.invoke(); return }
        t.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
        t.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}
            override fun onDone(id: String?) { scope.launch(Dispatchers.Main) { onDone?.invoke() } }
            override fun onError(id: String?) { scope.launch(Dispatchers.Main) { onDone?.invoke() } }
        })
        val params = Bundle()
        t.speak(text, TextToSpeech.QUEUE_ADD, params, utteranceId)
    }

    fun stop() { tts?.stop() }
    fun shutdown() { tts?.shutdown(); tts = null; scope.cancel() }
}

/**
 * Pure voice-capability logic (testable without a device).
 * Codes mirror TextToSpeech.setLanguage() results: 0+=OK, -1 missing data, -2 not supported.
 * Never labels a voice "Southern" — only reports Vietnamese availability truthfully.
 */
object VoiceCheck {
    fun vietnameseSupported(langStatus: Int): Boolean = langStatus >= 0

    fun userMessage(langStatus: Int): String? = when {
        langStatus >= 0 -> null
        langStatus == -1 -> "Thiếu dữ liệu giọng tiếng Việt. Cài Google TTS + gói tiếng Việt rồi thử lại lúc đang đậu."
        else -> "Máy chưa hỗ trợ giọng tiếng Việt. Dùng audio tải sẵn hoặc đổi máy có Google TTS."
    }
}
