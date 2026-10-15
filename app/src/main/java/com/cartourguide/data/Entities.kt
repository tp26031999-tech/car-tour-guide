package com.cartourguide.data

import androidx.room.*

@Entity(tableName = "places")
data class PlaceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val regionId: String,
    val category: String,
    val importance: Int,
    val storyId: String,
)

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val placeId: String,
    val title: String,
    val textVi: String,
    val durationSec: Int,
    val sourceUrl: String?,
    val sourceName: String?,
    val verification: String,
    val contentVersion: Int,
)

@Dao
interface PlaceDao {
    @Query("SELECT * FROM places WHERE regionId IN (:regionIds)")
    suspend fun forRegions(regionIds: Set<String>): List<PlaceEntity>

    @Query("SELECT * FROM places")
    suspend fun all(): List<PlaceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlaces(p: List<PlaceEntity>)

    @Query("SELECT * FROM stories WHERE id = :id LIMIT 1")
    suspend fun story(id: String): StoryEntity?

    @Query("SELECT * FROM stories WHERE placeId = :placeId LIMIT 1")
    suspend fun storyForPlace(placeId: String): StoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStories(s: List<StoryEntity>)

    @Query("DELETE FROM places WHERE regionId = :regionId")
    suspend fun deleteRegionPlaces(regionId: String)

    @Query("SELECT COUNT(*) FROM places WHERE regionId = :regionId")
    suspend fun countFor(regionId: String): Int

    // --- Narrated history (survives process death; per-place cooldown) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markNarrated(n: NarratedEntity)

    @Query("SELECT * FROM narrated WHERE journeyScope = :scope")
    suspend fun narratedIn(scope: String): List<NarratedEntity>

    @Query("DELETE FROM narrated WHERE journeyScope = :scope")
    suspend fun clearNarrated(scope: String)
}

/** Survives process death so a restarted app does not replay the same stories. */
@Entity(tableName = "narrated")
data class NarratedEntity(
    @PrimaryKey val placeId: String,
    val journeyScope: String, // e.g. "default" MVP single-scope; future: per-day id
    val narratedAtMs: Long,
    val storyId: String,
)
