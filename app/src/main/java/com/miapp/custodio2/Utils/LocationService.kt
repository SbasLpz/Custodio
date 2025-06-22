package com.miapp.custodio2.Utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.gms.location.LocationServices
import com.miapp.custodio2.BotonesActivity
import com.miapp.custodio2.ClasesRequest.Registro
import com.miapp.custodio2.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.runBlocking
import okhttp3.internal.notify

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
//        if (Build.VERSION.SDK_INT > 30){
//            val filter = IntentFilter().apply {
//                addAction(Intent.ACTION_SHUTDOWN)
//                addAction(Intent.ACTION_REBOOT)
//                addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
//            }
//
//            //registerReceiver(serviceStopReceiver, filter)
//            ContextCompat.registerReceiver(this, serviceStopReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
//        }
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

        //100000L 10000L
        locationClient
            .getLocationUpdates(5000L)
            //.catch { e -> e.printStackTrace() }
            .catch {
                println("ALGO HA PASAOO :0")
            }
            .onEmpty {
                println("ALGO HA PASAOO :0 x12")
//                if(botonesActivity != null) runBlocking {
//                    val registro = Registro("0","Servicio Sin Ubicación", utils.getCurrentDate(), utils.latitude, utils.longitude, preferencias.getGlobalData(botonesActivity, "TM"))
//                    //Aqui iba el mismo codigo de sendButtonData()
//                    utils.doRequest(registro, botonesActivity)
//                }
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

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        serviceScope.cancel()
        println("------- >30 SERVICIO CANCELADO  -------")
        var tipo = 0
        //aca iba usuario cerro la app
        var acc = "El usuario cerro la app"
        var date = utils.getCurrentDate()
        var latt =  lat0
        var longg = long0
        val tkn = preferencias.getGlobalDataWithContext(applicationContext, "TM")
        println("--- ♠♠♠♠ TKN with Context FROM SERVICE: ${tkn}")
        sendOnDestroyRegisger(tipo, acc, date, latt, longg, tkn)
    }

    companion object {
        const val SERVICE_START = "SERVICE_START"
        const val SERVICE_STOP = "SERVICE_STOP"
        var lat0 = ""
        var long0 = ""
    }

    fun sendOnDestroyRegisger(tipo: Int, acc:String, date:String, latt:String, longg:String, tkn:String) {
        val reApi = OneTimeWorkRequestBuilder<BotonesActivity.UpdloaWorker>()
            .setInputData(
                workDataOf(
                    "tipo" to tipo, "acc" to acc, "date" to date,
                    "latt" to latt, "longg" to longg, "tkn" to tkn
                ),
            )
        val workManager = WorkManager.getInstance(application)
        val continuation = workManager.beginWith(reApi.build())

        continuation.enqueue()
    }


}