package com.cartourguide.location

import android.annotation.SuppressLint
import android.content.Context
import com.cartourguide.model.GeoFix
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Fused Location wrapper. Emits fixes; all radius math stays local (no network per update). */
class LocationMonitor(private val ctx: Context) {
    private val client: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(ctx)

    @SuppressLint("MissingPermission")
    fun fixes(intervalMs: Long = 5000L): Flow<GeoFix> = callbackFlow {
        val req = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, intervalMs)
            .setMinUpdateDistanceMeters(20f)
            .build()
        val cb = object : LocationCallback() {
            override fun onLocationResult(r: LocationResult) {
                for (l in r.locations) {
                    trySend(GeoFix(l.latitude, l.longitude,
                        if (l.hasAccuracy()) l.accuracy else null,
                        if (l.hasSpeed()) l.speed else null,
                        if (l.hasBearing()) l.bearing else null,
                        System.currentTimeMillis()))
                }
            }
        }
        try {
            client.requestLocationUpdates(req, cb, ctx.mainLooper)
        } catch (e: SecurityException) { close(e) }
        awaitClose { client.removeLocationUpdates(cb) }
    }
}
