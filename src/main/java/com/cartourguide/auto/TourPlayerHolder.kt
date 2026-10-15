package com.cartourguide.auto

import com.cartourguide.audio.PlaybackManager

/**
 * Bridges car-screen media controls to the real [PlaybackManager].
 * Wired once from TourApp; Auto custom commands map to Vietnamese voice intents:
 *  "Tạm dừng"->pause, "Tiếp tục"->resume, "Bỏ qua"/"Điểm tiếp theo"->skip, "Kể thêm"->moreInfo.
 */
object TourPlayerHolder {
    private var playback: PlaybackManager? = null
    private var moreInfo: (() -> Unit)? = null
    private val recent = mutableListOf<Pair<String, String>>()

    fun attach(pm: PlaybackManager, onMoreInfo: () -> Unit) {
        playback = pm
        moreInfo = onMoreInfo
    }

    fun recentItems(): List<Pair<String, String>> = recent.toList()

    fun publishRecent(id: String, title: String) {
        recent.removeAll { it.first == id }
        recent.add(0, id to title)
    }

    fun pause() = playback?.pause() ?: Unit
    fun resume() = playback?.resume() ?: Unit
    fun skip() = playback?.skip() ?: Unit
    fun requestMoreInfo() { moreInfo?.invoke() }
}
