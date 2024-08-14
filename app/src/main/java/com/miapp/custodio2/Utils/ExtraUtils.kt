package com.miapp.custodio2.Utils

import android.content.Context
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.miapp.custodio2.BotonesActivity
import com.miapp.custodio2.ClasesRequest.APIService
import com.miapp.custodio2.ClasesRequest.Registro
import com.miapp.custodio2.ClasesRequest.RegistroRes
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
            println("TENGO QUE ENVIAR LA UBICACIOOOON !!!!")

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

}