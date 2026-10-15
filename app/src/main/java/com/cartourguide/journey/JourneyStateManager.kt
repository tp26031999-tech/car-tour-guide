package com.cartourguide.journey

import com.cartourguide.audio.PlaybackManager
import com.cartourguide.core.*
import com.cartourguide.data.PlaceDao
import com.cartourguide.location.LocationMonitor
import com.cartourguide.model.GeoFix
import com.cartourguide.model.Place
import com.cartourguide.prefs.PreferencesRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/** Journey state: collects fixes, triggers candidates, ranks, narrates. Offline-first. */
class JourneyStateManager(
    private val prefs: PreferencesRepository,
    private val dao: PlaceDao,
    private val locations: LocationMonitor,
    private val playback: PlaybackManager,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    data class Status(
        val running: Boolean = false,
        val lastFix: GeoFix? = null,
        val nearbyCount: Int = 0,
        val lastStoryTitle: String? = null,
        val recentStoryIds: List<String> = emptyList(),
        val recentTitles: List<String> = emptyList(),
        val note: String? = null,
    )
    private val _status = MutableStateFlow(Status())
    val status: StateFlow<Status> = _status.asStateFlow()

    private var job: Job? = null
    private var trigger = TriggerEngine()
    private var ranker = StoryRanker()
    private var lastStoryAt: Long? = null
    private val storyTimes = ArrayDeque<Long>()
    private val narratedThisJourney = mutableSetOf<String>()
    private var placesCache: List<Place> = emptyList()
    var simFixes: Flow<GeoFix>? = null // injected by tests / demo mode

    fun start() {
        if (_status.value.running) return
        job = scope.launch {
            _status.value = _status.value.copy(running = true, note = null)
            // Load offline regions
            val cfg = firstPrefs()
            trigger = TriggerEngine(TriggerConfig(radiusMeters = cfg.radiusMeters.toDouble()))
            ranker = StoryRanker(RankerConfig(
                minIntervalBetweenStoriesMs = cfg.minIntervalMin * 60_000L,
                maxStoriesPerHour = cfg.maxPerHour))
            placesCache = dao.forRegions(cfg.downloadedRegions.ifEmpty { setOf("sg-central") }).map {
                Place(it.id, it.name, it.lat, it.lng, it.regionId, it.category, it.importance, it.storyId)
            }
            // Restore narrated history so a restarted process does not replay stories.
            narratedThisJourney.clear()
            val saved = dao.narratedIn(SCOPE)
            for (n in saved) {
                narratedThisJourney.add(n.placeId)
                trigger.markNarrated(n.placeId, n.narratedAtMs)
            }
            val flow = simFixes ?: locations.fixes()
            flow.collect { fix -> onFix(fix, cfg.radiusMeters.toDouble(), cfg.speechRate, cfg.autoNarration) }
        }
    }

    fun stop() { job?.cancel(); job = null; _status.value = _status.value.copy(running = false) }

    fun restartAfterDeath() { // process recreation: resume without replaying whole story
        val wasRunning = _status.value.running
        if (wasRunning) { stop(); start() }
    }

    private suspend fun firstPrefs() = prefs.flow.first()

    private suspend fun onFix(fix: GeoFix, radius: Double, rate: Float, auto: Boolean) {
        val cands = trigger.candidates(fix, placesCache, fix.timeMs)
        _status.value = _status.value.copy(lastFix = fix, nearbyCount = cands.size)
        if (!auto || cands.isEmpty()) return
        val now = fix.timeMs
        storyTimes.removeAll { now - it > 3_600_000L }
        val inputs = cands.mapNotNull { c ->
            val story = dao.storyForPlace(c.place.id) ?: return@mapNotNull null
            RankInput(c.place, c.distanceM, trigger.isApproaching(fix, c.place),
                narratedThisJourney.contains(c.place.id), story.durationSec,
                confidence(story.verification))
        }
        val pick = ranker.pick(inputs, radius, now, lastStoryAt, storyTimes.size) ?: return
        val story = dao.storyForPlace(pick.place.id) ?: return
        narratedThisJourney.add(pick.place.id)
        trigger.markNarrated(pick.place.id, now)
        dao.markNarrated(com.cartourguide.data.NarratedEntity(pick.place.id, SCOPE, now, story.id))
        lastStoryAt = now; storyTimes.addLast(now)
        playback.playText(PlaybackQueue.Item(story.id, pick.place.id, story.title), story.textVi, rate)
        com.cartourguide.auto.TourPlayerHolder.publishRecent(story.id, story.title)
        _status.value = _status.value.copy(lastStoryTitle = story.title,
            recentStoryIds = (_status.value.recentStoryIds + story.id).takeLast(20),
            recentTitles = (_status.value.recentTitles + story.title).takeLast(20))
    }

    private fun confidence(v: String) = when (v) { "VERIFIED" -> 1.0; "LEGEND" -> 0.7; else -> 0.5 }

    /** "Kể thêm": replay extended context for the last story (offline text, no AI). */
    fun replayExtendedInfo() {
        val lastId = _status.value.recentStoryIds.lastOrNull() ?: return
        scope.launch {
            val placeId = placesCache.firstOrNull { it.storyId == lastId }?.id ?: return@launch
            val story = dao.storyForPlace(placeId) ?: return@launch
            val extra = story.textVi + " Nguồn: ${story.sourceName ?: "tư liệu địa phương"}."
            playback.playText(PlaybackQueue.Item(story.id + "#more", placeId, story.title + " (kể thêm)"), extra, 1.0f)
        }
    }

    companion object { const val SCOPE = "default" }
}
