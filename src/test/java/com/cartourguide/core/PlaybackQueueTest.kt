package com.cartourguide.core

import org.junit.Assert.*
import org.junit.Test

class PlaybackQueueTest {
    @Test fun ordering_fifoNoOverlap() {
        val q = PlaybackQueue()
        q.enqueue(PlaybackQueue.Item("s1", "p1", "A"))
        q.enqueue(PlaybackQueue.Item("s2", "p2", "B"))
        assertEquals("s1", q.next()?.storyId)
        assertTrue(q.isPlaying())
        assertEquals("s2", q.next()?.storyId) // previous replaced, never overlapping
    }

    @Test fun duplicateSuppressed() {
        val q = PlaybackQueue()
        assertTrue(q.enqueue(PlaybackQueue.Item("s1", "p1", "A")))
        assertFalse(q.enqueue(PlaybackQueue.Item("s1", "p1", "A")))
    }

    @Test fun pauseResume() {
        val q = PlaybackQueue()
        q.enqueue(PlaybackQueue.Item("s1", "p1", "A"))
        q.next()
        q.pause(123L)
        assertEquals(123L, q.pausedAtMs)
        assertFalse(q.isPlaying())
        q.resume()
        assertTrue(q.isPlaying())
    }

    @Test fun skip_advances() {
        val q = PlaybackQueue()
        q.enqueue(PlaybackQueue.Item("s1", "p1", "A"))
        q.enqueue(PlaybackQueue.Item("s2", "p2", "B"))
        q.next()
        assertEquals("s2", q.skip()?.storyId)
    }
}
