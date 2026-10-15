package com.cartourguide.core

import com.cartourguide.model.GeoFix
import com.cartourguide.model.Place
import com.cartourguide.sim.SimRoute
import org.junit.Assert.*
import org.junit.Test

private fun place(id: String = "p1", lat: Double = 10.7770, lng: Double = 106.6953) =
    Place(id, "Test", lat, lng, "r", "di-tich", 5, "s1")

class TriggerEngineTest {

    @Test fun radius_identifiesCandidatesWithin1000m() {
        val e = TriggerEngine(TriggerConfig(radiusMeters = 1000.0, requiredConsecutiveHits = 1))
        val near = GeoFix(10.7775, 106.6953, 10f, 5f, 0f, 0L)
        assertEquals(1, e.candidates(near, listOf(place())).size)
        val far = GeoFix(10.7570, 106.6953, 10f, 5f, 0f, 0L) // ~2.2km
        assertEquals(0, e.candidates(far, listOf(place())).size)
    }

    @Test fun noisySingleFix_doesNotTrigger() {
        val e = TriggerEngine(TriggerConfig(radiusMeters = 1000.0, requiredConsecutiveHits = 2))
        val edge = GeoFix(10.7860, 106.6953, 60f, 8f, 180f, 0L) // ~1000m boundary
        assertTrue(e.candidates(edge, listOf(place())).isEmpty()) // 1st hit suppressed
        assertEquals(1, e.candidates(edge.copy(timeMs = 5000), listOf(place())).size) // 2nd confirms
    }

    @Test fun badAccuracy_suppressed() {
        val e = TriggerEngine(TriggerConfig(requiredConsecutiveHits = 1))
        val fix = GeoFix(10.7771, 106.6953, 500f, 5f, 0f, 0L)
        assertTrue(e.candidates(fix, listOf(place())).isEmpty())
    }

    @Test fun cooldown_suppressesRepeats() {
        val e = TriggerEngine(TriggerConfig(requiredConsecutiveHits = 1, placeCooldownMs = 60_000L))
        val fix = GeoFix(10.7771, 106.6953, 10f, 5f, 0f, 0L)
        assertEquals(1, e.candidates(fix, listOf(place())).size)
        e.markNarrated("p1", 0L)
        assertTrue(e.candidates(fix.copy(timeMs = 10_000), listOf(place())).isEmpty())
        assertEquals(1, e.candidates(fix.copy(timeMs = 61_000), listOf(place())).size)
    }

    @Test fun approaching_detected() {
        val e = TriggerEngine()
        val towardPalace = GeoFix(10.7700, 106.6953, 10f, 12f, 0f, 0L) // heading north, palace north
        assertEquals(true, e.isApproaching(towardPalace, place()))
        val away = GeoFix(10.7700, 106.6953, 10f, 12f, 180f, 0L)
        assertEquals(false, e.isApproaching(away, place()))
        assertNull(e.isApproaching(GeoFix(10.77, 106.69, 10f, 0.1f, null, 0L), place()))
    }

    @Test fun simJourney_approachedThenPassed_triggersOnce() {
        val e = TriggerEngine(TriggerConfig(radiusMeters = 1000.0, requiredConsecutiveHits = 2))
        val p = place()
        var triggerCount = 0
        for (f in SimRoute.fixes()) {
            if (e.candidates(f, listOf(p), f.timeMs).isNotEmpty()) {
                triggerCount++
                e.markNarrated(p.id, f.timeMs) // journey narrates once then cooldown suppresses
            }
        }
        assertEquals(1, triggerCount)
    }

    @Test fun stationaryJitter_noRepeatStorm() {
        val e = TriggerEngine(TriggerConfig(requiredConsecutiveHits = 2, placeCooldownMs = 3_600_000L))
        val p = place()
        var n = 0
        for (f in SimRoute.stationaryJitter(10.7771, 106.6953)) {
            if (e.candidates(f, listOf(p), f.timeMs).isNotEmpty()) { n++; e.markNarrated(p.id, f.timeMs) }
        }
        assertEquals(1, n)
    }
}
