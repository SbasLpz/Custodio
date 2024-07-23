package com.miapp.custodio2.Utils

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Priority
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.miapp.custodio2.BotonesActivity
import com.miapp.custodio2.ClasesRequest.APIService
import com.miapp.custodio2.ClasesRequest.Registro
import com.miapp.custodio2.ClasesRequest.RegistroRes
import com.miapp.custodio2.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.notify
import org.json.JSONObject
import retrofit2.Retrofit

class LocationService: Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient
    var datos = Registro("-1","", "--", "--", "", "")
    val utils = RequestPermissions()
    val preferencias = Preferencias()
    var lata = "ssssss"

    val botonesActivity = BotonesActivity.ActivityHolder.botonesActivity

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = ServicioUbicacion(applicationContext, LocationServices.getFusedLocationProviderClient(applicationContext))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        utils.ServiceRunning = true
        println(" ****** onStartCommand ******")
        when(intent?.action){
            SERVICE_START -> empezar()
            SERVICE_STOP -> detener()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun empezar() {
        println(" A DARRLEEEEEEEEEEEEEE")
        val noti = Notification.Builder(this, "Mylocation")
            .setContentTitle("Comsi Custodio")
            .setContentText("Servicio activo")
            .setSmallIcon(R.drawable.logo_comsi)
            .setPriority(Notification.PRIORITY_DEFAULT)
            .setOngoing(true)
            .build()
        //val notiManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.notify(1, noti)
        //noti.flags = Notification.FLAG_ONGOING_EVENT

//100000L 10000L
        locationClient
            .getLocationUpdates(5000L)
            //.catch { e -> e.printStackTrace() }
            .catch {
                println("ALGO HA PASAOO :0")
            }
            .onEmpty {
                println("ALGO HA PASAOO :0 x12")
                if(botonesActivity != null) runBlocking {
                    val registro = Registro("0","Servicio Sin Ubicación", utils.getCurrentDate(), utils.latitude, utils.longitude, preferencias.getGlobalData(botonesActivity, "TM"))
                    //Aqui iba el mismo codigo de sendButtonData()
                    utils.doRequest(registro, botonesActivity)
                }
            }
            .onEach { location ->
                val lat = location.latitude.toString()
                val long = location.longitude.toString()
                println("AQUI TOY LAT $lat y LONG $long")
                lat0 = lat
                long0 = long
            }
            .launchIn(serviceScope)

        startForeground(1, noti)

        println("**** BUAAAAHHH !***")
        //startForeground(1, noti)
    }

    private fun detener() {
        stopForeground(true)
        stopSelf()
        println("------------  ----- APARCAO ---------------")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        println("-------  SERVICIO CANCELADO  -------")
    }

    companion object {
        const val SERVICE_START = "SERVICE_START"
        const val SERVICE_STOP = "SERVICE_STOP"
        var lat0 = ""
        var long0 = ""
    }



}