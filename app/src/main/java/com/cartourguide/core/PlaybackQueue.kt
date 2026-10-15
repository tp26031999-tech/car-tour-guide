package com.cartourguide.core

/** Single-queue playback logic (pure, testable). Never overlapping: one current item. */
class PlaybackQueue {
    data class Item(val storyId: String, val placeId: String, val title: String)

    private val queue = ArrayDeque<Item>()
    var current: Item? = null; private set
    var pausedAtMs: Long? = null; private set

    fun enqueue(item: Item): Boolean {
        if (current?.storyId == item.storyId) return false // duplicate of current
        if (queue.any { it.storyId == item.storyId }) return false
        queue.addLast(item)
        return true
    }

    fun next(): Item? {
        current = queue.removeFirstOrNull()
        pausedAtMs = null
        return current
    }

    fun pause(nowMs: Long) { if (current != null) pausedAtMs = nowMs }
    fun resume() { pausedAtMs = null }
    fun skip(): Item? = next()
    fun clear() { queue.clear(); current = null; pausedAtMs = null }
    fun pending(): List<Item> = queue.toList()
    fun isPlaying(): Boolean = current != null && pausedAtMs == null
}
