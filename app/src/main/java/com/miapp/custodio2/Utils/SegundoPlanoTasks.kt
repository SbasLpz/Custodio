package com.miapp.custodio2.Utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.miapp.custodio2.BotonesActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SegundoPlanoTasks: Service() {
    val utils = RequestPermissions()
    val channelID = "2024"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        crearCanal()
        println("HOLA MUY WENAS")
        //utils.startLocationUpdates2(this@SegundoPlanoTasks)
        startForeground(1, getNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        println("// HOLA MUY WENAS //")
        return null
    }

    private fun crearCanal(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val serviceChannel = NotificationChannel(channelID, "Servicio Localizacion", NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun getNotification(): Notification {
        val notificationIntent = Intent(this, BotonesActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(this, channelID)
            .setContentTitle("Location Service")
            .setContentText("Getting location updates")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        }
        return builder.build()
    }
}