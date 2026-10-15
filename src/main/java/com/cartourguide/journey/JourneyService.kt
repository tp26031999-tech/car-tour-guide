package com.cartourguide.journey

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.cartourguide.R
import com.cartourguide.TourApp
import com.cartourguide.ui.MainActivity

/** Foreground service keeps location + narration alive with screen off (within OS limits). */
class JourneyService : LifecycleService() {

    override fun onCreate() {
        super.onCreate()
        (application as TourApp).ttsSpeaker.init()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startForegroundNotification()
        (application as TourApp).journeyManager.start()
        return START_STICKY
    }

    override fun onDestroy() {
        (application as TourApp).journeyManager.stop()
        super.onDestroy()
    }

    private fun startForegroundNotification() {
        val ch = "journey"
        val nm = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) nm.createNotificationChannel(
            NotificationChannel(ch, "Hành trình", NotificationManager.IMPORTANCE_LOW))
        val pi = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val n: Notification = NotificationCompat.Builder(this, ch)
            .setContentTitle("Đang dẫn tour")
            .setContentText("Nghe thuyết minh quanh bạn (bán kính đã cài). Bấm để mở.")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pi).setOngoing(true).build()
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(1, n)
        }
    }

    override fun onBind(intent: Intent) = super.onBind(intent)
}
