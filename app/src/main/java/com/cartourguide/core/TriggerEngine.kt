package com.cartourguide.core

import com.cartourguide.model.GeoFix
import com.cartourguide.model.Place

data class TriggerConfig(
    val radiusMeters: Double = 1000.0,
    /** Consecutive in-radius fixes required before candidacy (GPS-noise hysteresis). */
    val requiredConsecutiveHits: Int = 2,
    /** Per-place cooldown to avoid repeat narration in one journey. */
    val placeCooldownMs: Long = 6 * 60 * 60 * 1000L,
    /** Accuracy worse than this is treated as unreliable (candidate weight penalised). */
    val maxUsableAccuracyM: Float = 150f,
)

data class Candidate(val place: Place, val distanceM: Double)

/**
 * Deterministic trigger engine (pure logic).
 * Radius identifies CANDIDATES only; playback decision belongs to [StoryRanker].
 * Handles: GPS accuracy, brief boundary crossings (hysteresis via hit counters),
 * approach vs receding (bearing check helper), duplicate suppression via [seen].
 */
class TriggerEngine(private val config: TriggerConfig = TriggerConfig()) {

    private val hitCounts = mutableMapOf<String, Int>()
    private val lastNarratedAt = mutableMapOf<String, Long>()

    fun getConfig() = config

    /** Returns places within radius (expanded by accuracy) that pass hysteresis + cooldown. */
    fun candidates(fix: GeoFix, places: List<Place>, nowMs: Long = fix.timeMs): List<Candidate> {
        val out = mutableListOf<Candidate>()
        for (p in places) {
            val d = GeoUtils.haversineMeters(fix.lat, fix.lng, p.lat, p.lng)
            val accuracy = fix.accuracyMeters ?: 50f
            val effectiveRadius = config.radiusMeters + minOf(accuracy.toDouble(), 300.0) * 0.5
            val inside = d <= effectiveRadius
            val hits = if (inside) (hitCounts[p.id] ?: 0) + 1 else 0
            hitCounts[p.id] = hits
            if (!inside) continue
            if (hits < config.requiredConsecutiveHits) continue // noisy single fix
            if (accuracy > config.maxUsableAccuracyM) continue // too uncertain to trust
            val last = lastNarratedAt[p.id]
            if (last != null && nowMs - last < config.placeCooldownMs) continue
            out.add(Candidate(p, d))
        }
        return out.sortedBy { it.distanceM }
    }

    fun markNarrated(placeId: String, nowMs: Long) {
        lastNarratedAt[placeId] = nowMs
    }

    /** True if moving toward place (bearing aligned with travel bearing). Null bearing -> null. */
    fun isApproaching(fix: GeoFix, place: Place): Boolean? {
        val b = fix.bearingDeg ?: return null
        val speed = fix.speedMps ?: 0f
        if (speed < 1.0f) return null // parked / unreliable heading
        val toPlace = GeoUtils.bearingDeg(fix.lat, fix.lng, place.lat, place.lng)
        return GeoUtils.bearingDiff(b.toDouble(), toPlace) < 90.0
    }

    fun reset() { hitCounts.clear() }
    fun resetCooldowns() { lastNarratedAt.clear() }

    /** Snapshot for process-recreation tests and future persistence. */
    fun snapshot(): Map<String, Long> = lastNarratedAt.toMap()
    fun restoreCooldowns(saved: Map<String, Long>) {
        lastNarratedAt.clear(); lastNarratedAt.putAll(saved)
    }
}
