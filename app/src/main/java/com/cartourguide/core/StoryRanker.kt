package com.cartourguide.core

import com.cartourguide.model.Place

data class RankerConfig(
    val wImportance: Double = 3.0,
    val wDistance: Double = 2.0,
    val wApproaching: Double = 1.5,
    val wFreshness: Double = 2.5,
    val wDuration: Double = 0.5,
    val minIntervalBetweenStoriesMs: Long = 3 * 60 * 1000L,
    val maxStoriesPerHour: Int = 8,
    val preferredMaxDurationSec: Int = 150,
)

data class RankInput(
    val place: Place,
    val distanceM: Double,
    val approaching: Boolean?,
    val alreadyNarratedThisJourney: Boolean,
    val storyDurationSec: Int,
    val storyConfidence: Double, // 0..1 (provenance quality)
)

/**
 * Transparent scoring ranker. No network, no AI. Higher = better.
 * Returns null when nothing is worth narrating (silence is a valid outcome).
 */
class StoryRanker(private val config: RankerConfig = RankerConfig()) {
    fun getConfig() = config

    fun score(i: RankInput, radiusMeters: Double): Double {
        if (i.alreadyNarratedThisJourney) return Double.NEGATIVE_INFINITY
        if (i.storyConfidence < 0.4) return Double.NEGATIVE_INFINITY
        var s = 0.0
        s += config.wImportance * (i.place.importance / 5.0) * 10.0
        val closeness = (1.0 - (i.distanceM / radiusMeters).coerceIn(0.0, 1.0))
        s += config.wDistance * closeness * 10.0
        s += config.wApproaching * when (i.approaching) {
            true -> 10.0; false -> -4.0; null -> 2.0
        }
        s += config.wFreshness * (i.storyConfidence * 10.0)
        // Prefer short digestible stories while driving
        val over = (i.storyDurationSec - config.preferredMaxDurationSec).coerceAtLeast(0) / 60.0
        s -= config.wDuration * over * 5.0
        return s
    }

    /** Picks best candidate respecting global pacing limits. */
    fun pick(
        inputs: List<RankInput>,
        radiusMeters: Double,
        nowMs: Long,
        lastStoryAtMs: Long?,
        storiesLastHour: Int,
    ): RankInput? {
        if (lastStoryAtMs != null && nowMs - lastStoryAtMs < config.minIntervalBetweenStoriesMs) return null
        if (storiesLastHour >= config.maxStoriesPerHour) return null
        if (inputs.isEmpty()) return null
        val scored = inputs.map { it to score(it, radiusMeters) }.filter { it.second.isFinite() }
        if (scored.isEmpty()) return null
        val best = scored.maxByOrNull { it.second }!!
        if (best.second < 10.0) return null // quality gate: remain silent
        return best.first
    }
}
