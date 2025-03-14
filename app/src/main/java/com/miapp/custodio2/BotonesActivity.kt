package com.miapp.custodio2

import android.app.Activity
import android.app.AlertDialog
import android.content.Context

import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.miapp.custodio2.ClasesRequest.Adapters.Adapter
import com.miapp.custodio2.ClasesRequest.Adapters.CardViewData
import com.miapp.custodio2.ClasesRequest.*
import com.miapp.custodio2.Utils.Checker
import com.miapp.custodio2.Utils.LocationService
import com.miapp.custodio2.Utils.Preferencias
import com.miapp.custodio2.Utils.RequestPermissions
import com.miapp.custodio2.databinding.ActivityBotonesBinding
import com.techiness.progressdialoglibrary.ProgressDialog
import kotlinx.coroutines.*
import java.util.ArrayList
import kotlin.Result as Result

class BotonesActivity : AppCompatActivity(), Adapter.OnItemClickListener, Checker {
    //Para que funciones el Binding
    private lateinit var binding: ActivityBotonesBinding
    //Utils donde esta funciones genericas
    val utils = RequestPermissions()
    val preferencias = Preferencias()
    var botones: BotonesRes? = null
    //new
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var connManager: ConnectivityManager

    object ActivityHolder {
        var botonesActivity: BotonesActivity? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBotonesBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        GlobalScope.launch {
//            val registro = Registro("0","Usuario en Botones", utils.getCurrentDate(), LocationService.lat0, LocationService.long0, preferencias.getGlobalData(this@BotonesActivity, "TM"))
//            //Aqui iba el mismo codigo de sendButtonData()
//            utils.doRequest(registro, this@BotonesActivity)
//        }

        connManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connManager.registerDefaultNetworkCallback(networkCallback)

        ActivityHolder.botonesActivity = this

        preferencias.setGlobalData(this, "verM", "false")

        preferencias.setGlobalData(this, "FinalEnviado", "false")

        binding.tvMisionRui.text = "RUI: "+preferencias.getGlobalData(this, "Rui")
        binding.tvMisionNomb.text = "NOMB: "+preferencias.getGlobalData(this, "Nomb")

        lifecycleScope.launch {
            utils.startLocationUpdates(this@BotonesActivity)
            //Solo la primera vez hara el request para traer los botones y guardarlos en las preferencias
            if(preferencias.getGlobalData(this@BotonesActivity, "Sesion") == "primera"){
                val btns = Botones(utils.claveApi)
                utils.doRequest(btns, this@BotonesActivity)
                //pasar el objeto a gson(string)
                val gson = GsonBuilder().setPrettyPrinting().create()
                val jsonBotones = gson.toJson(utils.infoBotones)
                preferencias.setGlobalData(this@BotonesActivity, "infoBotones", jsonBotones)
                val gson2 = GsonBuilder().setPrettyPrinting().create()
                val jsonBotones2 = preferencias.getGlobalData(this@BotonesActivity, "infoBotones")
                botones = gson2.fromJson(jsonBotones2, BotonesRes::class.java)
            }

            //Cuando NO sea la Primera vez, cargara los botones de las preferencias
            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonBotones = preferencias.getGlobalData(this@BotonesActivity, "infoBotones")
            botones = gson.fromJson(jsonBotones, BotonesRes::class.java)
            //Toast.makeText(this@BotonesActivity, "PRUEBA: "+botones!!.Data.get(0).Accion, Toast.LENGTH_SHORT).show()

            val theList: ArrayList<CardViewData> = ArrayList()
            //---prueba
            for (i in 0 until botones!!.Data.size){
                println(botones!!.Data[i].Icono)
                val item = CardViewData(i ,botones!!.Data[i].Icono, botones!!.Data[i].Nombre)

                theList += item
            }
            //--- fin prueba
            binding.recyclerviewButtons.adapter = Adapter(theList, this@BotonesActivity)

            binding.recyclerviewButtons.layoutManager = GridLayoutManager(this@BotonesActivity, 3).also {
                it.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (position % 60 == 6)//Boton panico grande. Cambiar a == 7 para cuando la posicion del boton en la base de datos sea la septima.
                            1//3
                        else
                            1
                    }
                }
            }
            binding.recyclerviewButtons.setHasFixedSize(true)
        }

