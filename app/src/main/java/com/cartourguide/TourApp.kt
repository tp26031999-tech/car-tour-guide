package com.cartourguide

import android.app.Application
import com.cartourguide.audio.PlaybackManager
import com.cartourguide.audio.TtsSpeaker
import com.cartourguide.content.RegionContentManager
import com.cartourguide.data.AppDatabase
import com.cartourguide.journey.JourneyStateManager
import com.cartourguide.location.LocationMonitor
import com.cartourguide.prefs.PreferencesRepository

/** Manual service locator to avoid adding a DI framework to the MVP. */
class TourApp : Application() {
    lateinit var prefs: PreferencesRepository; private set
    lateinit var db: AppDatabase; private set
    lateinit var locationMonitor: LocationMonitor; private set
    lateinit var journeyManager: JourneyStateManager; private set
    lateinit var ttsSpeaker: TtsSpeaker; private set
    lateinit var playbackManager: PlaybackManager; private set
    lateinit var regionManager: RegionContentManager; private set

    override fun onCreate() {
        super.onCreate()
        prefs = PreferencesRepository(this)
        db = AppDatabase.get(this)
        ttsSpeaker = TtsSpeaker(this, prefs)
        playbackManager = PlaybackManager(this, ttsSpeaker)
        locationMonitor = LocationMonitor(this)
        journeyManager = JourneyStateManager(prefs, db.placeDao(), locationMonitor, playbackManager)
        regionManager = RegionContentManager(this, db)
        // Car-screen buttons drive the real playback queue; "Kể thêm" replays extended info.
        com.cartourguide.auto.TourPlayerHolder.attach(playbackManager) {
            journeyManager.replayExtendedInfo()
        }
    }
}
