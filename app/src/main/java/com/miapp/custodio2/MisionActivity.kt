package com.miapp.custodio2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.CheckBox
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.miapp.custodio2.ClasesRequest.*
import com.miapp.custodio2.Utils.Preferencias
import com.miapp.custodio2.Utils.RequestPermissions
import com.miapp.custodio2.databinding.ActivityMisionBinding
import com.techiness.progressdialoglibrary.ProgressDialog
import java.lang.Exception
import androidx.lifecycle.lifecycleScope
import com.miapp.custodio2.Utils.LocationService
import kotlinx.coroutines.launch


class MisionActivity : AppCompatActivity() {
    //Para que funciones el Binding
    private lateinit var binding: ActivityMisionBinding
    //Utils donde esta funciones genericas
    val utils = RequestPermissions()
    val preferencias = Preferencias()
    var sellado = "nada"
    //new
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var checkboxPredio: CheckBox

    var esPrimeraVez = false
    //var progressDialog2: android.app.ProgressDialog? = null
    //Progress dialog
    //private lateinit var progressDialog: ProgressDialog

    // ========================== MAIN MISION ==========================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMisionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Settings progrss dialog foto upload
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.SERVICE_START
            startService(this)
        }

        checkboxPredio = binding.checkboxUsoPredio

        println("Hola estoy en MISION")
        //Pone la longitud y latitud en ""
        utils.latitude = ""
        utils.longitude = ""

        println("Hola habilite el boton")
        binding.btnSiguiente.isEnabled = true

        binding.tvNombreCustodio.text = preferencias.getGlobalData(this, "NombreCustodio")

        /*Toast.makeText(this, "Token Mision: "+preferencias.getGlobalData(this, "TM"), Toast.LENGTH_SHORT).show()*/
        //Empzara a tomar las UBICACIONES
        //Toast.makeText(this, "Bienvenido MISION", Toast.LENGTH_SHORT).show()
        //utils.startLocationUpdates(this)

        //○ Seccion de Carga de datos ○
        if(preferencias.getGlobalData(this@MisionActivity, "verM") == "true"){
            //Empezara los location updates en la actividad
            utils.startLocationUpdates(this)
            //Cargara los datos NO Editables del /Autenticar ya que verM sera true
            loadDatos()
            //
            loadDatosUpdated()
        } else {
            //Empezara los location updates en la actividad
            utils.startLocationUpdates(this)

            when(preferencias.getGlobalData(this, "Sesion")){
                "segunda" -> {
                    esPrimeraVez = false
                    loadDatosUpdated()
                    startActivity(Intent(this, BotonesActivity::class.java))
                    this.finish()
                }
                "primera" -> {
                    esPrimeraVez = true
                    //Carga los datos del response de /Autenticar para mostrarlo en el Mision
                    loadDatos()
                }
                else -> {esPrimeraVez = false}
            }
        }
        //○↑ -alt24 FIN Seccion de Carga de datos ↑○ alt9

        //Pondra El marchamo fiscal en 0 si esta en "" para que no de error a la hora de subir una foto
        if(preferencias.getGlobalData(this, "au_Fiscal") == "" || preferencias.getGlobalData(this, "au_Fiscal") == "--- Foto subida con exito ---"){
            println("ENTRE CONDICION 1!!!!")
            binding.etMarchamoFiscal.setText("0")
            println("TEXXTOOO:"+binding.etMarchamoFiscal.text.toString())
        } else {
            println("au NO ES VACIO")
        }

        //Obtiene la Direccion de Enrtrega
        lifecycleScope.launch {
            if(preferencias.getGlobalData(this@MisionActivity, "Sesion") == "primera"){
                val envio = Envio(preferencias.getGlobalData(this@MisionActivity, "TM"))
                utils.getDireccion(envio, this@MisionActivity)
                if(utils.infoDireccion != null){
                    binding.tvMostrarDireccion.setText(utils.infoDireccion!!.Message)
                    preferencias.setGlobalData(this@MisionActivity, "direccion", utils.infoDireccion!!.Message)
                } else {
                    binding.tvMostrarDireccion.setText("Error de resolución de host: "+getString(R.string.UrlBase))
                }
            } else {
                val entrega = preferencias.getGlobalData(this@MisionActivity, "direccion")
                binding.tvMostrarDireccion.setText(entrega)
            }
        }

        binding.ivMarchamoFiscalFoto.setOnClickListener {
            utils.pickImage(this, 2)
            //progressDialog.show()
        }

        binding.ivFotoCabezal.setOnClickListener {
            utils.pickImage(this, 3)
        }

        //FIN - Obtiene la Direccion de Enrtrega
        binding.btnSiguiente.setOnClickListener {
//            GlobalScope.launch {
//                Intent(applicationContext, LocationService::class.java).apply {
//                    action = LocationService.SERVICE_START
//                    startService(this)
//                }
//            }
            try {
                /*utils.stopLocationUpdates()*/
                /*runBlocking {*/
                //showDialog()
                utils.progressDialog = ProgressDialog(this)
                utils.progressDialog!!.theme = ProgressDialog.THEME_LIGHT
                utils.progressDialog!!.mode = ProgressDialog.MODE_INDETERMINATE
                utils.progressDialog!!.setMessage("Cargando...")
                utils.progressDialog!!.setTitle("")
                utils.progressDialog!!.show()
                Codigo2()

            } catch (e: Exception){
                utils.progressDialog!!.dismiss()
                binding.btnSiguiente.isEnabled = true
                Toast.makeText(this, "Algo salio mal: long"+utils.longitude+" lat: "+utils.latitude, Toast.LENGTH_LONG).show()
            }

        }

    }

    private fun showDialog(){
        utils.progressDialog = null
        utils.progressDialog = ProgressDialog(this)
        utils.progressDialog!!.theme = ProgressDialog.THEME_LIGHT
        utils.progressDialog!!.mode = ProgressDialog.MODE_INDETERMINATE
        utils.progressDialog!!.setMessage("Cargando...")
        utils.progressDialog!!.setTitle("")
        utils.progressDialog!!.show()
    }