        if (utils.isServiceRunning(this, LocationService::class.java)){
            println("77777777 SERVICIO CORRIENDO 77777777")
//            runBlocking {
//                val registro = Registro("0", "Servicio ACTIVO !", utils.getCurrentDate(), LocationService.lat0, LocationService.long0, preferencias.getGlobalData(this@BotonesActivity, "TM"))
//                //Aqui iba el mismo codigo de sendButtonData()
//                utils.doRequest(registro, this@BotonesActivity)
//            }
        } else {
            println("77777777 // ! SERVICIO NO EJECUTANDOSE ! // 77777777")
//            runBlocking {
//                val registro = Registro("0","Servicio INACTIVO !", utils.getCurrentDate(), "0.10", "0.10", preferencias.getGlobalData(this@BotonesActivity, "TM"))
//                //Aqui iba el mismo codigo de sendButtonData()
//                utils.doRequest(registro, this@BotonesActivity)
//            }
        }

        //Boton de la MISION
        binding.misionSpace.setOnClickListener {
            preferencias.setGlobalData(this, "verM", "true")
            val intent = Intent(this, MisionActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        //Boton de la FOTO
        binding.fotoSpace.setOnClickListener {
            lifecycleScope.launch {
                //utils.infoFoto = null
                utils.pickImage(this@BotonesActivity, 4)
            }
        }

        binding.perfilSpace.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            if(preferencias.getGlobalData(this@BotonesActivity, "Panel") == "true"){
                startActivity(Intent(this@BotonesActivity, PanelActivity::class.java))
                this@BotonesActivity.finishAffinity()
            }
        }
//        GlobalScope.launch(Dispatchers.Main) {
//            val registro = Registro("0","Usuario en Botones", utils.getCurrentDate(), LocationService.lat0, LocationService.long0, preferencias.getGlobalData(this@BotonesActivity, "TM"))
//            //Aqui iba el mismo codigo de sendButtonData()
//            utils.doRequest(registro, this@BotonesActivity)
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            val uri: Uri = data?.data!!
            //Convierte la Uri a base64 para subir la imagen
            utils.foto = utils.converToBase64(this, uri)

            lifecycleScope.launch {
                //Si hay foto del marchamo fiscal subirlo sino seguir
                if(utils.foto != ""){
                    //Nuevo objeto tipo Foto
                    val foto = Foto(requestCode,"fotografia", utils.getCurrentDate(), utils.foto, utils.latitude, utils.longitude, preferencias.getGlobalData(this@BotonesActivity, "TM"))
                    utils.doRequest(foto, this@BotonesActivity)
                    println("FOTOGRAFIA BOTONES: \n"+utils.foto)
                    if (utils.infoFoto != null){
                        if(!utils.infoFoto!!.Success){
                            Toast.makeText(this@BotonesActivity, "No se pudo subir la foto", Toast.LENGTH_SHORT).show()
                            utils.progressDialog!!.dismiss()
                        } else {
                            Toast.makeText(this@BotonesActivity, "Foto subida con exito", Toast.LENGTH_SHORT).show()
                            utils.progressDialog!!.dismiss()
                        }
                    }
                    utils.foto = ""
                }
            }

        }else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            utils.progressDialog!!.dismiss()
        } else {
            Toast.makeText(this, "Acción cancelada", Toast.LENGTH_SHORT).show()
            utils.progressDialog!!.dismiss()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStop() {
        super.onStop()
        println("DESTRUIDA")
        preferencias.setGlobalData(this, "Sesion", "primera")
        utils.stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        connManager.unregisterNetworkCallback(networkCallback)
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.SERVICE_STOP
            startService(this)
            println("KKK    Termine las ubicaciones SERVICE.    KKK")
        }

        GlobalScope.launch {
            val registro = Registro("0","Cerro la APP el usuario", utils.getCurrentDate(), LocationService.lat0, LocationService.long0, preferencias.getGlobalData(this@BotonesActivity, "TM"))
            //Aqui iba el mismo codigo de sendButtonData()
            utils.doRequest(registro, this@BotonesActivity)
        }

        var tipo = 0
        var acc = "Se CERRO la app !!"
        var date = utils.getCurrentDate()
        var latt =  LocationService.lat0
        var longg = LocationService.long0
        var tkn = preferencias.getGlobalData(this@BotonesActivity, "TM")
        sendOnDestroyRegisger(tipo, acc, date, latt, longg, tkn)
        println("888888888888888888  CERRE LA APP  8888888888888888888")
    }

    fun sendOnDestroyRegisger(tipo: Int, acc:String, date:String, latt:String, longg:String, tkn:String) {
        val reApi = OneTimeWorkRequestBuilder<UpdloaWorker>()
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
    class UpdloaWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
        //val apiReq = inputData.ge.getString(KEY_IMAGE_URI)
        override suspend fun doWork(): Result {
            return try {
                val tipo = inputData.getInt("tipo", 0)
                val acc = inputData.getString("acc")
                val date = inputData.getString("date")
                val latt = inputData.getString("latt")
                val longg = inputData.getString("longg")
                val tkn = inputData.getString("tkn")
                val registro = Registro(tipo.toString(), acc.toString(), date.toString(), latt.toString(), longg.toString(), tkn.toString())
                RequestPermissions().registro(registro, null)
                Result.success()
            } catch (e: Exception){
                print("Exception en el Registro de Salida de la app.")
                Result.failure()
            } catch (e: java.lang.Exception){
                print("Exception en el Registro de Salida de la app.")
                Result.failure()
            }
        }

    }
    override fun onPause() {
        super.onPause()
        utils.stopLocationUpdates222(this)
        println("NO MAS UBIS NORMALES")
        if (!utils.stopLocationUpdates222(this)){
            preferencias.setGlobalData(this, "Sesion", "primera")
            finishAffinity()
        }
    }

    //Maneja los click a los botonoes
    override fun onItemClick(position: Int, name: String, id: Int) {
        val btns = preferencias.getGlobalData(this, "btns")
        //utils.infoBotones = if (Gson().fromJson(btns, BotonesRes::class.java) != null) Gson().fromJson(btns, BotonesRes::class.java) else null
        utils.infoBotones = if (Gson().fromJson(btns, BotonesRes::class.java) != null) Gson().fromJson(btns, BotonesRes::class.java) else utils.infoBotones

        if (utils.infoBotones == null) {
            Toast.makeText(this, "No se pudo Enviar el comando, recargue la pantalla de botones.", Toast.LENGTH_LONG).show()
            return
        }
        println("/// --- /// --- / ID: ${utils.infoBotones!!.Data[position].Id}")
        utils.progressDialog = ProgressDialog(this)
        utils.progressDialog!!.theme = ProgressDialog.THEME_LIGHT
        utils.progressDialog!!.mode = ProgressDialog.MODE_INDETERMINATE
        utils.progressDialog!!.setMessage("Enviando información...")
        utils.progressDialog!!.setTitle("Enviando datos")
        utils.progressDialog!!.show()
        lifecycleScope.launch {
            utils.latitude = ""
            utils.longitude = ""

            utils.onlyCurrentLocation(this@BotonesActivity){ result ->
                if(result == "true"){
                    var tipoEvento = "1"
                    if(name == "Pánico" || name == "Desperfectos" || name == "Puesto de Registro" || name == "Unidad Accidentada"){
                        tipoEvento = "2"
                    }

                    //Ya se tiene la UBICACION
                    val registro = Registro(tipoEvento, name, utils.getCurrentDate(), utils.latitude, utils.longitude, preferencias.getGlobalData(this@BotonesActivity, "TM"))

                    //Aqui iba el mismo codigo de sendButtonData()
                    sendButtonData(name, registro)

                } else {
                    //Ocurrio un error al obtener la ubicacion
                    utils.progressDialog!!.dismiss()
                    Toast.makeText(this@BotonesActivity, "No se pudo obtener la ubicación, intente de nuevo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    //FIN - Maneja los click a los botonoes

    suspend fun cerrarAlert(name: String){
        utils.onlyCurrentLocation(this) { result ->
            if(result == "true"){
                lifecycleScope.launch {
                    val registro = Registro("2", name, utils.getCurrentDate(), utils.latitude, utils.longitude, preferencias.getGlobalData(this@BotonesActivity, "TM"))
                    utils.doRequest(registro, this@BotonesActivity)
                    //preferencias.updateGlobalData(this, "Sesion", "segunda")
                    preferencias.setGlobalData(this@BotonesActivity, "Sesion", "segunda")

                    if(name == "Pánico"){
                        startActivity(Intent(this@BotonesActivity, InicioScreen::class.java))
                        //this@BotonesActivity.finish()
                    } else {
                        //this@BotonesActivity.finish()
                    }
                }
            } else {
                Toast.makeText(this@BotonesActivity, "Error al obtener la ubicación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun showDireccion(){
        val entrega = preferencias.getGlobalData(this, "direccion")

        /*val envio = Envio(preferencias.getGlobalData(this, "TM"))
        utils.getDireccion(envio, this)*/

        MaterialAlertDialogBuilder(this)
            .setTitle("Dirección de entrega")
            .setMessage(entrega)
            .setNeutralButton("Aceptar") { dialog, which ->
                // Respond to neutral button press
            }
            .show()
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        moveTaskToBack(true)
        println("ON BACK PRESSED")
    }


    private fun sendButtonData(name: String, registro: Registro){
        lifecycleScope.launch {
            when(name){
                "Dirección de entrega" ->{
                    showDireccion()
                    utils.progressDialog!!.dismiss()
                }
                "Llamada" -> utils.call(this@BotonesActivity, utils.tel)
                "Pánico" -> {
                    cerrarAlert("Pánico")
                    responseRequest(name)
                }
                "Puesto de Registro" -> {
                    cerrarAlert("Puesto de Registro")
                    responseRequest(name)
                }
                "Finalizar" -> {
                    /** Abrir nueva actividad **/
                    //startActivity(Intent(this@BotonesActivity, FinalizarActivity::class.java))
                    mostrarDialogoConValidacion(this@BotonesActivity, registro, name)
                }
                else ->{
                    utils.doRequest(registro, this@BotonesActivity)
                    responseRequest(name)
                }
            }
        }
    }

    private fun responseRequest(name:String){
        if(utils.infoRegistro == null){
            //println("VERRR_EE: "+utils.infoRegistro!!.Success)
            utils.progressDialog!!.dismiss()
            //Toast.makeText(this@BotonesActivity, "SICCAP ERROR: "+utils.errorString, Toast.LENGTH_SHORT).show()
            //progressDialog.dismiss()
        } else {
            utils.progressDialog!!.dismiss()
            if(utils.infoRegistro!!.Success){
                utils.progressDialog!!.dismiss()
                Toast.makeText(this@BotonesActivity, "Comando: "+name+" enviado con exito", Toast.LENGTH_SHORT).show()
                //progressDialog.dismiss()
            } else {
                utils.progressDialog!!.dismiss()
                Toast.makeText(this@BotonesActivity, "El comando: "+name+" no pudo enviarse", Toast.LENGTH_SHORT).show()
                //progressDialog.dismiss()
            }
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) {
            super.onLost(network)
            binding.version.setTextColor(Color.RED)
        }

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            binding.version.setTextColor(Color.DKGRAY)
        }
    }


    private fun showInputDialog() {
        var input = EditText(this).apply {
            hint = "Ingrese el número"
        }

        MaterialAlertDialogBuilder(this@BotonesActivity)
            .setTitle("Número finalización")
            .setView(input)
            .setMessage("Este es el código de finalización de su misión asgnada.")
            .setPositiveButton("Dar permiso") { dialog, which ->
                // Respond to neutral button press
                utils.openAppNotificationSettings(this)
            }
            .setNegativeButton("Salir") { dialog, which ->

            }
            .show()
    }

    override fun updateTextView() {
        println("huh")
    }

    override fun getTxtVersion(): TextView {
        return binding.version
    }

    fun mostrarDialogoConValidacion(context: Context, registro: Registro, name:String) {
        val editText = EditText(context).apply {
            hint = "Ingrese un el numero "
        }

        var messageText = "Ingrese el código asigando"

        if(preferencias.getGlobalData(this, "FinalEnviado") == "true"){
            utils.progressDialog!!.dismiss()
            Toast.makeText(this, "Código de finalización ya ha sido Enviado", Toast.LENGTH_LONG).show()
            return
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle("Ingrese el Cdógio de Finalización")
            .setMessage("Ingrese el código asigando")
            .setView(editText)
            .setPositiveButton("Aceptar", null) // Se asigna después para evitar cierre automático
            .setNegativeButton("Salir") { dialog, _ ->
                dialog.dismiss()
                utils.progressDialog!!.dismiss()
            }
            .create()

        dialog.setOnShowListener {
            val botonAceptar = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            botonAceptar.setOnClickListener {
                val inputCod = editText.text.toString().toInt()
                if (inputCod != preferencias.getGlobalData(this, "Codigo").toInt()) {
                    //editText.error = "Este campo no puede estar vacío"
                    dialog.setMessage("Número de finalizacón incorrecto")
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        //Toast.makeText(context, "Dato ingresado: $inputCod", Toast.LENGTH_SHORT).show()
                        dialog.setMessage("Finalización enviado con exito !")

                        registro.Accion.plus(" (${inputCod})")
                        utils.doRequest(registro, this@BotonesActivity)
                        responseRequest(name)
                        preferencias.setGlobalData(this@BotonesActivity, "FinalEnviado", "true")
                        //dialog.dismiss()
                    }
                    // Validación correcta, procesar el dato y cerrar el diálogo
                }
            }
        }

        dialog.show()
    }

}