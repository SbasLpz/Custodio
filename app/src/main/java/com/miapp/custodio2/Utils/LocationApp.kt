package com.miapp.custodio2.Utils

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class LocationApp: Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val canal = NotificationChannel(
                "Mylocation",
                "Ubicación",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val notiManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notiManager.createNotificationChannel(canal)
        }
    }
}