//    private fun Codigo(){
//        binding.btnSiguiente.isEnabled = false
//        //Detecta que boton de sellado esta seleccionado
//        if(binding.rbSelladoCandado.isChecked){
//            sellado = "Candado"
//        }
//        if(binding.rbSelladoMarchamo.isChecked){
//            sellado = "Marchamo"
//        }
//
//        if(utils.latitude == "" && utils.longitude == ""){
//            //onlyLocation()
//            utils.onlyCurrentLocation(this@MisionActivity){
//                if(it == "true"){
//                    println("tengo ubicacion")
//                    runBlocking {
//                        sendDataMision()
//                    }
//                } else {
//                    utils.progressDialog!!.dismiss()
//                    Toast.makeText(this@MisionActivity, "No se obtuvo localización, intente de nuevo", Toast.LENGTH_LONG).show()
//                }
//            }
//        } else {
//            runBlocking {
//                sendDataMision()
//            }
//        }
//    }
    private fun Codigo2(){
        binding.btnSiguiente.isEnabled = false
        //Detecta que boton de sellado esta seleccionado
        if(binding.rbSelladoCandado.isChecked){
            sellado = "Candado"
        }
        if(binding.rbSelladoMarchamo.isChecked){
            sellado = "Marchamo"
        }

        if(utils.latitude == "" && utils.longitude == ""){
            //onlyLocation()
            utils.onlyCurrentLocation(this@MisionActivity){
                if(it == "true"){
                    println("tengo ubicacion")
                    lifecycleScope.launch {
                        sendDataMision()
                    }
                } else {
                    utils.progressDialog!!.dismiss()
                    Toast.makeText(this@MisionActivity, "No se obtuvo localización, intente de nuevo", Toast.LENGTH_LONG).show()
                    binding.btnSiguiente.isEnabled = true
                }
            }
        } else {
            lifecycleScope.launch {
                sendDataMision()
            }
        }
    }
    // ========================== FIN DEL MISION ==========================

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, BotonesActivity::class.java)
        startActivity(intent)
        this.finish()
    }

    //Aqui se obtiene la imagen seleccionada o tomada con la camara
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            val uri: Uri = data?.data!!

            //println("SIZE URI: "+uri.toString())
            //println("SIZE URI: "+uri.toFile())

            utils.foto = utils.converToBase64(this, uri)

            lifecycleScope.launch {
                //Si hay foto del marchamo fiscal subirlo sino seguir

                utils.progressDialog!!.dismiss()
                if(utils.foto != ""){
                    //Nuevo objeto tipo Foto
                        /** En ves de Accion, ahora el requestCode trae el ID del tipo de foto: 2 es de MarchamoFiscal y 3 la del Cabezal**/
                    //val foto = Foto("MarchamoFiscal", utils.getCurrentDate(), utils.foto, utils.latitude, utils.longitude, preferencias.getGlobalData(this@MisionActivity, "TM"))
                    var foto = Foto(requestCode, "MarchamoFiscal", utils.getCurrentDate(), utils.foto, utils.latitude, utils.longitude, preferencias.getGlobalData(this@MisionActivity, "TM"))
                    if(requestCode == 3){
                        foto = Foto(requestCode, "Cabezal", utils.getCurrentDate(), utils.foto, utils.latitude, utils.longitude, preferencias.getGlobalData(this@MisionActivity, "TM"))
                    } else if (requestCode == 2){
                        foto = Foto(requestCode, "MarchamoFiscal", utils.getCurrentDate(), utils.foto, utils.latitude, utils.longitude, preferencias.getGlobalData(this@MisionActivity, "TM"))
                    }


                    println("DATOS QUE SE ENVIAN")
                    //utils.doRequest(foto, this@MisionActivity)
                    println("Id: "+requestCode)
                    println("Accion: MarchamoFiscal")
                    println("Fecha: "+utils.getCurrentDate())

                    println("Latitud: "+utils.latitude)
                    println("Longitud: "+utils.latitude)
                    println("Token: "+preferencias.getGlobalData(this@MisionActivity, "TM"))
                    println("Foto: \n"+utils.foto)

                    if (esPrimeraVez){
                        /** Se agregar la foto al Array y luego se suben todas en primeraSesion() **/
                        utils.listOfFotos.add(foto)
                        println("PRIMERA EN MISION")
                    } else {
                        println("YA NO ES PRIMERA EN MISION")
                        utils.doRequest(foto, this@MisionActivity)

                        if (utils.infoFoto != null){
                            if(!utils.infoFoto!!.Success){
                                Toast.makeText(this@MisionActivity, "No se pudo subir la foto", Toast.LENGTH_SHORT).show()
                                Toast.makeText(this@MisionActivity, utils.infoFoto!!.Message, Toast.LENGTH_LONG).show()

                                /** MOSTRAR EL ERROR DE LA FOTO **/
                                binding.etNotas.isFocusableInTouchMode = true
                                binding.etNotas.isFocusable = true
                                binding.etNotas.setText(utils.infoFoto!!.Message)

                                println("ERROR: "+utils.infoFoto!!.Message)
                                utils.progressDialog!!.dismiss()
                            } else {
                                //binding.etMarchamoFiscal.setText("--- Foto subida con exito ---")
                                Toast.makeText(this@MisionActivity, "Foto subida con exito", Toast.LENGTH_SHORT).show()
                                utils.progressDialog!!.dismiss()
                            }
                        }

                    }
                    utils.foto = ""
                }
            }

        }else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data)+" intentelo de nuevo", Toast.LENGTH_SHORT).show()
            utils.progressDialog!!.dismiss()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
            utils.progressDialog!!.dismiss()
        }
    }

    // ========================== FUNCIONES DEL MISION ACTIVITY ==========================
    //--NOTA:verM indica cuando Mision se abre desde la parte de Botones--//

    //Carga los datos que viene del response de /Autenticar
    private fun loadDatos(){
        //Estos datos al ser los NO editables no imtpota si viene o no de Botones siempre los cargara
        binding.tvRui.setText("Rui: "+preferencias.getGlobalData(this, "Rui"))
        binding.tvNomb.setText("Nomb: "+preferencias.getGlobalData(this, "Nomb"))

        binding.etPlacaCabezal.setText(preferencias.getGlobalData(this, "au_Cabezal"))
        binding.etPlacaTC.setText(preferencias.getGlobalData(this, "au_Tc"))
        binding.etNumContenedor.setText(preferencias.getGlobalData(this, "au_Contenedor"))
        binding.etNombrePiloto.setText(preferencias.getGlobalData(this, "au_Piloto"))
        binding.etNumArma.setText(preferencias.getGlobalData(this, "au_Arma"))
        /*Esto no iba, se supone que si estas en botones y regresas el numero del fiscal aparece
        * pero ante un error que en emulador no ocurre se supone que lo ponia en 0, asi que por si
        * acaso se coloco aqui tambien esto, */
        //binding.etMarchamoFiscal.setText("0000000000")
        //Cuando sea la primera vez verM != true ó verM == false (que no venga de Botones) cargara los datos editables tambien con lo que
        //venga del /Autenticar
        if (preferencias.getGlobalData(this, "verM") != "true"){
            //Editables
            binding.etTelTransportista.setText(preferencias.getGlobalData(this, "au_TelTrans"))
            binding.etNombreTransportista.setText(preferencias.getGlobalData(this, "au_Trans"))
            checkboxPredio.isChecked = preferencias.getGlobalData(this, "au_UsoPredio").toBoolean()
            binding.etNumArma.setText(preferencias.getGlobalData(this, "au_Arma"))

            binding.etTelefonoPiloto.setText(preferencias.getGlobalData(this, "au_Tel"))
            binding.etMarchamoFiscal.setText(preferencias.getGlobalData(this, "au_Fiscal"))
            binding.etMarchamoGps.setText(preferencias.getGlobalData(this, "au_Gps"))
            binding.etNotas.setText(preferencias.getGlobalData(this, "au_Notas"))
            when(preferencias.getGlobalData(this, "au_Sellado")){
                "" -> binding.rbSelladoMarchamo.isChecked = true
                "Candado" -> binding.rbSelladoCandado.isChecked = true
                "Marchamo" -> binding.rbSelladoMarchamo.isChecked = true
            }
        }
    }

    //Se encarga de cargar los datos que fueron actualizados
    private fun loadDatosUpdated(){

        if (binding.etTelTransportista.text.toString() != preferencias.getGlobalData(this, "TelTrans")){
            binding.etTelTransportista.setText(preferencias.getGlobalData(this, "TelTrans"))
        }
        if (binding.etNombreTransportista.text.toString() != preferencias.getGlobalData(this, "Trans")){
            binding.etNombreTransportista.setText(preferencias.getGlobalData(this, "Trans"))
        }
        if (checkboxPredio.isChecked != preferencias.getGlobalData(this, "UsoPredio").toBoolean()){
            checkboxPredio.isChecked = preferencias.getGlobalData(this, "UsoPredio").toBoolean()
        }
        if (binding.etNumArma.text.toString() != preferencias.getGlobalData(this, "Arma")){
            binding.etNumArma.setText(preferencias.getGlobalData(this, "Arma"))
        }


        if (binding.etTelefonoPiloto.text.toString() != preferencias.getGlobalData(this, "Tel")){
            binding.etTelefonoPiloto.setText(preferencias.getGlobalData(this, "Tel"))
        }
        if (checkboxPredio.isChecked != preferencias.getGlobalData(this, "UsoPredio").toBoolean()){
            checkboxPredio.isChecked = preferencias.getGlobalData(this, "UsoPredio").toBoolean()
        }

        if (binding.etMarchamoFiscal.text.toString() != preferencias.getGlobalData(this, "M_Fiscal")){
            binding.etMarchamoFiscal.setText(preferencias.getGlobalData(this, "M_Fiscal"))
        }
        if (binding.etMarchamoGps.text.toString() != preferencias.getGlobalData(this, "M_Gps")){
            binding.etMarchamoGps.setText(preferencias.getGlobalData(this, "M_Gps"))
        }
        if (binding.etNotas.text.toString() != preferencias.getGlobalData(this, "Notas")){
            binding.etNotas.setText(preferencias.getGlobalData(this, "Notas"))
        }
        if (sellado != preferencias.getGlobalData(this, "Sellado")){
            if (preferencias.getGlobalData(this, "Sellado") == "Marchamo"){
                binding.rbSelladoMarchamo.isChecked = true
                binding.rbSelladoCandado.isChecked = false
            } else {
                binding.rbSelladoMarchamo.isChecked = false
                binding.rbSelladoCandado.isChecked = true
            }
        }
    }

    //Establece los campos editables como preferencias
    //Se establcen la primera vez que se inica sesion Sesion = primera
    private fun setPreferencias(){
        preferencias.setGlobalData(this, "TelTrans", binding.etTelTransportista.text.toString())
        preferencias.setGlobalData(this, "Trans", binding.etNombreTransportista.text.toString())
        preferencias.setGlobalData(this, "UsoPredio", checkboxPredio.isChecked.toString())
        preferencias.setGlobalData(this, "Arma", binding.etNumArma.text.toString())

        preferencias.setGlobalData(this, "Tel", binding.etTelefonoPiloto.text.toString())
        preferencias.setGlobalData(this, "Sellado", sellado)
        preferencias.setGlobalData(this, "M_Fiscal", binding.etMarchamoFiscal.text.toString())
        preferencias.setGlobalData(this, "M_Gps", binding.etMarchamoGps.text.toString())
        preferencias.setGlobalData(this, "Notas", binding.etNotas.text.toString())
    }

    //Actualizara las preferencias editables que han sido modificadas
    //Este se usa mas que nada cuando el Sesion es de tipo 3 - Cuendo se esta en Botones
    private suspend fun updateData(){
        if (binding.etTelTransportista.text.toString() != preferencias.getGlobalData(this, "TelTrans")){
            //Update request
            val update = Update("TELEFONOTRANSPORTE", preferencias.getGlobalData(this, "TelTrans"), binding.etTelTransportista.text.toString(),
                utils.latitude, utils.longitude, preferencias.getGlobalData(this, "TM"))
            utils.doRequest(update, this)
            preferencias.setGlobalData(this, "TelTrans", binding.etTelTransportista.text.toString())
            println("UPDATE TELEFONOTRANSPORTE= Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
        }
        if (binding.etNombreTransportista.text.toString() != preferencias.getGlobalData(this, "Trans")){
            //Update request
            val update = Update("TRANSPORTE", preferencias.getGlobalData(this, "Trans"), binding.etNombreTransportista.text.toString(),
                utils.latitude, utils.longitude, preferencias.getGlobalData(this, "TM"))
            utils.doRequest(update, this)
            preferencias.setGlobalData(this, "Trans", binding.etNombreTransportista.text.toString())
            println("UPDATE TRANSPORTE= Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
        }
        if (checkboxPredio.isChecked.toString() != preferencias.getGlobalData(this, "UsoPredio")){
            var predio = "0"
            if(checkboxPredio.isChecked.toString() == "true"){
                predio = "1"
            }
            //Update request
            val update = Update("USAPREDIO", preferencias.getGlobalData(this, "UsoPredio"), predio,
                utils.latitude, utils.longitude, preferencias.getGlobalData(this, "TM"))
            utils.doRequest(update, this)
            preferencias.setGlobalData(this, "UsoPredio", checkboxPredio.isChecked.toString())
            println("UPDATE Tel= Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
        }
        //ARMA
        if (binding.etNumArma.text.toString() != preferencias.getGlobalData(this, "Arma")){
            //Update request
            val update = Update("ESCOPETA", preferencias.getGlobalData(this, "Arma"), binding.etNumArma.text.toString(),
                utils.latitude, utils.longitude, preferencias.getGlobalData(this, "TM"))

            utils.doRequest(update, this)
            //Actualiza las preferencias
            /*preferencias.updateGlobalData(this, "Tel", binding.etNumArma.text.toString())*/
            preferencias.setGlobalData(this, "Arma", binding.etNumArma.text.toString())
            println("UPDATE Tel= Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
        }

        //TELEFONO
        if (binding.etTelefonoPiloto.text.toString() != preferencias.getGlobalData(this, "Tel")){
            //Update request
            val update = Update("TELEFONO", preferencias.getGlobalData(this, "Tel"), binding.etTelefonoPiloto.text.toString(),
                utils.latitude, utils.longitude, preferencias.getGlobalData(this, "TM"))

            utils.doRequest(update, this)
            //Actualiza las preferencias
            /*preferencias.updateGlobalData(this, "Tel", binding.etTelefonoPiloto.text.toString())*/
            preferencias.setGlobalData(this, "Tel", binding.etTelefonoPiloto.text.toString())
            println("UPDATE Tel= Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
        }
        //SELLADO
        if (sellado != preferencias.getGlobalData(this, "Sellado")){
            //Update request
            val update = Update("Sellado", preferencias.getGlobalData(this, "Sellado"), sellado, utils.latitude,
                utils.longitude, preferencias.getGlobalData(this, "TM"))

            utils.doRequest(update, this)
            //Actualiza las preferencias
            /*preferencias.updateGlobalData(this, "Sellado", sellado)*/
            preferencias.setGlobalData(this, "Sellado", sellado)
            println("UPDATE Sellado= Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
        }
        //MARCHAMO FISCAL
        if (binding.etMarchamoFiscal.text.toString() != preferencias.getGlobalData(this, "M_Fiscal")){
            //Update request
            val update = Update("MARCHAMOFISCAL", preferencias.getGlobalData(this, "M_Fiscal"), binding.etMarchamoFiscal.text.toString(),
                utils.latitude, utils.longitude, preferencias.getGlobalData(this, "TM"))

            utils.doRequest(update, this)
            //Actualiza las preferencias
            /*preferencias.updateGlobalData(this, "M_Fiscal", binding.etMarchamoFiscal.text.toString())*/
            preferencias.setGlobalData(this, "M_Fiscal", binding.etMarchamoFiscal.text.toString())
            println("UPDATE M Fiscal= Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
        }
        //MARCHAMNO GPS
        if (binding.etMarchamoGps.text.toString() != preferencias.getGlobalData(this, "M_Gps")){
            //Update request
            val update = Update("MarchamoGps", preferencias.getGlobalData(this, "M_Gps"), binding.etMarchamoGps.text.toString(),
                utils.latitude, utils.longitude, preferencias.getGlobalData(this, "TM"))

            utils.doRequest(update, this)
            //Actualiza las preferencias
            /*preferencias.updateGlobalData(this, "M_Gps", binding.etMarchamoGps.text.toString())*/
            preferencias.setGlobalData(this, "M_Gps", binding.etMarchamoGps.text.toString())
            println("UPDATE M Gps= Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
        }
        //NOTAS
        if (binding.etNotas.text.toString() != preferencias.getGlobalData(this, "Notas")){
            //Update request
            val update = Update("Notas", preferencias.getGlobalData(this, "Notas"), binding.etNotas.text.toString(), utils.latitude,
                utils.longitude, preferencias.getGlobalData(this, "TM"))

            utils.doRequest(update, this)
            //Actualiza las preferencias
            /*preferencias.updateGlobalData(this, "Notas", binding.etNotas.text.toString())*/
            preferencias.setGlobalData(this, "Notas", binding.etNotas.text.toString())
            println("UPDATE Notas= Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
        }
        utils.stopLocationUpdates()
    }

    private suspend fun updateData1Sesion(){
        if (utils.tipoErrorString == "Mis/1"){
            println("OHAYOOOO")
            return
        } else {
            println("UNNN_OHAYOOOO: "+utils.tipoErrorString)
        }
        if (binding.etTelTransportista.text.toString() != preferencias.getGlobalData(this, "au_TelTrans")){
            //Update request
            val update = Update("TELEFONOTRANSPORTE", preferencias.getGlobalData(this, "au_TelTrans"), binding.etTelTransportista.text.toString(),
                utils.latitude, utils.longitude, preferencias.getGlobalData(this, "TM"))

            utils.doRequest(update, this)
            println("1UPDATE TelTrans= Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
            if(utils.infoUpdate!!.Success){
                preferencias.updateGlobalData(this, "TelTrans", binding.etTelTransportista.text.toString())
            }
        }
        if (binding.etNombreTransportista.text.toString() != preferencias.getGlobalData(this, "au_Trans")){
            //Update request
            val update = Update("TRANSPORTE", preferencias.getGlobalData(this, "au_Trans"), binding.etNombreTransportista.text.toString(),
                utils.latitude, utils.longitude, preferencias.getGlobalData(this, "TM"))

            utils.doRequest(update, this)
            println("1UPDATE TRANSPORTE= Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
            if(utils.infoUpdate!!.Success){
                preferencias.updateGlobalData(this, "Trans", binding.etNombreTransportista.text.toString())
            }
        }
        if (checkboxPredio.isChecked.toString() != preferencias.getGlobalData(this, "au_UsoPredio")){
            var predio = "0"
            if(checkboxPredio.isChecked.toString() == "true"){
                predio = "1"
            }
            //Update request
            val update = Update("USAPREDIO", preferencias.getGlobalData(this, "au_UsoPredio"), predio,
                utils.latitude, utils.longitude, preferencias.getGlobalData(this, "TM"))

            utils.doRequest(update, this)
            println("1UPDATE Tel= Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
            if(utils.infoUpdate!!.Success){
                preferencias.updateGlobalData(this, "Tel", checkboxPredio.isChecked.toString())
            }
        }
        //ARMA
        if (binding.etNumArma.text.toString() != preferencias.getGlobalData(this, "au_Arma")){
            //Update request
            val update = Update("ESCOPETA", preferencias.getGlobalData(this, "au_Arma"), binding.etNumArma.text.toString(),
                utils.latitude, utils.longitude, preferencias.getGlobalData(this, "TM"))

            utils.doRequest(update, this)
            println("1UPDATE Arma= Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
            if(utils.infoUpdate!!.Success){
                preferencias.updateGlobalData(this, "Arma", binding.etNumArma.text.toString())
            }
        }

        //TELEFONO
        if (binding.etTelefonoPiloto.text.toString() != preferencias.getGlobalData(this, "au_Tel")){
            //Update request
            val update = Update("TELEFONO", preferencias.getGlobalData(this, "au_Tel"), binding.etTelefonoPiloto.text.toString(),
                utils.latitude, utils.longitude, preferencias.getGlobalData(this, "TM"))

            utils.doRequest(update, this)
            println("1UPDATE Tel= Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
            if(utils.infoUpdate!!.Success){
                preferencias.updateGlobalData(this, "Tel", binding.etTelefonoPiloto.text.toString())
            }
        }
        //SELLADO
        if (sellado != preferencias.getGlobalData(this, "au_Sellado")){
            //Update request
            val update = Update("Sellado", preferencias.getGlobalData(this, "au_Sellado"), sellado, utils.latitude,
                utils.longitude, preferencias.getGlobalData(this, "TM"))

            utils.doRequest(update, this)
            println("1UPDATE Sellado= Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
            if(utils.infoUpdate!!.Success){
                preferencias.updateGlobalData(this, "Sellado", sellado)
            }
        }
        //MARCHAMO FISCAL
        if (binding.etMarchamoFiscal.text.toString() != preferencias.getGlobalData(this, "au_Fiscal")){
            //Update request
            val update = Update("MARCHAMOFISCAL", preferencias.getGlobalData(this, "au_Fiscal"), binding.etMarchamoFiscal.text.toString(),
                utils.latitude, utils.longitude, preferencias.getGlobalData(this, "TM"))

            utils.doRequest(update, this)
            println("1UPDATE M Fiscal= Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
            if(utils.infoUpdate!!.Success){
                preferencias.updateGlobalData(this, "M_Fiscal", binding.etMarchamoFiscal.text.toString())
            }
        }
        //MARCHAMNO GPS
        if (binding.etMarchamoGps.text.toString() != preferencias.getGlobalData(this, "au_Gps")){
            //Update request
            val update = Update("MarchamoGps", preferencias.getGlobalData(this, "au_Gps"), binding.etMarchamoGps.text.toString(),
                utils.latitude, utils.longitude, preferencias.getGlobalData(this, "TM"))

            utils.doRequest(update, this)
            println("1UPDATE M Gps= Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
            if(utils.infoUpdate!!.Success){
                preferencias.updateGlobalData(this, "M_Gps", binding.etMarchamoGps.text.toString())
            }
        }
        //NOTAS
        if (binding.etNotas.text.toString() != preferencias.getGlobalData(this, "au_Notas")){
            //Update request
            val update = Update("Notas", preferencias.getGlobalData(this, "au_Notas"), binding.etNotas.text.toString(), utils.latitude,
                utils.longitude, preferencias.getGlobalData(this, "TM"))

            utils.doRequest(update, this)
            println("1UPDATE Notas= Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
            if(utils.infoUpdate!!.Success){
                preferencias.updateGlobalData(this, "Notas", binding.etMarchamoGps.text.toString())
            }
        }
    }

    private suspend fun primeraSesion(){

        //Request a /NuevaMision

        val mision = Mision(binding.etNumArma.text.toString(), "", binding.etMarchamoFiscal.text.toString(), binding.etMarchamoGps.text.toString(),
            binding.etNotas.text.toString(), preferencias.getGlobalData(this@MisionActivity, "Rui"), binding.etNombrePiloto.text.toString(),
            binding.etPlacaCabezal.text.toString(), sellado, binding.etTelefonoPiloto.text.toString(),
            preferencias.getGlobalData(this@MisionActivity, "TM"), binding.etTelTransportista.text.toString(),
            binding.etNombreTransportista.text.toString(), if (checkboxPredio.isChecked) "1" else "0")

        //Establece los campos modificables como preferencias
        setPreferencias()

        utils.doRequest(mision, this@MisionActivity)

        println("Pre HOLLAAAAAAAA")
        if (utils.infoMision == null && utils.tipoErrorString == "Mis/1"){
            println("MeD HOLLAAAAAAAA")

            utils.progressDialog!!.dismiss()
            binding.btnSiguiente.isEnabled = true
            return
        } else {
            println("Posiblemente el error sea otro como Mis/2 TipoErrorString es: "+utils.tipoErrorString)
        }
        println("Oficial HOLLAAAAAAAA")

        val info: MisionRes = utils.infoMision!!

        if (info.Success){

            utils.subirListFotos(utils.listOfFotos, this)
            //progressDialog2!!.dismiss()
            esPrimeraVez = false
            utils.progressDialog!!.dismiss()
            val intent = Intent(this@MisionActivity, BotonesActivity::class.java)
            startActivity(intent)
            this@MisionActivity.finishAffinity()
            //utils.stopLocationUpdates()

        } else {
            binding.btnSiguiente.isEnabled = true
            println("Fallo Mision")
            println("Success: "+info.Success.toString()+" Mensaje: "+info.Mensaje)
            Toast.makeText(this@MisionActivity, "Success: "+info.Success.toString()+" Mensaje: "+info.Mensaje, Toast.LENGTH_SHORT).show()
            //utils.stopLocationUpdates()
            utils.progressDialog!!.dismiss()
        }
    }

    private suspend fun sendDataMision(){
        if(preferencias.getGlobalData(this@MisionActivity, "verM") == "true"){
            utils.progressDialog!!.dismiss()
            updateData()
            //agregado:
            preferencias.setGlobalData(this@MisionActivity, "Sesion", "reanudar")
            //agregado fin
            val intent = Intent(this@MisionActivity, BotonesActivity::class.java)
            startActivity(intent)
            this@MisionActivity.finish()
        } else {
            when(preferencias.getGlobalData(this@MisionActivity, "Sesion")){
                "primera" -> {
                    updateData1Sesion()
                    primeraSesion()
                }
                else -> {
                    utils.progressDialog!!.dismiss()
                }
            }
        }
    }

    //Solicita la ubicacion de este momento, en raros casos es null
    //Al ser con lastLocation la ubicacion no es muy precisa
    //https://developer.android.com/training/location/retrieve-current
    private fun onlyLocation(){
        //new
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    // Got last known location. In some rare situations this can be null.
                    utils.latitude = location!!.latitude.toString()
                    utils.longitude = location!!.longitude.toString()
                    Toast.makeText(this, "OL= lat:"+utils.latitude+" long:"+utils.longitude, Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Sorry :'(", Toast.LENGTH_SHORT).show()
                }
        }
        //end new
    }

}