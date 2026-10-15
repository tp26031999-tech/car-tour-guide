package com.cartourguide.core

import org.junit.Assert.*
import org.junit.Test

class GeoUtilsTest {
    @Test fun haversine_hcmcPalaceToBenThanh_about400m() {
        val d = GeoUtils.haversineMeters(10.7770, 106.6953, 10.7725, 106.6980)
        assertTrue("expected ~400-800m, got $d", d in 300.0..900.0)
    }

    @Test fun haversine_samePoint_zero() {
        assertEquals(0.0, GeoUtils.haversineMeters(10.0, 106.0, 10.0, 106.0), 0.001)
    }

    @Test fun triggerRadius_1000mBoundary() {
        // ~1000m north of palace ≈ 0.009 deg latitude
        val d = GeoUtils.haversineMeters(10.7770, 106.6953, 10.7860, 106.6953)
        assertTrue("expected ~1000m, got $d", d in 900.0..1100.0)
    }

    @Test fun bearing_northIsZero() {
        val b = GeoUtils.bearingDeg(10.0, 106.0, 11.0, 106.0)
        assertTrue(b < 5 || b > 355)
    }
}
