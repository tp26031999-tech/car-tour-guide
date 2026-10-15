package com.cartourguide.prefs

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.ds by preferencesDataStore("tour_prefs")

data class TourPrefs(
    val radiusMeters: Int = 1000,
    val minIntervalMin: Int = 3,
    val maxPerHour: Int = 8,
    val speechRate: Float = 1.0f,
    val autoNarration: Boolean = true,
    val downloadedRegions: Set<String> = emptySet(),
)

class PreferencesRepository(private val ctx: Context) {
    private val RADIUS = intPreferencesKey("radius_m")
    private val INTERVAL = intPreferencesKey("min_interval_min")
    private val MAXH = intPreferencesKey("max_per_hour")
    private val RATE = floatPreferencesKey("speech_rate")
    private val AUTO = booleanPreferencesKey("auto_narration")
    private val REGIONS = stringSetPreferencesKey("regions")

    val flow: Flow<TourPrefs> = ctx.ds.data.map {
        TourPrefs(
            radiusMeters = it[RADIUS] ?: 1000,
            minIntervalMin = it[INTERVAL] ?: 3,
            maxPerHour = it[MAXH] ?: 8,
            speechRate = it[RATE] ?: 1.0f,
            autoNarration = it[AUTO] ?: true,
            downloadedRegions = it[REGIONS] ?: emptySet(),
        )
    }

    suspend fun setRadius(m: Int) = ctx.ds.updateData { it.toMutablePreferences().apply { this[RADIUS] = m.coerceIn(200, 5000) }.toPreferences() }
    suspend fun setMinIntervalMin(v: Int) = ctx.ds.updateData { it.toMutablePreferences().apply { this[INTERVAL] = v.coerceIn(1, 30) }.toPreferences() }
    suspend fun setMaxPerHour(v: Int) = ctx.ds.updateData { it.toMutablePreferences().apply { this[MAXH] = v.coerceIn(1, 20) }.toPreferences() }
    suspend fun setSpeechRate(r: Float) = ctx.ds.updateData { it.toMutablePreferences().apply { this[RATE] = r.coerceIn(0.5f, 2.0f) }.toPreferences() }
    suspend fun setAuto(v: Boolean) = ctx.ds.updateData { it.toMutablePreferences().apply { this[AUTO] = v }.toPreferences() }
    suspend fun addRegion(id: String) = ctx.ds.updateData {
        it.toMutablePreferences().apply { this[REGIONS] = (this[REGIONS] ?: emptySet()) + id }.toPreferences()
    }
    suspend fun removeRegion(id: String) = ctx.ds.updateData {
        it.toMutablePreferences().apply { this[REGIONS] = (this[REGIONS] ?: emptySet()) - id }.toPreferences()
    }
}
