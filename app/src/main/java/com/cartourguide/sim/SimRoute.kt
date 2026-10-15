package com.cartourguide.sim

import com.cartourguide.model.GeoFix

/**
 * Simulated GPS route for automated tests + demo (no device movement needed).
 * Route: approaches Dinh Doc Lap from ~2km away, passes by, with noise near the 1000m boundary.
 * Timestamps are synthetic (1s apart); tests override with controlled clocks where needed.
 */
object SimRoute {
    // Center near Independence Palace 10.7770,106.6953
    fun fixes(startMs: Long = 0L): List<GeoFix> {
        val pts = mutableListOf<GeoFix>()
        var t = startMs
        // Start ~2.2km south, drive north toward palace, then continue north past it
        val lats = listOf(
            10.7570, 10.7600, 10.7630, 10.7660, 10.7680, // far (>1000m)
            10.7695, 10.7710, 10.7730, 10.7750, 10.7765, // entering ~1000m boundary w/ jitter
            10.7770, 10.7785, 10.7805, 10.7830, 10.7860, // passing + leaving
        )
        val lng = 106.6953
        for ((i, la) in lats.withIndex()) {
            val jitter = if (i in 5..8) (if (i % 2 == 0) 0.0004 else -0.0004) else 0.0
            pts.add(GeoFix(la + jitter, lng, 12f, 11f, 0f, t))
            t += 30_000L
        }
        return pts
    }

    /** Same place repeated (parking jitter) to test duplicate suppression. */
    fun stationaryJitter(lat: Double, lng: Double, n: Int = 10, startMs: Long = 0L): List<GeoFix> {
        return (0 until n).map { i ->
            GeoFix(lat + (if (i % 2 == 0) 0.0001 else -0.0001), lng, 15f, 0.2f, null, startMs + i * 10_000L)
        }
    }
}
