package com.cartourguide.model

/** Verified local place. Coordinates: WGS-84 decimal degrees. */
data class Place(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val regionId: String,
    val category: String,
    val importance: Int, // 1..5, curated
    val storyId: String,
)

/** Curated story with provenance. Text is Southern-friendly Vietnamese. */
data class Story(
    val id: String,
    val placeId: String,
    val title: String,
    val textVi: String,
    val durationSecEstimate: Int,
    val sourceUrl: String?,
    val sourceName: String?,
    val verification: Verification,
    val contentVersion: Int,
)

enum class Verification { VERIFIED, LEGEND, NEEDS_CHECK }

data class GeoFix(
    val lat: Double,
    val lng: Double,
    val accuracyMeters: Float?,
    val speedMps: Float?,
    val bearingDeg: Float?,
    val timeMs: Long,
)
