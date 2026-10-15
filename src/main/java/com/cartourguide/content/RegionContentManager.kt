package com.cartourguide.content

import android.content.Context
import com.cartourguide.data.AppDatabase
import com.cartourguide.data.PlaceEntity
import com.cartourguide.data.SeedData
import com.cartourguide.data.StoryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Regions supported offline in MVP. Bundled JSON in assets/regions/*.json + seed fallback. */
@Serializable
data class RegionMeta(val id: String, val name: String, val version: Int, val approxKb: Int)

@Serializable
data class RegionBundle(
    val region: RegionMeta,
    val places: List<PlaceJson>,
    val stories: List<StoryJson>,
)
@Serializable
data class PlaceJson(val id: String, val name: String, val lat: Double, val lng: Double,
    val category: String, val importance: Int, val storyId: String)
@Serializable
data class StoryJson(val id: String, val placeId: String, val title: String, val textVi: String,
    val durationSec: Int, val sourceUrl: String?, val sourceName: String?, val verification: String)

sealed interface DownloadState {
    data object Idle : DownloadState
    data class Progress(val regionId: String, val pct: Int) : DownloadState
    data class Done(val regionId: String) : DownloadState
    data class Error(val regionId: String, val msg: String) : DownloadState
}

/**
 * Offline-first download manager. MVP source = bundled assets (works without internet).
 * Remote sync is an optional interface (not enabled by default, no paid API).
 * Supports: progress, cancel/retry (job cancel + re-run), corrupt handling (JSON validation),
 * storage estimate (approxKb), delete, offline-readiness signal.
 */
class RegionContentManager(
    private val ctx: Context,
    private val db: AppDatabase,
) {
    private val _state = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val state: StateFlow<DownloadState> = _state.asStateFlow()
    @Volatile private var cancelled = false

    val available = listOf(
        RegionMeta("sg-central", "Sài Gòn trung tâm", 1, 48),
        RegionMeta("sg-cu-chi", "Củ Chi", 1, 22),
        RegionMeta("sg-cangio", "Cần Giờ", 1, 20),
        RegionMeta("mekong-mytho", "Mỹ Tho – Mỹ Thuận", 1, 30),
        RegionMeta("mekong-cantho", "Cần Thơ", 1, 24),
    )

    fun cancel() { cancelled = true }

    suspend fun download(regionId: String, retry: Boolean = false): Result<Unit> {
        cancelled = false
        _state.value = DownloadState.Progress(regionId, 5)
        return try {
            // 1) Try bundled asset first (offline-capable path)
            val bundle = loadAssetBundle(regionId) ?: seedBundle(regionId)
                ?: return fail(regionId, "Không có dữ liệu vùng $regionId.")
            _state.value = DownloadState.Progress(regionId, 50)
            if (cancelled) return fail(regionId, "Đã hủy.")
            validate(bundle)?.let { return fail(regionId, it) }
            _state.value = DownloadState.Progress(regionId, 80)
            db.placeDao().upsertPlaces(bundle.places.map {
                PlaceEntity(it.id, it.name, it.lat, it.lng, regionId, it.category, it.importance, it.storyId)
            })
            db.placeDao().upsertStories(bundle.stories.map {
                StoryEntity(it.id, it.placeId, it.title, it.textVi, it.durationSec,
                    it.sourceUrl, it.sourceName, it.verification, bundle.region.version)
            })
            _state.value = DownloadState.Done(regionId)
            Result.success(Unit)
        } catch (e: Exception) {
            fail(regionId, "Lỗi: ${e.message}")
        }
    }

    suspend fun delete(regionId: String) { db.placeDao().deleteRegionPlaces(regionId); _state.value = DownloadState.Idle }

    suspend fun offlineReady(regionIds: Set<String>): Boolean {
        for (id in regionIds) if (db.placeDao().countFor(id) <= 0) return false
        return regionIds.isNotEmpty()
    }

    private fun fail(regionId: String, msg: String): Result<Unit> {
        _state.value = DownloadState.Error(regionId, msg)
        return Result.failure(IllegalStateException(msg))
    }

    private fun validate(b: RegionBundle): String? = RegionValidator.validate(b)

    private fun loadAssetBundle(regionId: String): RegionBundle? {
        return try {
            val bytes = ctx.assets.open("regions/$regionId.json").readBytes()
            Json { ignoreUnknownKeys = true }.decodeFromString<RegionBundle>(bytes.decodeToString())
        } catch (_: Exception) { null }
    }

    private fun seedBundle(regionId: String): RegionBundle? {
        val meta = available.firstOrNull { it.id == regionId } ?: return null
        val ps = SeedData.places.filter { it.regionId == regionId }
        if (ps.isEmpty()) return null
        val ids = ps.map { it.id }.toSet()
        val ss = SeedData.stories.filter { ids.contains(it.placeId) }
        return RegionBundle(meta,
            ps.map { PlaceJson(it.id, it.name, it.lat, it.lng, it.category, it.importance, it.storyId) },
            ss.map { StoryJson(it.id, it.placeId, it.title, it.textVi, it.durationSec, it.sourceUrl, it.sourceName, it.verification) })
    }
}
