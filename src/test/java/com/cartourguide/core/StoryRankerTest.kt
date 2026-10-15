package com.cartourguide.core

import com.cartourguide.model.Place
import org.junit.Assert.*
import org.junit.Test

private fun inp(id: String, imp: Int, dist: Double, appr: Boolean?, narr: Boolean, dur: Int, conf: Double) =
    RankInput(Place(id, id, 0.0, 0.0, "r", "c", imp, "s"), dist, appr, narr, dur, conf)

class StoryRankerTest {
    private val ranker = StoryRanker()

    @Test fun prefersImportantCloseApproaching() {
        val a = inp("a", 5, 200.0, true, false, 70, 1.0)
        val b = inp("b", 2, 900.0, false, false, 300, 0.8)
        val pick = ranker.pick(listOf(a, b), 1000.0, 10_000L, null, 0)
        assertEquals("a", pick?.place?.id)
    }

    @Test fun silenceWhenNothingInteresting() {
        val low = inp("x", 1, 950.0, false, false, 600, 0.3)
        assertNull(ranker.pick(listOf(low), 1000.0, 10_000L, null, 0))
        assertNull(ranker.pick(emptyList(), 1000.0, 10_000L, null, 0))
    }

    @Test fun pacing_minIntervalRespected() {
        val a = inp("a", 5, 200.0, true, false, 70, 1.0)
        assertNull(ranker.pick(listOf(a), 1000.0, 60_000L, lastStoryAtMs = 0L, storiesLastHour = 0))
        assertNotNull(ranker.pick(listOf(a), 1000.0, 300_000L, lastStoryAtMs = 0L, storiesLastHour = 0))
    }

    @Test fun pacing_maxPerHour() {
        val a = inp("a", 5, 200.0, true, false, 70, 1.0)
        assertNull(ranker.pick(listOf(a), 1000.0, 10_000_000L, 0L, storiesLastHour = 8))
    }

    @Test fun alreadyNarrated_neverPicked() {
        val a = inp("a", 5, 100.0, true, true, 60, 1.0)
        assertNull(ranker.pick(listOf(a), 1000.0, 10_000L, null, 0))
    }
}
