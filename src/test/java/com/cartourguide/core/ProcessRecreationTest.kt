package com.cartourguide.core

import com.cartourguide.model.GeoFix
import com.cartourguide.model.Place
import org.junit.Assert.*
import org.junit.Test

/** Process recreation: cooldown snapshot survives restart, no replay storm. */
class ProcessRecreationTest {
    private val place = Place("p1", "Test", 10.7770, 106.6953, "r", "c", 5, "s1")

    @Test fun snapshotRestore_suppressesAlreadyNarrated() {
        val e1 = TriggerEngine(TriggerConfig(requiredConsecutiveHits = 1, placeCooldownMs = 3_600_000L))
        val fix = GeoFix(10.7771, 106.6953, 10f, 5f, 0f, 0L)
        assertEquals(1, e1.candidates(fix, listOf(place)).size)
        e1.markNarrated("p1", 0L)
        val snap = e1.snapshot()

        // Simulate process death: brand-new engine, restore persisted cooldowns
        val e2 = TriggerEngine(TriggerConfig(requiredConsecutiveHits = 1, placeCooldownMs = 3_600_000L))
        e2.restoreCooldowns(snap)
        assertTrue(e2.candidates(fix.copy(timeMs = 10_000), listOf(place)).isEmpty())

        // After cooldown expiry, narration allowed again
        assertEquals(1, e2.candidates(fix.copy(timeMs = 3_700_000), listOf(place)).size)
    }

    @Test fun queue_survivesInterruptionStorm_withoutOverlap() {
        val q = PlaybackQueue()
        q.enqueue(PlaybackQueue.Item("s1", "p1", "A"))
        q.enqueue(PlaybackQueue.Item("s2", "p2", "B"))
        q.next() // s1 playing
        q.pause(100L) // nav prompt
        q.resume() // prompt over
        q.pause(200L) // incoming call
        q.resume()
        assertTrue(q.isPlaying())
        assertEquals("s2", q.skip()?.storyId) // user skips; next starts, never two at once
        assertEquals(0, q.pending().size)
    }
}
