package com.miapp.custodio2.Utils

import android.Manifest
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.util.Base64
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.miapp.custodio2.ClasesRequest.*
import com.techiness.progressdialoglibrary.ProgressDialog
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class RequestPermissions() {

    //Se usa para obtener las ubicaciones
    lateinit var fusedLocationClient: FusedLocationProviderClient

    //UpdatedLocation
    private lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback

    //Firebase Mesaging para el Token de la Mision
    private lateinit var fireMessaging: FirebaseMessaging

    //Permisos
    var permisos = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CALL_PHONE)

    //UBICACION
    var latitude = ""
    var longitude = ""
    var coordenadas = arrayOf("", "")

    //C2DM - TOKEN DEVICE
    var tokenDevice = "solo"

    //RESPONSES
    //Inicio de sesion - /Autenticar
    var infoAutenticar:Autenticar? = null
    var infoMision:MisionRes? = null
    var infoUpdate:UpdateRes? = null
    var infoFoto:FotoRes? = null
    var infoBotones: BotonesRes? = null
    var infoDireccion: EnvioRes? = null
    var infoRegistro: RegistroRes? = null
    var infoCheck: CheckRes? = null

    //Foto del marchamo fiscal
    var foto = ""
    //Fecha actual
    var fechaActual = ""
    //Clave de la API
    val claveApi = "4024d038c2515f27a43a979789dc46427134f39aa52e98fb209f85ebcbcb4d76"
    //Telefono de emergencia para la llamada del Boton Llamada
    var tel = ""
    var progressDialog: ProgressDialog? = null

    var errorString = "Sin identificar"
    var tipoErrorString = "0"

    var listOfFotos: MutableList<Foto> = mutableListOf()

    //PERMISOS
    //act: Activity, permisos: Array<String>, code: Int
    fun solicitarPermisos(act: Activity) {

        if (Build.VERSION.SDK_INT >= 33) {
            Toast.makeText(act, "Android 11 o mayor", Toast.LENGTH_SHORT).show()
            println("SDK mayor a 33 o igual")
            // El dispositivo está ejecutando Android 11 o una versión posterior
            permisos = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.CAMERA, android.Manifest.permission.READ_MEDIA_IMAGES, android.Manifest.permission.CALL_PHONE)
        }

        println("Permisos: ")
        for (k in permisos) {
            println("PP: "+k)
        }
        ActivityCompat.requestPermissions(act, permisos, 125)

        /*if(ContextCompat.checkSelfPermission(act, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            MaterialAlertDialogBuilder(act)
                .setTitle("Active los permisos necesarios")
                .setMessage(act.getString(com.miapp.custodio2.R.string.askPermisos))
                .setNeutralButton("Aceptar") { dialog, which ->
                    // Respond to neutral button press
                }
                .show()
        }*/
    }

    fun askForPermissions(act: Activity) {
        // En tu actividad, dentro de un método o función
        if (Build.VERSION.SDK_INT >= 33) {
            Toast.makeText(act, "Android 11 o mayor", Toast.LENGTH_SHORT).show()
            println("SDK mayor a 33 o igual")
            // El dispositivo está ejecutando Android 11 o una versión posterior
            permisos = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.CAMERA, android.Manifest.permission.ACCESS_MEDIA_LOCATION, android.Manifest.permission.CALL_PHONE)
        }

        val permisosNoConcedidos = mutableListOf<String>()

        for (permiso in permisos) {
            val resultado = ContextCompat.checkSelfPermission(act, permiso)
            if (resultado != PackageManager.PERMISSION_GRANTED) {
                permisosNoConcedidos.add(permiso)
                //Toast.makeText(act, permiso.toString()+" !! NO concedido !!", Toast.LENGTH_LONG).show()
            } else {
                //Toast.makeText(act, permiso.toString()+" CONCEDIDO", Toast.LENGTH_SHORT).show()
            }
        }

        if (permisosNoConcedidos.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                act,
                permisosNoConcedidos.toTypedArray(),
                100
            )
        }
    }

    fun checkGrantedpermissions(act: Activity):Boolean {

        for (permiso in permisos) {
            val resultado = ContextCompat.checkSelfPermission(act, permiso)

            if (resultado != PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(act, permiso.toString()+" NO concedido", Toast.LENGTH_LONG).show()

                MaterialAlertDialogBuilder(act)
                    .setTitle("Active los permisos necesarios")
                    .setMessage(act.getString(com.miapp.custodio2.R.string.askPermisos))
                    .setNeutralButton("Aceptar") { dialog, which ->
                        // Respond to neutral button press
                        ActivityCompat.requestPermissions(act, permisos, 125)
                    }
                    .setPositiveButton("Dar permiso") {dialog, which ->
                        //println("PEEEERMMMMM: "+permiso)
                        //askPerm(permiso, act)
                        abrirConfiguracion(act)
                    }
                    .show()
                //askForPermissions(act)
                return false
            }
        }
        return true
    }

    private fun abrirConfiguracion(act: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", act.packageName, null)
        startActivityForResult(act, intent, 123, null)
    }



    fun verificarPermisos(act: Activity) :Boolean{

        var almacenamiento = Manifest.permission.READ_EXTERNAL_STORAGE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // El dispositivo está ejecutando Android 11 o una versión posterior
            almacenamiento = Manifest.permission.MANAGE_EXTERNAL_STORAGE
        }

        if (ActivityCompat.checkSelfPermission(act, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(act, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(act, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(act, almacenamiento) != PackageManager.PERMISSION_GRANTED) {
            // PERMISOS NOT GRANTED
                MaterialAlertDialogBuilder(act)
                    .setTitle("Active los permisos necesarios")
                    .setMessage(act.getString(com.miapp.custodio2.R.string.askPermisos))
                    .setNeutralButton("Aceptar") { dialog, which ->
                        // Respond to neutral button press
                        ActivityCompat.requestPermissions(act, permisos, 125)
                    }
                    .show()
            return false

            } else {
                return true
        }
    }


    //UBICACION
    fun startLocationUpdates(act: Activity) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(act)

        if (ActivityCompat.checkSelfPermission(act, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(act, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            //Permisos ]GRANTED
            //Toast.makeText(act, "EMPECE", Toast.LENGTH_SHORT).show()
                println("EMPECEEEEEEe")
                locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                    .build()
                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        try {
                            super.onLocationResult(result)
                            result ?: return
                            //result.locations.isNotEmpty() && !result.locations.isNullOrEmpty()

                            //println("E.er.r.: "+result.lastLocation!!.latitude.toString())

                            if (result.locations.isNotEmpty() && !result.locations.isNullOrEmpty()) {
                                latitude = result.lastLocation!!.latitude.toString()
                                longitude = result.lastLocation!!.longitude.toString()
                                coordenadas.set(0, latitude)
                                coordenadas.set(1, longitude)
                                println("RESULT.LATITUD: "+latitude+" RESULT.LONGITUD: "+longitude)
                                //myCallback.invoke(coordenadas)
                                /*Toast.makeText(act, "Consegui UBICACION SIUUU", Toast.LENGTH_SHORT).show()*/
                                /*stopLocationUpdates()*/

                            } else {
                                coordenadas.set(0, "Empty")
                                println("ELSE __ RESULT.LATITUD: "+latitude+" RESULT.LONGITUD: "+longitude)
                                //myCallback.invoke(coordenadas)
                                //Toast.makeText(act, "result es nulo o vacio", Toast.LENGTH_SHORT).show()
                                Toast.makeText(act, "Problemas con la señal, Ubicación obtenida en vacío", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: SecurityException) {
                            // Excepción relacionada con los permisos de ubicación
                            Toast.makeText(act, "No se tienen los permisos de ubicación necesarios", Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        } catch (e: Exception) {
                            // Otra excepción genérica
                            Toast.makeText(act, "Error al obtener la ubicación: ${e.message}", Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        }

                    }
                }

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
        } else {
            println("Sin permisos concedidos")
            Toast.makeText(act, "Permisos de ubicacion no concedidos", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun stopLocationUpdates222(act: Activity):Boolean {
        if (::locationCallback.isInitialized){
            fusedLocationClient.removeLocationUpdates(locationCallback)
            println("TODO BIEN AQQ")
            return true
        } else{
            //Toast.makeText(act, "locationCallback es NULL", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    fun isLocationCallbackNull(): Boolean{
        if (::locationCallback.isInitialized){
            return false
        } else {
            return true
        }
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Verificar si el proveedor de ubicación de red está habilitado
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        // Verificar si el proveedor de ubicación GPS está habilitado
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        // Devolver verdadero si al menos uno de los proveedores está habilitado
        return isNetworkEnabled || isGpsEnabled
    }

    fun onlyCurrentLocation(act: Activity, myCallback: (result: String?) -> Unit){
        if (ActivityCompat.checkSelfPermission(
                act, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                act, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //Permisos GRANTED
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken(){
                override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
                override fun isCancellationRequested() = false
            })
                .addOnSuccessListener { location: Location? ->
                    if (location == null) {
                        //Toast.makeText(act, "No se pudo obetener la Ubicación", Toast.LENGTH_SHORT).show()
                        myCallback.invoke("false")
                    } else {
                        latitude = location.latitude.toString()
                        longitude = location.longitude.toString()

                        println("OCL= lat:"+latitude+" long:"+longitude)
                        //Toast.makeText(act, "OCL= lat:"+latitude+" long:"+longitude, Toast.LENGTH_SHORT).show()
                        myCallback.invoke("true")
                    }
                }
        }
    }

    fun lastLocation(act: Activity, myCallback: (result: String?) -> Unit){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(act)
        if (ActivityCompat.checkSelfPermission(
                act,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                act,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //Si hay permisos
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    // Got last known location. In some rare situations this can be null.
                    latitude = location!!.latitude.toString()
                    longitude = location!!.longitude.toString()
                    //Toast.makeText(act, "OL= lat:"+latitude+" long:"+longitude, Toast.LENGTH_SHORT).show()
                    myCallback.invoke("true")

                }
                .addOnFailureListener {
                    Toast.makeText(act, "Error al obtener la ubicacion, intente de nuevo", Toast.LENGTH_SHORT).show()
                    myCallback.invoke("false")
                }
        }
    }

    //El C2DM = token del dispositivo
    fun getDeviceToken(act: Activity) {
        /*if (!this::fusedLocationClient.isInitialized){
            startLocationUpdates(act)
            Toast.makeText(act, "IM HERE WWW", Toast.LENGTH_SHORT).show()
        }*/
        fireMessaging = FirebaseMessaging.getInstance()

        fireMessaging.token
            .addOnSuccessListener {
                tokenDevice = it
                //myCallback.invoke(tokenDevice)
                println("TOKEN_DEVICE: "+tokenDevice)

            }
            .addOnFailureListener {
                Toast.makeText(act, "OnFailureListener: " + it.toString(), Toast.LENGTH_SHORT)
                    .show()
                tokenDevice = "Exception"
                //myCallback.invoke(tokenDevice)
            }
    }

    //Seleccionar imagen de Galeria
    fun pickImage(act:Activity, reqCode: Int){
        progressDialog = ProgressDialog(act)
        progressDialog!!.theme = ProgressDialog.THEME_LIGHT
        progressDialog!!.mode = ProgressDialog.MODE_INDETERMINATE
        progressDialog!!.setMessage("Subiendo foto...")
        progressDialog!!.setTitle("Enviando foto")

        if (progressDialog == null) {
            println("Oh no, no se desplego el progrems dialog en PickImage")
            println("Oh no, no se desplego el progrems dialog en PickImage")
            return
        }

        val items = arrayOf("Galería", "Cámara")
        MaterialAlertDialogBuilder(act)
            .setTitle("Subir foto con: ")
            .setItems(items) { dialog, which ->
                when(which){
                    0 -> {
                        progressDialog!!.show()
                        //galeria
                        ImagePicker.with(act)
                            .compress(1000)
                            .maxResultSize(1032, 1032)
                            .galleryOnly()
                            .start(reqCode)
                    }
                    1 -> {
                        progressDialog!!.show()
                        //camara
                        ImagePicker.with(act)
                            .compress(1000)
                            .maxResultSize(1032, 1032)
                            .cameraOnly()	//User can only capture image using Camera
                            .start(reqCode)
                    }
                }
            }
            .setNeutralButton("Cancelar") { dialog, which ->
                // Respond to neutral button press
            }
            .show()
    }


    fun converToBase64(act: Activity, uri: Uri): String{
        val bitmap: Bitmap = BitmapFactory.decodeStream(act.contentResolver.openInputStream(uri))
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val bits = baos.toByteArray()
        val base64image = Base64.encodeToString(bits, Base64.DEFAULT)
        return base64image
    }

    fun getCurrentDate(): String{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Fecha de hoy
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            fechaActual = LocalDateTime.now().format(formatter)
            return fechaActual
        } else {
            //TODO("VERSION.SDK_INT < O")
            //Fecha de hoy
            val simpleDate = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
            fechaActual = simpleDate.format(Date())
            return fechaActual
        }
    }

    fun call(act: Activity, tel: String){
        if (tel != ""){
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$tel")
            act.startActivity(intent)
        } else {
            Toast.makeText(act, "Telefono para llamada no proveido", Toast.LENGTH_SHORT).show()
        }
    }


    //Request
    suspend fun doRequest(datos: Any, act: Activity){

        when(datos){
            //Autenticar
            is Inicio -> autenticar(datos, act)
            is Mision -> mision(datos, act)
            is Update -> update(datos, act)
            is Foto -> subirFoto(datos, act)
            is MutableList<*> -> subirListFotos(datos as MutableList<Foto>, act)
            is Botones -> getBotones(datos, act)
            is Envio -> getDireccion(datos, act)
            is Registro -> registro(datos, act)
            is Check -> checkMision(datos, act)
            else -> println("ELSE WHEN")
        }
    }

    suspend fun autenticar(datos: Inicio, act: Activity){
        try {
//            val okHttpClient = OkHttpClient.Builder()
//                .connectTimeout(1, TimeUnit.MILLISECONDS) // Tiempo de espera de 1 milisegundo
//                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(act.getString(com.miapp.custodio2.R.string.UrlBase))
                .build()
            val service = retrofit.create(APIService::class.java)
            val jsonObject = JSONObject()

            jsonObject.put("C2dm", datos.C2dm)
            jsonObject.put("Huella", datos.Huella)
            jsonObject.put("Latitud", datos.Latitud)
            jsonObject.put("Longitud", datos.Longitud)
            jsonObject.put("Palabra", datos.Palabra)
            jsonObject.put("Password", datos.Password)
            jsonObject.put("Usuario", datos.Usuario)
            val jsonObjectString = jsonObject.toString()
            val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())
            println("-----------requestBody: "+jsonObjectString)

            val response = service.endInicionSesion(requestBody)

            if (response.isSuccessful) {
                //Obtiene la respuesta del request y la pasa a JSON
                val gson = GsonBuilder().setPrettyPrinting().create()
                val prettyJson = gson.toJson(JsonParser.parseString(response.body()?.string()))
                //El JSON lo pasas a objeto tipo Autenticar (readREsult)
                val gson2 = GsonBuilder().setPrettyPrinting().create()
                val readResult:Autenticar = gson2.fromJson(prettyJson, Autenticar::class.java)

                println("URL: "+act.getString(com.miapp.custodio2.R.string.UrlBase))
                println("EXITO-Autenticar, json="+prettyJson)
                infoAutenticar = readResult
                println("readResult: "+readResult)
                println("readResult.PrimerUso: "+readResult.PrimerUso)
            } else {
                println("ERROR-Autenticar: "+response.code().toString())
                println("Consulta Hecha: "+jsonObjectString)
                infoAutenticar = null
                errorString = "ERROR AUT: "+response.code().toString()+" CONSULTA: "+jsonObjectString
            }
        } catch (e: UnknownHostException){
            println("Au/ Error de resolución de host: ${e.message}")
            Toast.makeText(act, "Au/ Error comunicación con host, verfique que tenga conexión a Internet", Toast.LENGTH_LONG).show()
        } catch (e: SocketTimeoutException) {
            println("Au/ Error de tiempo de espera de conexión: ${e.message}")
            Toast.makeText(act, "Au/ Error de tiempo de espera de conexión, intente de nuevo", Toast.LENGTH_LONG).show()
        }
    }

    suspend fun mision(datos: Mision, act: Activity){
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl(act.getString(com.miapp.custodio2.R.string.UrlBase))
                .build()
            val service = retrofit.create(APIService::class.java)
            val jsonObject = JSONObject()

            jsonObject.put("Escopeta", datos.Escopeta)
            jsonObject.put("Marchamo", datos.Marchamo)
            jsonObject.put("MarchamoFiscal", datos.MarchamoFiscal)
            jsonObject.put("MarchamoGPS", datos.MarchamoGPS)
            jsonObject.put("Notas", datos.Notas)
            jsonObject.put("NumeroViaje", datos.NumeroViaje)
            jsonObject.put("Piloto", datos.Piloto)
            jsonObject.put("Placa", datos.Placa)
            jsonObject.put("Sellado", datos.Sellado)
            jsonObject.put("Telefono", datos.Telefono)
            jsonObject.put("Token", datos.Token)
            val jsonObjectString = jsonObject.toString()
            val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

            val response = service.endNuevaMision(requestBody)
            if (response.isSuccessful) {
                //Obtiene la respuesta del request y la pasa a JSON
                val gson = GsonBuilder().setPrettyPrinting().create()
                val prettyJson = gson.toJson(JsonParser.parseString(response.body()?.string()))
                //El JSON lo pasas a objeto tipo Autenticar (readREsult)
                val gson2 = GsonBuilder().setPrettyPrinting().create()
                val readResult:MisionRes = gson2.fromJson(prettyJson, MisionRes::class.java)

                println("Datos Enviados: "+jsonObjectString)
                println("EXITO-Mision, json="+prettyJson)
                infoMision = readResult
            } else {
                println("ERROR-Mision: "+response.code().toString())
                println("Consulta Hecha: "+jsonObjectString)
                infoMision = null
            }
        } catch (e: UnknownHostException){
            println("Mis/ Error de resolución de host: ${e.message}")
            tipoErrorString = "Mis/1"

            MaterialAlertDialogBuilder(act)
                .setTitle("Error de conexion con Comsi SICCAP ")
                .setMessage("Error ${e.localizedMessage}: ${e.message} \n\nProblemas con conexion a Comsi SICCAP, compruebe estar con conexión sino itente mas tarde")
                .setNeutralButton("Aceptar") { dialog, which ->
                    // Respond to neutral button press
                    return@setNeutralButton
                }
                .show()

            Toast.makeText(act, "Mis/ Error comunicación con host, verfique que tenga conexión a Internet", Toast.LENGTH_LONG).show()
            errorString = "Mis/ Error de resolución de host: ${e.message}"
            return
        } catch (e: SocketTimeoutException) {
            println("Mis/ Error de tiempo de espera de conexión: ${e.message}")
            tipoErrorString = "Mis/2"
            Toast.makeText(act, "Mis/ Error de tiempo de espera de conexión, intente de nuevo", Toast.LENGTH_LONG).show()
            errorString = "Mis/ Error de tiempo de espera de conexión: ${e.message}"
        }
    }

    suspend fun update(datos: Update, act: Activity){
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl(act.getString(com.miapp.custodio2.R.string.UrlBase))
                .build()
            val service = retrofit.create(APIService::class.java)
            val jsonObject = JSONObject()

            jsonObject.put("CampoActual", datos.CampoActual)
            jsonObject.put("DatoActual", datos.DatoActual)
            jsonObject.put("DatoNuevo", datos.DatoNuevo)
            jsonObject.put("Latitud", datos.Latitud)
            jsonObject.put("Longitud", datos.Longitud)
            jsonObject.put("Token", datos.Token)
            val jsonObjectString = jsonObject.toString()
            val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

            val response = service.endUpdate(requestBody)
            if (response.isSuccessful) {
                //Obtiene la respuesta del request y la pasa a JSON
                val gson = GsonBuilder().setPrettyPrinting().create()
                val prettyJson = gson.toJson(JsonParser.parseString(response.body()?.string()))
                //El JSON lo pasas a objeto tipo Autenticar (readREsult)
                val gson2 = GsonBuilder().setPrettyPrinting().create()
                val readResult:UpdateRes = gson2.fromJson(prettyJson, UpdateRes::class.java)

                println("Datos Enviados: "+jsonObjectString)
                println("EXITO-Update, json="+prettyJson)
                infoUpdate = readResult
            } else {
                println("ERROR-Update: "+response.code().toString())
                println("Consulta Hecha: "+jsonObjectString)
                infoUpdate = null
            }
        } catch (e: UnknownHostException){
            println("Update/ Error de resolución de host: ${e.message}")
            Toast.makeText(act, "Update/ Error comunicación con host, verfique que tenga conexión a Internet", Toast.LENGTH_LONG).show()
            //tipoErrorString = "Update/1"
        } catch (e: SocketTimeoutException) {
            println("Update/ Error de tiempo de espera de conexión: ${e.message}")
            Toast.makeText(act, "Update/ Error de tiempo de espera de conexión, intente de nuevo", Toast.LENGTH_LONG).show()
        }
    }

    suspend fun subirFoto(datos: Foto, act: Activity){
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl(act.getString(com.miapp.custodio2.R.string.UrlBase))
                .build()
            val service = retrofit.create(APIService::class.java)
            val jsonObject = JSONObject()

            //jsonObject.put("ID", datos.Id)
            jsonObject.put("Accion", datos.Accion)
            jsonObject.put("Fecha", datos.Fecha)
            jsonObject.put("Foto", datos.Foto)
            jsonObject.put("Latitud", datos.Latitud)
            jsonObject.put("Longitud", datos.Longitud)
            jsonObject.put("TipoFotografia", datos.Id)
            jsonObject.put("Token", datos.Token)
            val jsonObjectString = jsonObject.toString()
            val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

            //val response = service.endSaveFoto(requestBody)
            val response = service.endGrabarFotoTipo(requestBody)

            if (response.isSuccessful) {
                //Obtiene la respuesta del request y la pasa a JSON
                val gson = GsonBuilder().setPrettyPrinting().create()
                val prettyJson = gson.toJson(JsonParser.parseString(response.body()?.string()))
                //El JSON lo pasas a objeto tipo Autenticar (readREsult)
                val gson2 = GsonBuilder().setPrettyPrinting().create()
                val readResult:FotoRes = gson2.fromJson(prettyJson, FotoRes::class.java)

                println("UBI, lat="+datos.Latitud+" lon="+datos.Longitud+" TKN="+datos.Token)
                println("Consulta Foto: "+jsonObjectString)
                println("EXITO-FOTO, json="+prettyJson)
                infoFoto = readResult
            } else {
                Toast.makeText(act, "Erro al subir la foto", Toast.LENGTH_SHORT).show()
                println("ERROR-SUBIR FOTO: "+response.code().toString())
                println("Consulta Hecha: "+jsonObjectString)
                infoFoto = null
                errorString = "ERROR FOTO: "+response.code().toString()+" CONSULTA: "+jsonObjectString
            }
        } catch (e: UnknownHostException){
            println("Foto/ Error de resolución de host: ${e.message}")
            Toast.makeText(act, "Foto/ Error comunicación con host, verfique que tenga conexión a Internet", Toast.LENGTH_LONG).show()
        } catch (e: SocketTimeoutException) {
            println("Foto/ Error de tiempo de espera de conexión: ${e.message}")
            Toast.makeText(act, "Foto/ Error de tiempo de espera de conexión, intente de nuevo", Toast.LENGTH_LONG).show()
        }
    }

    suspend fun subirListFotos(datos: MutableList<Foto>, act: Activity){
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl(act.getString(com.miapp.custodio2.R.string.UrlBase))
                .build()
            val service = retrofit.create(APIService::class.java)


            for (foto in datos){
                /** Inicio iteracion Fotos **/
                val jsonObject = JSONObject()
                //jsonObject.put("ID", foto.Id)
                jsonObject.put("Accion", foto.Accion)
                jsonObject.put("Fecha", foto.Fecha)
                jsonObject.put("Foto", foto.Foto)
                jsonObject.put("Latitud", foto.Latitud)
                jsonObject.put("Longitud", foto.Longitud)
                jsonObject.put("TipoFotografia", foto.Id)
                jsonObject.put("Token", foto.Token)

                val jsonObjectString = jsonObject.toString()
                val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

                val response = service.endGrabarFotoTipo(requestBody)

                if (response.isSuccessful) {
                    //Obtiene la respuesta del request y la pasa a JSON
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val prettyJson = gson.toJson(JsonParser.parseString(response.body()?.string()))
                    //El JSON lo pasas a objeto tipo Autenticar (readREsult)
                    val gson2 = GsonBuilder().setPrettyPrinting().create()
                    val readResult:FotoRes = gson2.fromJson(prettyJson, FotoRes::class.java)

                    println("UBI, lat="+foto.Latitud+" lon="+foto.Longitud+" TKN="+foto.Token)
                    println("Consulta Foto: "+jsonObjectString)
                    println("EXITO-FOTO, json="+prettyJson)
                    infoFoto = readResult
                    Toast.makeText(act, "Foto de Tipo: "+foto.Id+" Success: "+ infoFoto!!.Message, Toast.LENGTH_SHORT).show()
                    println("Foto de Tipo: "+foto.Id+" Success: "+ infoFoto!!.Success.toString()+" Message: "+infoFoto!!.Message)
                } else {
                    Toast.makeText(act, "Erro al subir la foto", Toast.LENGTH_SHORT).show()
                    println("ERROR-SUBIR FOTO: "+response.code().toString())
                    println("Consulta Hecha: "+jsonObjectString)
                    Toast.makeText(act, "Foto de Tipo: "+foto.Id+" ERROR: "+response.errorBody().toString(), Toast.LENGTH_SHORT).show()
                    infoFoto = null
                    errorString = "ERROR FOTO: "+response.code().toString()+" CONSULTA: "+jsonObjectString
                    println("ERROR Foto Tipo: "+foto.Id+" ErrorString: "+errorString)
                }
                /** Fin iteracion Fotos **/
            }

            progressDialog!!.dismiss()
        } catch (e: UnknownHostException){
            println("ListFts/ Error de resolución de host: ${e.message}")
            Toast.makeText(act, "ListFts/ Error comunicación con host, verfique que tenga conexión a Internet", Toast.LENGTH_LONG).show()
        } catch (e: SocketTimeoutException) {
            println("ListFts/ Error de tiempo de espera de conexión: ${e.message}")
            Toast.makeText(act, "ListFts/ Error de tiempo de espera de conexión, intente de nuevo", Toast.LENGTH_LONG).show()
        }
    }

    suspend fun getBotones(datos: Botones, act: Activity){
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl(act.getString(com.miapp.custodio2.R.string.UrlBase))
                .build()
            val service = retrofit.create(APIService::class.java)
            val jsonObject = JSONObject()

            jsonObject.put("ClaveApi", datos.ClaveAPi)
            val jsonObjectString = jsonObject.toString()
            val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

            val response = service.endBotones(requestBody)
            if (response.isSuccessful) {
                //Obtiene la respuesta del request y la pasa a JSON
                val gson = GsonBuilder().setPrettyPrinting().create()
                val prettyJson = gson.toJson(JsonParser.parseString(response.body()?.string()))
                //El JSON lo pasas a objeto tipo Autenticar (readREsult)
                val gson2 = GsonBuilder().setPrettyPrinting().create()
                val readResult:BotonesRes = gson2.fromJson(prettyJson, BotonesRes::class.java)

                println("Consulta Botones: "+jsonObjectString)
                println("EXITO-GET BOTONES, json="+prettyJson)
                infoBotones = readResult
            } else {
                println("ERROR-GET BOTONES: "+response.code().toString())
                println("Consulta Hecha: "+jsonObjectString)
                errorString = "ERROR-GET BOTONES: "+response.code().toString()+" CONSULTA: "+jsonObjectString
            }
        } catch (e: UnknownHostException){
            println("getBtns/ Error de resolución de host: ${e.message}")
            Toast.makeText(act, "getBtns/ Error comunicación con host, verfique que tenga conexión a Internet", Toast.LENGTH_LONG).show()
        } catch (e: SocketTimeoutException) {
            println("getBtns/ Error de tiempo de espera de conexión: ${e.message}")
            Toast.makeText(act, "getBtns/ Error de tiempo de espera de conexión, intente de nuevo", Toast.LENGTH_LONG).show()
        }
    }

    suspend fun getDireccion(datos: Envio, act: Activity){
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl(act.getString(com.miapp.custodio2.R.string.UrlBase))
                .build()
            val service = retrofit.create(APIService::class.java)
            val jsonObject = JSONObject()

            jsonObject.put("Token", datos.Token)
            val jsonObjectString = jsonObject.toString()
            val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

            val response = service.endGetDireccion(requestBody)
            if (response.isSuccessful) {
                //Obtiene la respuesta del request y la pasa a JSON
                val gson = GsonBuilder().setPrettyPrinting().create()
                val prettyJson = gson.toJson(JsonParser.parseString(response.body()?.string()))
                //El JSON lo pasas a objeto tipo Autenticar (readREsult)
                val gson2 = GsonBuilder().setPrettyPrinting().create()
                val readResult:EnvioRes = gson2.fromJson(prettyJson, EnvioRes::class.java)

                println("Consulta DIRECCION: "+jsonObjectString)
                println("EXITO-GET DIRECCION, json="+prettyJson)
                infoDireccion = readResult
            } else {
                println("ERROR-GET DIRECCION: "+response.code().toString())
                println("Consulta Hecha: "+jsonObjectString)
                errorString = "ERROR-GET DIRECCION: "+response.code().toString()+" CONSULTA: "+jsonObjectString
            }
        } catch (e: UnknownHostException){
            println("getDirec/ Error de resolución de host: ${e.message}")
            Toast.makeText(act, "getDirec/ Error comunicación con host, verfique que tenga conexión a Internet", Toast.LENGTH_LONG).show()
        } catch (e: SocketTimeoutException) {
            println("getDirec/ Error de tiempo de espera de conexión: ${e.message}")
            Toast.makeText(act, "getDirec/ Error de tiempo de espera de conexión, intente de nuevo", Toast.LENGTH_LONG).show()
        }
    }

    suspend fun registro(datos: Registro, act: Activity){
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl(act.getString(com.miapp.custodio2.R.string.UrlBase))
                .build()
            val service = retrofit.create(APIService::class.java)
            val jsonObject = JSONObject()

            jsonObject.put("Accion", datos.Accion)
            jsonObject.put("Fecha", datos.Fecha)
            jsonObject.put("Latitud", datos.Latitud)
            jsonObject.put("Longitud", datos.Longitud)
            jsonObject.put("Token", datos.Token)
            val jsonObjectString = jsonObject.toString()
            val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

            try {
                val response = service.endAcciones(requestBody)
                if (response.isSuccessful) {
                    //Obtiene la respuesta del request y la pasa a JSON
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val prettyJson = gson.toJson(JsonParser.parseString(response.body()?.string()))
                    //El JSON lo pasas a objeto tipo Autenticar (readREsult)
                    val gson2 = GsonBuilder().setPrettyPrinting().create()
                    val readResult:RegistroRes = gson2.fromJson(prettyJson, RegistroRes::class.java)

                    println("Consulta REGISTRO: "+jsonObjectString)
                    println("EXITO-REGISTRO, json="+prettyJson)
                    println("ReadResult.Success: "+readResult.Success)
                    infoRegistro = readResult
                } else {
                    println("ERROR-REGISTRO: "+response.code().toString())
                    println("Consulta Hecha: "+jsonObjectString)
                    errorString = "ERROR-REGISTRO: "+response.code().toString()+" CONSULTA: "+jsonObjectString
                }
            } catch (e: SocketTimeoutException){
                Toast.makeText(act, "Intente de nuevo. La conexión con la API fallo", Toast.LENGTH_LONG).show()
            }
        } catch (e: UnknownHostException){
            println("Reg/ Error de resolución de host: ${e.message}")

            MaterialAlertDialogBuilder(act)
                .setTitle("Error de conexión con Comsi SICCAP ")
                .setMessage("Error : ${e.message} \n\nProblemas con conexion a Comsi SICCAP, compruebe estar con conexión o sino itentelo mas tarde")
                .setNeutralButton("Aceptar") { dialog, which ->
                    // Respond to neutral button press
                    return@setNeutralButton
                }
                .show()

            Toast.makeText(act, "Reg/ Error comunicación con host, verfique que tenga conexión a Internet", Toast.LENGTH_LONG).show()
        } catch (e: SocketTimeoutException) {
            println("Reg/ Error de tiempo de espera de conexión: ${e.message}")
            Toast.makeText(act, "Reg/ Error de tiempo de espera de conexión, intente de nuevo", Toast.LENGTH_LONG).show()
        }
//        if (this.infoRegistro == null){
//            println("INFO_RES: ES NULL ")
//            println("INFO_RES0: "+ this.infoRegistro!!.Success)
//        } else {
//            println("INFO_RES: "+ this.infoRegistro!!.Success)
//        }
    }

    suspend fun checkMision(datos: Check, act: Activity){
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl(act.getString(com.miapp.custodio2.R.string.UrlBase))
                .build()
            val service = retrofit.create(APIService::class.java)
            val jsonObject = JSONObject()

            jsonObject.put("Requerimiento", datos.Requerimiento)
            jsonObject.put("Token", datos.Token)
            val jsonObjectString = jsonObject.toString()
            val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

            val response = service.endCheckMission(requestBody)
            if (response.isSuccessful) {
                //Obtiene la respuesta del request y la pasa a JSON
                val gson = GsonBuilder().setPrettyPrinting().create()
                val prettyJson = gson.toJson(JsonParser.parseString(response.body()?.string()))
                //El JSON lo pasas a objeto tipo Autenticar (readREsult)
                val gson2 = GsonBuilder().setPrettyPrinting().create()
                val readResult:CheckRes = gson2.fromJson(prettyJson, CheckRes::class.java)

                println("Consulta Check: "+jsonObjectString)
                println("EXITO-Check, json="+prettyJson)
                infoCheck = readResult
            } else {
                println("ERROR-Check: "+response.code().toString())
                println("Consulta Hecha: "+jsonObjectString)
                errorString = "ERROR-Check: "+response.code().toString()+" CONSULTA: "+jsonObjectString
            }
        } catch (e: UnknownHostException){
            println("CheckMision/ Error de resolución de host: ${e.message}")
            Toast.makeText(act, "CheckMision/ Error comunicación con host, verfique que tenga conexión a Internet", Toast.LENGTH_LONG).show()
        } catch (e: SocketTimeoutException) {
            println("CheckMision/ Error de tiempo de espera de conexión: ${e.message}")
            Toast.makeText(act, "CheckMision/ Error de tiempo de espera de conexión, intente de nuevo", Toast.LENGTH_LONG).show()
        }
    }


    suspend fun registro2(datos: Registro, act: ExtraUtils){
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl(act.getString(com.miapp.custodio2.R.string.UrlBase))
                .build()
            val service = retrofit.create(APIService::class.java)
            val jsonObject = JSONObject()

            jsonObject.put("Accion", datos.Accion)
            jsonObject.put("Fecha", datos.Fecha)
            jsonObject.put("Latitud", datos.Latitud)
            jsonObject.put("Longitud", datos.Longitud)
            jsonObject.put("Token", datos.Token)
            val jsonObjectString = jsonObject.toString()
            val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

            val response = service.endAcciones(requestBody)
            if (response.isSuccessful) {
                //Obtiene la respuesta del request y la pasa a JSON
                val gson = GsonBuilder().setPrettyPrinting().create()
                val prettyJson = gson.toJson(JsonParser.parseString(response.body()?.string()))
                //El JSON lo pasas a objeto tipo Autenticar (readREsult)
                val gson2 = GsonBuilder().setPrettyPrinting().create()
                val readResult:RegistroRes = gson2.fromJson(prettyJson, RegistroRes::class.java)

                println("Consulta REGISTRO: "+jsonObjectString)
                println("EXITO-REGISTRO, json="+prettyJson)
                infoRegistro = readResult
            } else {
                println("ERROR-REGISTRO: "+response.code().toString())
                println("Consulta Hecha: "+jsonObjectString)
                errorString = "ERROR-REGISTRO: "+response.code().toString()+" CONSULTA: "+jsonObjectString
            }
        } catch (e: UnknownHostException){
            println("Reg2/ Error de resolución de host: ${e.message}")
            Toast.makeText(act, "Reg2/ Error comunicación con host, verfique que tenga conexión a Internet", Toast.LENGTH_LONG).show()
        } catch (e: SocketTimeoutException) {
            println("Reg2/ Error de tiempo de espera de conexión: ${e.message}")
            Toast.makeText(act, "Reg2/ Error de tiempo de espera de conexión, intente de nuevo", Toast.LENGTH_LONG).show()
        }
    }

    fun startLocationUpdates2(act: Service, callback: CallbackUbi){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(act)

        if (ActivityCompat.checkSelfPermission(
                act,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                act,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //Permisos ]GRANTED


            locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .build()
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)
                    result ?: return
                    result ?: return

                    if (result.locations.isNotEmpty()) {
                        latitude = result.lastLocation!!.latitude.toString()
                        longitude = result.lastLocation!!.longitude.toString()
                        coordenadas.set(0, latitude)
                        coordenadas.set(1, longitude)
                        println("START-LOC-2 LAT: $latitude LONG: $longitude")
//                        val serviceIntent = Intent(act, SegundoPlanoTasks::class.java)
//                        act.startForegroundService(serviceIntent)
                        //myCallback.invoke(coordenadas)
                        /*Toast.makeText(act, "Consegui UBICACION SIUUU", Toast.LENGTH_SHORT).show()*/
                        /*stopLocationUpdates()*/
                        callback.onLocationReceived(location = result.lastLocation!!)

                    } else {
                        coordenadas.set(0, "Empty")
                        //myCallback.invoke(coordenadas)
                        callback.onLocationError("Not found")
                    }

                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }


}
