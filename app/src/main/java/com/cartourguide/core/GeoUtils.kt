package com.cartourguide.core

import kotlin.math.*

/** Pure geographic math. Independently testable, no Android deps. */
object GeoUtils {
    private const val EARTH_M = 6371000.0

    fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2.0)
        return 2 * EARTH_M * asin(sqrt(a))
    }

    /** Bearing from a->b in degrees [0,360). */
    fun bearingDeg(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val la1 = Math.toRadians(lat1)
        val la2 = Math.toRadians(lat2)
        val dLng = Math.toRadians(lng2 - lng1)
        val y = sin(dLng) * cos(la2)
        val x = cos(la1) * sin(la2) - sin(la1) * cos(la2) * cos(dLng)
        return (Math.toDegrees(atan2(y, x)) + 360) % 360
    }

    /** Smallest absolute bearing difference in degrees [0,180]. */
    fun bearingDiff(a: Double, b: Double): Double {
        val d = ((a - b + 540) % 360) - 180
        return abs(d)
    }
}
