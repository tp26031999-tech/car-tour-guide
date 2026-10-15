package com.cartourguide.auto

import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

/**
 * Android Auto entry point (media category).
 * Verified path: MediaLibraryService + MediaSession appears on car screen as a media app
 * alongside Google Maps navigation. Custom voice commands map to media controls:
 *  - Tạm dừng / Tiếp tục / Bỏ qua / Điểm tiếp theo = pause/resume/skip/next.
 *  - "Kể thêm" = custom command MORE_INFO (plays extended text when available).
 * Free-form hotword listening is NOT implemented (not supported for parked media apps;
 * continuous mic recording is out of scope for privacy + distraction rules).
 */
class TourMediaService : MediaLibraryService() {

    private var session: MediaLibrarySession? = null
    private var player: androidx.media3.exoplayer.ExoPlayer? = null

    override fun onCreate() {
        super.onCreate()
        // Transport-control player for the car screen. Actual narration audio is produced
        // by the phone TTS engine (audible over car Bluetooth); play/pause/skip presses
        // are forwarded to the real PlaybackManager via TourPlayerHolder.
        val exo = androidx.media3.exoplayer.ExoPlayer.Builder(this).build()
        player = exo
        exo.addListener(object : androidx.media3.common.Player.Listener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                if (playWhenReady) TourPlayerHolder.resume() else TourPlayerHolder.pause()
            }
        })
        session = MediaLibrarySession.Builder(this, exo, callback).build()
    }

    private val callback = object : MediaLibrarySession.Callback {
        override fun onGetLibraryRoot(s: MediaLibrarySession, b: MediaSession.ControllerInfo, p: LibraryParams?): ListenableFuture<LibraryResult<MediaItem>> {
            val root = MediaItem.Builder().setMediaId("root")
                .setMediaMetadata(MediaMetadata.Builder().setTitle("Chuyện quanh bạn").build()).build()
            return Futures.immediateFuture(LibraryResult.ofItem(root, p))
        }

        override fun onGetChildren(s: MediaLibrarySession, b: MediaSession.ControllerInfo,
            parentId: String, page: Int, pageSize: Int, p: LibraryParams?): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            val items = TourPlayerHolder.recentItems().map { (id, title) ->
                MediaItem.Builder().setMediaId(id)
                    .setMediaMetadata(MediaMetadata.Builder().setTitle(title).build()).build()
            }
            return Futures.immediateFuture(LibraryResult.ofItemList(ImmutableList.copyOf(items), p))
        }

        override fun onCustomCommand(s: MediaSession, c: MediaSession.ControllerInfo,
            command: androidx.media3.session.SessionCommand, args: Bundle): ListenableFuture<androidx.media3.session.SessionResult> {
            when (command.customAction) {
                "MORE_INFO" -> TourPlayerHolder.requestMoreInfo()
                "NEXT_PLACE" -> TourPlayerHolder.skip()
            }
            return Futures.immediateFuture(androidx.media3.session.SessionResult(androidx.media3.session.SessionResult.RESULT_SUCCESS))
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? = session

    override fun onDestroy() {
        session?.release(); session = null
        player?.release(); player = null
        super.onDestroy()
    }
}
