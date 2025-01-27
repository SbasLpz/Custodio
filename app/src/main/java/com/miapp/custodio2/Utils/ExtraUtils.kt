package com.miapp.custodio2.Utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.miapp.custodio2.BotonesActivity
import com.miapp.custodio2.ClasesRequest.APIService
import com.miapp.custodio2.ClasesRequest.Registro
import com.miapp.custodio2.ClasesRequest.RegistroRes
import com.miapp.custodio2.PanelActivity
import com.miapp.custodio2.R
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Retrofit

class ExtraUtils: FirebaseMessagingService() {

    var accion:String? = ""
    val utils = RequestPermissions()
    val preferencias = Preferencias()
    var datos = Registro("-1", accion!!, utils.getCurrentDate(), "--", "--", "")
    private lateinit var checker: Checker

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        println("BBBBBBBBBBBBBBBUUUUUUUUUUUUUUUUUUUUBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB")
        utils.received++

        val botonesActivity = BotonesActivity.ActivityHolder.botonesActivity

        accion = message.data.get("action")
        if (accion == "ACTION_LOCATE_DEVICE"){
            println("TENGO QUE ENVIAR LA UBICACIOOOON !!!! - onMessage")

            if (utils.isServiceRunning(this, LocationService::class.java)){
                //println("77777777 SERVICIO CORRIENDO 77777777")
//                if(botonesActivity != null) runBlocking {
//                    val registro = Registro("0","Servicio ACTIVO", utils.getCurrentDate(), LocationService.lat0, LocationService.long0, preferencias.getGlobalData(botonesActivity, "TM"))
//                    //Aqui iba el mismo codigo de sendButtonData()
//                    utils.doRequest(registro, botonesActivity)
//                }
            } else {
                println("77777777 // ! SERVICIO NO EJECUTANDOSE ! // 77777777")
//                if (botonesActivity != null) runBlocking {
//                    val registro = Registro("0","Servicio INACTIVO", utils.getCurrentDate(), "0.2000", "0.2000", preferencias.getGlobalData(botonesActivity, "TM"))
//                    //Aqui iba el mismo codigo de sendButtonData()
//                    utils.doRequest(registro, botonesActivity)
//                }
            }

            runBlocking {
                val dataMi = Registro("0","ACTION_LOCATE_DEVICE", utils.getCurrentDate(), LocationService.lat0, LocationService.long0, preferencias.extGetGlobalData(this@ExtraUtils, "TM"))
                send(applicationContext, dataMi)
            }
        } else if (accion == "robo"){
            println("♦♦ ♠♠ ALERTA DE UNIDAD ROBADA ♠♠ ♦♦")
            sendNotification(1, "urgent_channel", "Notificacion urgente", message.data.get("message"), message.data.get("url"))
            println("♦♦ ♠♠ JOB DONE ♠♠ ♦♦")
        }
    }

    private suspend fun send(context: Context, data: Registro){
        val retrofit = Retrofit.Builder()
            .baseUrl(context.getString(com.miapp.custodio2.R.string.UrlBase))
            .build()
        val service = retrofit.create(APIService::class.java)
        val jsonObject = JSONObject()

        jsonObject.put("Accion", data.Accion)
        jsonObject.put("Fecha", data.Fecha)
        jsonObject.put("Latitud", data.Latitud)
        jsonObject.put("Longitud", data.Longitud)
        jsonObject.put("Token", data.Token)
        val jsonObjectString = jsonObject.toString()
        val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

        val response = service.endAcciones(requestBody)
        if (response.isSuccessful) {
            //Obtiene la respuesta del request y la pasa a JSON
            val gson = GsonBuilder().setPrettyPrinting().create()
            val prettyJson = gson.toJson(JsonParser.parseString(response.body()?.string()))
            //El JSON lo pasas a objeto tipo Autenticar (readREsult)
            val gson2 = GsonBuilder().setPrettyPrinting().create()
            val readResult: RegistroRes = gson2.fromJson(prettyJson, RegistroRes::class.java)

            println("Consulta REGISTRO: "+jsonObjectString)
            println("EXITO-REGISTRO, json="+prettyJson)
            //infoRegistro = readResult
        } else {
            println("ERROR-REGISTRO: "+response.code().toString())
            println("Consulta Hecha: "+jsonObjectString)
            //errorString = "ERROR-REGISTRO: "+response.code().toString()+" CONSULTA: "+jsonObjectString
        }
    }

    private fun sendNotification(notiId: Int, channelId: String, channelName: String, msg: String?, url: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notificación de COMSI-Custodio"
                enableVibration(true)
            }

            val notiManager = getSystemService(NotificationManager::class.java)

            notiManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, PanelActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("msg", msg)
        intent.putExtra("url", url)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo_comsi) // Cambia por tu icono
            .setContentTitle("Unidad Robada")
            .setContentText("Presione esta notificación para ver detalles.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setColor(Color.RED)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(notiId, notification)
    }

}