package com.miapp.custodio2.Utils

import android.location.Location
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.miapp.custodio2.ClasesRequest.Registro
import kotlinx.coroutines.runBlocking

class ExtraUtils: FirebaseMessagingService() {

    var accion:String? = ""
    val utils = RequestPermissions()
    val preferencias = Preferencias()
//
//    interface LocationCallback {
//        fun onLocationReceived(location: Location)
//        fun onLocationError(error: String)
//    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        runBlocking {
//            val serviceIntent = Intent(this@ExtraUtils, SegundoPlanoTasks::class.java)
//            this@ExtraUtils.startService(serviceIntent)

            accion = message.data.get("action")

            if (accion == "ACTION_LOCATE_DEVICE"){
                var datos = Registro(accion!!, utils.getCurrentDate(), utils.latitude, utils.longitude, preferencias.extGetGlobalData(this@ExtraUtils, "TM"))
                utils.startLocationUpdates2(this@ExtraUtils, object : CallbackUbi {
                    override fun onLocationReceived(location: Location) {
                        // Manejar la ubicación recibida
                        datos = Registro(accion!!, utils.getCurrentDate(), location.latitude.toString(), location.longitude.toString(), preferencias.extGetGlobalData(this@ExtraUtils, "TM"))
                    }

                    override fun onLocationError(error: String) {
                        // Manejar el error
                        datos = Registro(accion!!, utils.getCurrentDate(), error, error, preferencias.extGetGlobalData(this@ExtraUtils, "TM"))
                    }
                })
                utils.registro2(datos, this@ExtraUtils)
            }

            if(accion == "robo"){
                var mensaje = message.data.get("message").toString()
                var url = message.data.get("url").toString()
                preferencias.extSetGlobalData(this@ExtraUtils, "Panel", "true")
                preferencias.extSetGlobalData(this@ExtraUtils, "mPanel", mensaje)
                preferencias.extSetGlobalData(this@ExtraUtils, "urlPanel", url)
            }
        }
    }

}