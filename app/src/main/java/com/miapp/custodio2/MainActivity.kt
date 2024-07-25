package com.miapp.custodio2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.miapp.custodio2.ClasesRequest.Inicio
import com.miapp.custodio2.ClasesRequest.Registro
import com.miapp.custodio2.Utils.Actualizaciones
import com.miapp.custodio2.Utils.LocationService
import com.miapp.custodio2.Utils.Preferencias
import com.miapp.custodio2.Utils.RequestPermissions
import com.miapp.custodio2.databinding.ActivityMainBinding
import com.techiness.progressdialoglibrary.ProgressDialog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    var appUpdateManager: AppUpdateManager? =  null
    //Para que funciones el Binding
    private lateinit var binding: ActivityMainBinding

    //Utils donde esta funciones genericas
    val utils = RequestPermissions()
    val preferencias = Preferencias()
    val appUpdates = Actualizaciones()
    /*var permisos = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CALL_PHONE)*/
    //C2dm o TokenDevice
    //new
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val UPDATE_REQUEST_CODE = 503

    // ========================== MAIN ==========================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateApp(this)
        //appUpdates.updateApp(this)


        /*if (!utils.checkForGooglePlayServices(this)){
            //No tiene los servicios de Google
            MaterialAlertDialogBuilder(this)
                .setTitle("Sin servicios de Google")
                .setMessage("Su dispisitivo esta restringido de los servicios de Google, que la app necesita para funcionar. \nSe le recomienda instalar GSpace en su telefono para que la app funcione")
                .setNeutralButton("Aceptar") { dialog, which ->
                    // Respond to neutral button press
                }
                .show()
        } else {

        }*/
        when(preferencias.getGlobalData(this, "Sesion")){
            "reanudar" -> {
                println("SESION = REANUDAR")
                startActivity(Intent(this, BotonesActivity::class.java))
                this.finish()
            }
            "segunda" -> {
                println("SESION = SEGUNDA")
                println("sgunda")
            }
            else -> {
                preferencias.setGlobalData(this, "Sesion", "primera")
                println("SESION = PRIMERA")
                //println("PRUEBA-ERROR: ")
            }
        }

        binding.btnIngresar.isEnabled = true

        //C2DM - TOKE DEVICE
        utils.getTknDev(this){
            //binding.etUsuario.setText(it)
            println("TOKEN DISPOSITIVO: "+it)
        }

        //binding.etUsuario.setText(utils.tokenDevice)

        //solcitarPermisos permite SOLICITAR un array de PERMISOS al usuario
        /*utils.solicitarPermisos(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CALL_PHONE), 122)*/

        /**utils.solicitarPermisos(this)**/
        utils.askForPermissions(this)

        //Empieza a obtener las ubicaicones
        binding.etUsuario.setOnFocusChangeListener { view, b ->
            //binding.txtFrase.setText(utils.tokenDevice)
            //Toast.makeText(this, "Toque Usuario", Toast.LENGTH_SHORT).show()

            utils.startLocationUpdates(this)
            println("K;LKK")
        }

        //BOTON Ingresar
        binding.btnIngresar.setOnClickListener {
//            Intent(applicationContext, LocationService::class.java).apply {
//                action = LocationService.SERVICE_START
//                startService(this)
//            }
            if(utils.checkGrantedpermissions(this)){
                if (utils.isLocationCallbackNull()){
                    utils.startLocationUpdates(this)
                } else {
                    println("El location del EtUser fucniono, no me active.")
                }

                utils.progressDialog = ProgressDialog(this)
                utils.progressDialog!!.theme = ProgressDialog.THEME_LIGHT
                utils.progressDialog!!.mode = ProgressDialog.MODE_INDETERMINATE
                utils.progressDialog!!.setMessage("Verfificando mision...")
                utils.progressDialog!!.setTitle("")
                utils.progressDialog!!.show()

                //utils.stopLocationUpdates()
                if (utils.stopLocationUpdates222(this)){
                    lifecycleScope.launch {
                        inicioSesion()
                    }
                }

            }
            /**if(utils.verificarPermisos(this)){
                runBlocking {
                    inicioSesion()
                }
            }**/
//            if(utils.checkGrantedpermissions(this)){
////                runBlocking {
////                    inicioSesion()
////                }
//                lifecycleScope.launch {
//                    inicioSesion()
//                }
//            }
        }


    }
    // ========================== FIN DEL MAIN ==========================

    override fun onStop() {
        super.onStop()
        // When status updates are no longer needed, unregister the listener.
        if (appUpdateManager != null){
            appUpdateManager!!.unregisterListener(listener)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 236) {
            if (resultCode == Activity.RESULT_OK) {
                // La actualización se realizó correctamente
                println("UpReq: estoy en el IF del Activity Result, Activity.RESULT_OK")
            } else {
                // La actualización falló
                println("UpReq: estoy en el else del Activity Result, el Activity.RESULT_OK fue falso")
            }
        }

        if (requestCode == 2025){
            println("HELLOOW HERE ENTREEE")
            if (resultCode != Activity.RESULT_OK){
                Toast.makeText(this, "La actualizacion fallo: "+resultCode, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            100 -> {
                var permisosDados = NotificationManagerCompat.from(this).areNotificationsEnabled()
                if (!permisosDados){
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Permiso de notificación requerido")
                        .setMessage("La app necesita este permiso para funciones criticas, como Pánico y ubicación")
                        .setPositiveButton("Dar permiso") { dialog, which ->
                            // Respond to neutral button press
                            utils.openAppNotificationSettings(this)
                        }
                        .setNegativeButton("Salir") { dialog, which ->

                        }
                        .show()

                }
            }
        }
    }


    // ========================== FUNCIONES DEL MAIN ==========================

    private fun setPreferencias(){
        //PREFERENCIAS
        //Token Mision
        preferencias.setGlobalData(this@MainActivity, "TM", utils.infoAutenticar!!.Token)
        //Rui
        preferencias.setGlobalData(this@MainActivity, "Rui", utils.infoAutenticar!!.Rui.toString())
        //nomrbre custodio
        preferencias.setGlobalData(this@MainActivity, "NombreCustodio", utils.infoAutenticar!!.NombreCustodio)
        //NOMB
        preferencias.setGlobalData(this, "Nomb", utils.infoAutenticar!!.Nombramiento)
        //Datos para Mision
        preferencias.setGlobalData(this@MainActivity, "au_Cabezal", utils.infoAutenticar!!.Placa)
        preferencias.setGlobalData(this@MainActivity, "au_Tc", utils.infoAutenticar!!.PlacaTC)
        preferencias.setGlobalData(this, "au_Contenedor", utils.infoAutenticar!!.NumeroContenedor)
        preferencias.setGlobalData(this@MainActivity, "au_Piloto", utils.infoAutenticar!!.Piloto)
        preferencias.setGlobalData(this, "au_Arma", utils.infoAutenticar!!.Escopeta)
        //Editables
        preferencias.setGlobalData(this, "au_Tel", utils.infoAutenticar!!.TelefonoPiloto)
        preferencias.setGlobalData(this, "au_Sellado", utils.infoAutenticar!!.Sellado)
        preferencias.setGlobalData(this, "au_Fiscal", utils.infoAutenticar!!.MarchamoFiscal)
        println("au_Fiscal="+utils.infoAutenticar!!.MarchamoFiscal)
        preferencias.setGlobalData(this, "au_Gps", utils.infoAutenticar!!.MarchamoGPS)
        preferencias.setGlobalData(this, "au_Notas", utils.infoAutenticar!!.Notas)
        //otras
        preferencias.setGlobalData(this, "verM", "false")
    }

    var contadorLocation = 0

    private suspend fun inicioSesion(){

        contadorLocation++
        if(utils.longitude == "" && utils.latitude == ""){
            //FuckProgress(this@MainActivity).light("Loading...")
            utils.progressDialog!!.dismiss()

            println("contadorLocation: "+contadorLocation)

            // Uso del método
            val isLocationEnabled = utils.isLocationEnabled(this)
            if (isLocationEnabled) {
                // La ubicación está habilitada
                println("La ubicación está habilitada")
                Toast.makeText(this, "Listo, Permiso concedido y Ubicacion activa", Toast.LENGTH_LONG).show()
                utils.latitude = "0.0001"
                utils.longitude = "0.0001"
            } else {
                // La ubicación no está habilitada
                println("La ubicación no está habilitada")

                MaterialAlertDialogBuilder(this)
                    .setTitle("No se ha logrado conseguir la ubicación")
                    .setMessage("Revise que la opción de Ubicacion ó GPS del telefono este activa")
                    .setNeutralButton("Aceptar") { dialog, which ->
                    }
                    .show()
                //Toast.makeText(this, "Revise que la Ubicacion este activa", Toast.LENGTH_LONG).show()
                utils.latitude = "0.0001"
                utils.longitude = "0.0001"
            }

            //Toast.makeText(this, "Revise el acceso a la ubicación e intente de nuevo", Toast.LENGTH_LONG).show()
            //utils.startLocationUpdates(this)
        } else {
            utils.stopLocationUpdates222(this)
            //utils.stopLocationUpdates()
            binding.btnIngresar.isEnabled = false
            //utils.startLocationUpdates(this@MainActivity)
//            println("ESA: "+contadorLocation)
//            if (contadorLocation == 3){
//                utils.latitude = "1.000"
//                utils.longitude = "1.000"
            println("KOLA: "+utils.latitude)
//            }

            val autenticar = Inicio(utils.tokenDevice, "false", utils.latitude, utils.longitude, binding.etPalabra.text.toString(),
                binding.etPassword.text.toString(), binding.etUsuario.text.toString())

            utils.doRequest(autenticar, this@MainActivity)
            /*utils.startLocationUpdates(this@MainActivity)*/
            if (utils.infoAutenticar != null){
                var info = utils.infoAutenticar

                //info!!.Rui = 0

                if (info!!.Rui == null || info!!.Rui == 0){
                    utils.progressDialog?.dismiss()
                    Toast.makeText(this@MainActivity, "Mision no asignada", Toast.LENGTH_SHORT).show()
                    binding.btnIngresar.isEnabled = true
                    /** 3 DATOS QUE OBTENDRA INFO ACT **/
                    println("PRIMER USOOOOOOOOOOO: "+info.PrimerUso)
                    preferencias.setGlobalData(this@MainActivity, "PrimerUso",  info.PrimerUso)
                    preferencias.setGlobalData(this@MainActivity, "UltimoUso", info.UltimoUso)
                    preferencias.setGlobalData(this@MainActivity, "TotalServiciosApp", info.TotalServiciosApp)
                    preferencias.setGlobalData(this@MainActivity, "TotalServiciosMes", info.TotalServiciosApp)
                    preferencias.setGlobalData(this@MainActivity, "NombreCustodio", info.NombreCustodio)
                    binding.btnIngresar.isEnabled = true
                    val intent = Intent(this@MainActivity, InfoActivity::class.java)
                    startActivity(intent)
                    this.finish()

                }

                //Condiciones
                if(info!!.Mensaje == "USUARIO O PASSWORD INCORRECTO O NO EXISTE"){
                    utils.progressDialog!!.dismiss()
                    Toast.makeText(this@MainActivity, info.Mensaje, Toast.LENGTH_SHORT).show()
                    binding.btnIngresar.isEnabled = true

                } else if(info.Rui.toInt() != -1 && info.Rui.toInt() != 1000){
                    val apiLevel = Build.VERSION.SDK_INT
                    var infoDevice = "Inicio en "+getString(R.string.version)+" Android "+apiLevel
                    println("--------INFO DEVICE: "+infoDevice)

                    utils.progressDialog!!.dismiss()
                    /*utils.stopLocationUpdates()*/
                    //utils.stopLocationUpdates()
                    println("RUI: "+info.Rui)
                    println("ENTRE AQUI")
                    utils.tel = utils.infoAutenticar!!.TelefonoEmergencia
                    setPreferencias()
                    println("ABRIENDO DATOS MISION ACTIVITY....")
                    //Iniciar Actividad del Datos Mision

                    val registro = Registro("0",infoDevice, utils.getCurrentDate(), utils.latitude, utils.longitude, preferencias.getGlobalData(this@MainActivity, "TM"))
                    //Aqui iba el mismo codigo de sendButtonData()
                    utils.doRequest(registro, this@MainActivity)

                    val intent = Intent(this@MainActivity, MisionActivity::class.java)
                    startActivity(intent)
                    this@MainActivity.finish()
                } else {
                    utils.progressDialog!!.dismiss()
                    Toast.makeText(this@MainActivity, "Mision no asignada", Toast.LENGTH_SHORT).show()
                    binding.btnIngresar.isEnabled = true
                    /** 3 DATOS QUE OBTENDRA INFO ACT **/
                    println("PRIMER USOOOOOOOOOOO: "+info.PrimerUso)
                    preferencias.setGlobalData(this@MainActivity, "PrimerUso",  info.PrimerUso)
                    preferencias.setGlobalData(this@MainActivity, "UltimoUso", info.UltimoUso)
                    preferencias.setGlobalData(this@MainActivity, "TotalServiciosApp", info.TotalServiciosApp)
                    preferencias.setGlobalData(this@MainActivity, "TotalServiciosMes", info.TotalServiciosApp)
                    preferencias.setGlobalData(this@MainActivity, "NombreCustodio", info.NombreCustodio)
                    binding.btnIngresar.isEnabled = true
                    val intent = Intent(this@MainActivity, InfoActivity::class.java)
                    startActivity(intent)
                    this.finish()
                }
                //Fin Condiciones
            } else {
                utils.progressDialog!!.dismiss()
                Toast.makeText(this@MainActivity, "Algo salio mal...", Toast.LENGTH_SHORT).show()
                binding.txtFrase.setText(utils.errorString)
                binding.btnIngresar.isEnabled = true
            }
        }
    }

    private fun updateApp(act: Activity){
        println("WENAAASSS")
        appUpdateManager = AppUpdateManagerFactory.create(act)
        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager!!.appUpdateInfo

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(
                    AppUpdateType.FLEXIBLE)) {
                // Request the update.
                println("UPDATE AVAILABLE")
                appUpdateManager!!.startUpdateFlowForResult(
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfo,
                    // an activity result launcher registered via registerForActivityResult
                    AppUpdateType.FLEXIBLE,
                    // Or pass 'AppUpdateType.FLEXIBLE' to newBuilder() for
                    // flexible updates.
                    act, 2025)
            }
        }
        appUpdateInfoTask.addOnFailureListener {
            println("JAJAJ FALLE: "+it.message)
        }

        // Before starting an update, register a listener for updates.
        appUpdateManager!!.registerListener(listener)

    }

    val listener: (InstallState) -> Unit = { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            // After the update is downloaded, show a notification
            // and request user confirmation to restart the app.
            popupSnackbarForCompleteUpdate()
        }
    }

    // Displays the snackbar notification and call to action.
    fun popupSnackbarForCompleteUpdate() {
        println("AWAWAWWWWWWW")
        Snackbar.make(

            findViewById(R.id.pico),
            "Reinicie la app. Una nueva version ha sido descargada",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("RESTART") { appUpdateManager!!.completeUpdate() }
            setActionTextColor(resources.getColor(R.color.white))
            show()
        }
    }


    //Solicita la ubicacion de este momento, en raros casos es null
    //https://developer.android.com/training/location/retrieve-current

}