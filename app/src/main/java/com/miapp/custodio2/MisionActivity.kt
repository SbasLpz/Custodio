package com.miapp.custodio2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.get
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.gson.Gson
import com.miapp.custodio2.Utils.LocationService
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MisionActivity : AppCompatActivity() {
    //Para que funciones el Binding
    private lateinit var binding: com.miapp.custodio2.databinding.ActivityMisionBinding
    //Utils donde esta funciones genericas
    val utils = RequestPermissions()
    val preferencias = Preferencias()
    var sellado = "nada"
    //new
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    //private lateinit var checkboxPredio: CheckBox

    var esPrimeraVez = false
    private lateinit var spinner: Spinner
    private lateinit var selectedSpinner: String
    var selectedSpinnerInt: Int = 0
    private lateinit var adapterSpinner: ArrayAdapter<CharSequence>

    private lateinit var codTransporte: String
    private lateinit var codPiloto: String

    var horaContacto = "indefinido"
    //var progressDialog2: android.app.ProgressDialog? = null
    //Progress dialog
    //private lateinit var progressDialog: ProgressDialog

    // ========================== MAIN MISION ==========================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMisionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        spinner = binding.spinner
        adapterSpinner = ArrayAdapter.
        createFromResource(this, R.array.your_options_array, android.R.layout.simple_spinner_dropdown_item)

        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapterSpinner
        spinner.setSelection(0)

        selectedSpinner = spinner.getItemAtPosition(0).toString()

        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, p1: View?, position: Int, id: Long) {
                selectedSpinner = parent?.getItemAtPosition(position).toString()
                selectedSpinnerInt = position
                println("******* SELECTED del Spinner: ${selectedSpinner} ********")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Do something
                selectedSpinner = spinner.getItemAtPosition(0).toString()
                selectedSpinnerInt = 0
            }
        }



        createSpinner(binding.spinnerCountry1, R.array.codsPais) {
                selectedItem, position -> codTransporte = selectedItem.substringAfter(") ") + binding.etTelTransportista.text.toString()
        }

        createSpinner(binding.spinnerCountry2, R.array.codsPais) {
                selectedItem, position -> codPiloto = selectedItem.substringAfter(") ") + binding.etTelefonoPiloto.text.toString()
        }

        binding.etHoraContacto.setOnClickListener {
            showTimePicker()
        }

        //Settings progrss dialog foto upload
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.SERVICE_START
            startService(this)
        }

        //checkboxPredio = binding.checkboxUsoPredio

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
        lifecycleScope.launch {7
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
            println("♦♦♦♦ ♦♦♦ ♦♦ ♦ tel Transporte ${codPiloto}")
            //binding.etNotas.setText(horaContacto)
            //binding.etNotas.setText(codPiloto+binding.etTelTransportista.text.toString()+" -- "+codTransporte)
            //binding.etNotas.setText(getCodTransporte()+" // "+getCodPiloto())
            //return@setOnClickListener

//            GlobalScope.launch {
//                Intent(applicationContext, LocationService::class.java).apply {
//                    action = LocationService.SERVICE_START
//                    startService(this)
//                }
//            }
            println("******* btnSiguiente - SELECTED del Spinner: ${selectedSpinner} ********")
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

    private fun createSpinner (spinner: Spinner, options: Int, onItemSelected: (String, Int) -> Unit) {
        ArrayAdapter.createFromResource(
            this,
            options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner.
            spinner.adapter = adapter
        }

        spinner.setSelection(0)
        onItemSelected(spinner.getItemAtPosition(0).toString(), 0)

        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                onItemSelected(parent.getItemAtPosition(pos).toString(), pos)
                // An item is selected. You can retrieve the selected item using
                // parent.getItemAtPosition(pos).
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback.
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

        val testval = preferencias.getGlobalData(this, "au_HoraContacto")
        //Estos datos al ser los NO editables no imtpota si viene o no de Botones siempre los cargara
        binding.tvRui.setText("Rui: "+preferencias.getGlobalData(this, "Rui"))
        binding.tvNomb.setText("Nomb: "+preferencias.getGlobalData(this, "Nomb"))

        binding.etPlacaCabezal.setText(preferencias.getGlobalData(this, "au_Cabezal"))
        binding.etPlacaTC.setText(preferencias.getGlobalData(this, "au_Tc"))
        binding.etNumContenedor.setText(preferencias.getGlobalData(this, "au_Contenedor"))
        binding.etNombrePiloto.setText(preferencias.getGlobalData(this, "au_Piloto"))
        binding.etNumArma.setText(preferencias.getGlobalData(this, "au_Arma"))

        spinner.setSelection(preferencias.getGlobalData(this, "au_Pais").toInt())

        binding.etTelefonoPiloto.setText(preferencias.getGlobalData(this, "au_Tel"))

        when(preferencias.getGlobalData(this, "au_Sellado")){
            "" -> binding.rbSelladoMarchamo.isChecked = true
            "Candado" -> binding.rbSelladoCandado.isChecked = true
            "Marchamo" -> binding.rbSelladoMarchamo.isChecked = true
        }

        binding.etMarchamoFiscal.setText(preferencias.getGlobalData(this, "au_Fiscal"))
        binding.etHoraPos.setText(preferencias.getGlobalData(this, "au_HoraPos"))
        binding.etHoraSolicitada.setText(preferencias.getGlobalData(this, "au_HoraSol"))
        binding.etHoraSalida.setText(preferencias.getGlobalData(this, "au_HoraSalida"))
        binding.etHoraContacto.setText(preferencias.getGlobalData(this, "au_HoraContacto"))
        binding.etLugarPos.setText(preferencias.getGlobalData(this, "au_Lugar"))
        binding.etLugarSalida.setText(preferencias.getGlobalData(this, "au_LugarSalida"))
        binding.etMarchamoGps.setText(preferencias.getGlobalData(this, "au_Gps"))
        binding.etNotas.setText(preferencias.getGlobalData(this, "au_Notas"))

        val testval2 = preferencias.getGlobalData(this, "au_HoraContacto")

        /*Esto no iba, se supone que si estas en botones y regresas el numero del fiscal aparece
        * pero ante un error que en emulador no ocurre se supone que lo ponia en 0, asi que por si
        * acaso se coloco aqui tambien esto, */
        //binding.etMarchamoFiscal.setText("0000000000")
        //Cuando sea la primera vez verM != true ó verM == false (que no venga de Botones) cargara los datos editables tambien con lo que
        //venga del /Autenticar
        if (preferencias.getGlobalData(this, "verM") != "true"){
            //Editables
            spinner.setSelection(preferencias.getGlobalData(this, "au_Pais").toInt())
            binding.etHoraPos.setText(preferencias.getGlobalData(this, "au_HoraPos"))
            binding.etLugarSalida.setText(preferencias.getGlobalData(this, "au_LugarSalida"))
            binding.etHoraSolicitada.setText(preferencias.getGlobalData(this, "au_HoraSol"))
            binding.etLugarPos.setText(preferencias.getGlobalData(this, "au_Lugar"))

            binding.etHoraSalida.setText(preferencias.getGlobalData(this, "au_HoraSalida"))

            //binding.etTelTransportista.setText(preferencias.getGlobalData(this, "au_TelTrans"))
            binding.etTelTransportista.setText(getAuTel("au_TelTrans")["num"])
            val indexSpinner1 = resources.getStringArray(R.array.codsPais).indexOfFirst {
                it.contains(getAuTel("au_TelTrans")["cod"].toString())
            }
            binding.spinnerCountry1.setSelection(indexSpinner1)


            binding.etNombreTransportista.setText(preferencias.getGlobalData(this, "au_Trans"))
            //checkboxPredio.isChecked = preferencias.getGlobalData(this, "au_UsoPredio").toBoolean()
            binding.etNumArma.setText(preferencias.getGlobalData(this, "au_Arma"))
            binding.etHoraContacto.setText(preferencias.getGlobalData(this, "au_HoraContacto"))

            //binding.etTelefonoPiloto.setText(preferencias.getGlobalData(this, "au_Tel"))
            binding.etTelefonoPiloto.setText(getAuTel("au_Tel")["num"])
            val indexSpinner2 = resources.getStringArray(R.array.codsPais).indexOfFirst {
                it.contains(getAuTel("au_Tel")["cod"].toString())
            }
            binding.spinnerCountry2.setSelection(indexSpinner2)

            binding.etHoraContacto.setText(preferencias.getGlobalData(this, "au_HoraContacto"))


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
        //return
        val valor = if (preferencias.getGlobalData(this, "Pais").toInt() == 7) 0 else preferencias.getGlobalData(this, "Pais").toInt()

        if (selectedSpinnerInt != valor){
            spinner.setSelection(valor)
        }

        if (getCodTransporte() != preferencias.getGlobalData(this, "TelTrans").trim()){
            //binding.etTelTransportista.setText(preferencias.getGlobalData(this, "TelTrans"))
            if (getCodPiloto() != preferencias.getGlobalData(this, "TelTrans")){
                binding.etTelTransportista.setText(getAuTel("TelTrans")["num"])
                val indexSpinner1 = resources.getStringArray(R.array.codsPais).indexOfFirst {
                    it.contains(getAuTel("TelTrans")["cod"].toString())
                }
                binding.spinnerCountry1.setSelection(indexSpinner1)
            }
        }
        if (binding.etNombreTransportista.text.toString() != preferencias.getGlobalData(this, "Trans")){
            binding.etNombreTransportista.setText(preferencias.getGlobalData(this, "Trans"))
        }
//        if (checkboxPredio.isChecked != preferencias.getGlobalData(this, "UsoPredio").toBoolean()){
//            checkboxPredio.isChecked = preferencias.getGlobalData(this, "UsoPredio").toBoolean()
//        }
        if (binding.etNumArma.text.toString() != preferencias.getGlobalData(this, "Arma")){
            binding.etNumArma.setText(preferencias.getGlobalData(this, "Arma"))
        }


        if (getCodPiloto() != preferencias.getGlobalData(this, "Tel")){
            //binding.etTelefonoPiloto.setText(preferencias.getGlobalData(this, "Tel"))
            binding.etTelefonoPiloto.setText(getAuTel("Tel")["num"])
            val indexSpinner2 = resources.getStringArray(R.array.codsPais).indexOfFirst {
                it.contains(getAuTel("Tel")["cod"].toString())
            }
            binding.spinnerCountry2.setSelection(indexSpinner2)
        }
//        if (checkboxPredio.isChecked != preferencias.getGlobalData(this, "UsoPredio").toBoolean()){
//            checkboxPredio.isChecked = preferencias.getGlobalData(this, "UsoPredio").toBoolean()
//        }+

        if (binding.etHoraContacto.text.toString() != preferencias.getGlobalData(this, "HoraContacto")) {
            binding.etHoraContacto.setText(preferencias.getGlobalData(this, "HoraContacto"))
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
        val valor = if (selectedSpinnerInt == 0) "7" else selectedSpinnerInt.toString()
        preferencias.setGlobalData(this, "Pais", valor)

        //preferencias.setGlobalData(this, "TelTrans", binding.etTelTransportista.text.toString())
        preferencias.setGlobalData(this, "TelTrans", getCodTransporte())
        preferencias.setGlobalData(this, "Trans", binding.etNombreTransportista.text.toString())
        //preferencias.setGlobalData(this, "UsoPredio", checkboxPredio.isChecked.toString())
        preferencias.setGlobalData(this, "Arma", binding.etNumArma.text.toString())

        preferencias.setGlobalData(this, "Tel", getCodPiloto())
        preferencias.setGlobalData(this, "Sellado", sellado)
        preferencias.setGlobalData(this, "M_Fiscal", binding.etMarchamoFiscal.text.toString())
        preferencias.setGlobalData(this, "M_Gps", binding.etMarchamoGps.text.toString())
        preferencias.setGlobalData(this, "Notas", binding.etNotas.text.toString())

        preferencias.setGlobalData(this, "HoraContacto", binding.etHoraContacto.text.toString())
    }

    //Actualizara las preferencias editables que han sido modificadas
    //Este se usa mas que nada cuando el Sesion es de tipo 3 - Cuendo se esta en Botones
    private suspend fun updateData(){
        val valor = if (preferencias.getGlobalData(this, "Pais").toInt() == 7) 0 else preferencias.getGlobalData(this, "Pais").toInt()

        if (selectedSpinnerInt != valor){
            val valor2 = if (selectedSpinnerInt == 0) "7" else selectedSpinnerInt.toString()
            //Update request
            val update = Update("PAIS", preferencias.getGlobalData(this, "Pais"), valor2,
                utils.latitude, utils.longitude, preferencias.getGlobalData(this, "TM"))
            utils.doRequest(update, this)
            preferencias.setGlobalData(this, "Pais", valor2)
            println("UPDATE PAIS= Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
        }
        if (getCodTransporte() != preferencias.getGlobalData(this, "TelTrans").trim()){
            //Update request
            val update = Update("TELEFONOTRANSPORTE", preferencias.getGlobalData(this, "TelTrans"), getCodTransporte(),
                utils.latitude, utils.longitude, preferencias.getGlobalData(this, "TM"))
            utils.doRequest(update, this)
            preferencias.setGlobalData(this, "TelTrans", getCodTransporte())
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
//        if (checkboxPredio.isChecked.toString() != preferencias.getGlobalData(this, "UsoPredio")){
//            var predio = "0"
//            if(checkboxPredio.isChecked.toString() == "true"){
//                predio = "1"
//            }
//            //Update request
//            val update = Update("USAPREDIO", preferencias.getGlobalData(this, "UsoPredio"), predio,
//                utils.latitude, utils.longitude, preferencias.getGlobalData(this, "TM"))
//            utils.doRequest(update, this)
//            preferencias.setGlobalData(this, "UsoPredio", checkboxPredio.isChecked.toString())
//            println("UPDATE Tel= Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
//        }
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
        if (getCodPiloto() != preferencias.getGlobalData(this, "Tel")){
            //Update request
            val update = Update("TELEFONO", preferencias.getGlobalData(this, "Tel"), getCodPiloto(),
                utils.latitude, utils.longitude, preferencias.getGlobalData(this, "TM"))

            utils.doRequest(update, this)
            //Actualiza las preferencias
            /*preferencias.updateGlobalData(this, "Tel", binding.etTelefonoPiloto.text.toString())*/
            preferencias.setGlobalData(this, "Tel", getCodPiloto())
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

        if (binding.etHoraContacto.text.toString() != preferencias.getGlobalData(this, "HoraContacto")){
            //Update request
            val update = Update("HoraContacto", preferencias.getGlobalData(this, "HoraContacto"), binding.etHoraContacto.text.toString(),
                utils.latitude, utils.longitude, preferencias.getGlobalData(this, "TM"))

            utils.doRequest(update, this)
            //Actualiza las preferencias
            preferencias.setGlobalData(this, "HoraContacto", binding.etHoraContacto.text.toString())
            println("UPDATE Hora Contacto= Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
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
        var auPais = if(preferencias.getGlobalData(this, "au_Pais") == null) 0 else preferencias.getGlobalData(this, "au_Pais").toInt()
        var pais = if(preferencias.getGlobalData(this, "Pais") == null) 0 else preferencias.getGlobalData(this, "au_Pais").toInt()
        val valor = if (auPais == 7) 0 else pais

        if (selectedSpinnerInt != valor){
            val valor2 = if (selectedSpinnerInt == 0) "7" else selectedSpinnerInt.toString()

            //Update request
            val update = Update("PAIS", preferencias.getGlobalData(this, "au_Pais"), valor2,
                utils.latitude, utils.longitude, preferencias.getGlobalData(this, "TM"))

            utils.doRequest(update, this)
            println("1UPDATE Pais= Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
            if(utils.infoUpdate!!.Success){
                preferencias.updateGlobalData(this, "Pais", valor2)
            }
        }
        if (getCodTransporte() != preferencias.getGlobalData(this, "au_TelTrans").trim()){
            //Update request
            val update = Update("TELEFONOTRANSPORTE", preferencias.getGlobalData(this, "au_TelTrans"), getCodTransporte(),
                utils.latitude, utils.longitude, preferencias.getGlobalData(this, "TM"))

            utils.doRequest(update, this)
            println("1UPDATE TelTrans= Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
            if(utils.infoUpdate!!.Success){
                preferencias.updateGlobalData(this, "TelTrans", getCodTransporte())
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
        if (getCodPiloto() != preferencias.getGlobalData(this, "au_Tel")){
            //Update request
            val update = Update("TELEFONO", preferencias.getGlobalData(this, "au_Tel"), getCodPiloto(),
                utils.latitude, utils.longitude, preferencias.getGlobalData(this, "TM"))

            utils.doRequest(update, this)
            println("1UPDATE Tel= Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
            if(utils.infoUpdate!!.Success){
                preferencias.updateGlobalData(this, "Tel", getCodPiloto())
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

        //HORA CONTACTO
        if (binding.etHoraContacto.text.toString() != preferencias.getGlobalData(this, "au_HoraContacto")){
            //Update request
            val update = Update("HoraContacto", preferencias.getGlobalData(this, "au_HoraContacto"), binding.etHoraContacto.text.toString(),
                utils.latitude, utils.longitude, preferencias.getGlobalData(this, "TM"))

            utils.doRequest(update, this)
            println("1UPDATE Hora Contacto = Success: "+utils.infoUpdate!!.Success.toString()+" Mensaje: "+utils.infoUpdate!!.Message)
            if(utils.infoUpdate!!.Success){
                preferencias.updateGlobalData(this, "HoraContacto", binding.etHoraContacto.text.toString())
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
        val horaContacto = binding.etHoraContacto.text
        val mision = Mision(binding.etNumArma.text.toString(), horaContacto.toString(), "", binding.etMarchamoFiscal.text.toString(), binding.etMarchamoGps.text.toString(),
            binding.etNotas.text.toString(), preferencias.getGlobalData(this@MisionActivity, "Rui"), binding.etNombrePiloto.text.toString(),

            binding.etPlacaCabezal.text.toString(), sellado, getCodPiloto(),
            preferencias.getGlobalData(this@MisionActivity, "TM"), getCodTransporte(),
            binding.etNombreTransportista.text.toString(), "0", /*selectedSpinnerInt.toString(), */ /* binding.etHoraContacto.text.toString()*/)
        //if (checkboxPredio.isChecked) "1" else "0"

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

        if(utils.infoMision == null) {
            utils.progressDialog!!.dismiss()
            MaterialAlertDialogBuilder(this)
                .setTitle(ApiErrorHandler.msg)
                .setMessage("Codigo Error: ${ApiErrorHandler.cod} \n\nInforme de ser necesario si el sistema no da respuesta positiva.")
                .setNeutralButton("Aceptar") { dialog, which ->
                    // Respond to neutral button press
                    return@setNeutralButton
                }
                .show()
            binding.btnSiguiente.isEnabled = true
            return
        }

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

    fun getCodTransporte(): String {
        var cod = binding.spinnerCountry1.selectedItem.toString().substringAfter(") ")
        if(cod == "Sin código") {
            cod = ""
        }
        codTransporte = cod + binding.etTelTransportista.text.toString()
        return  codTransporte
    }

    fun getCodPiloto(): String {
        var cod = binding.spinnerCountry2.selectedItem.toString().substringAfter(") ")
        if(cod == "Sin código") {
            cod = ""
        }
        codPiloto = cod + binding.etTelefonoPiloto.text.toString()
        return  codPiloto
    }

    private fun getAuTel(pref: String): Map<String, String> {
        /** Necesita venir con .Trim() **/
        var tel = preferencias.getGlobalData(this, pref)

        var codTelefono = tel.trim().dropLast(8).replace("+", "")
        var numTelefono = tel.takeLast(8)

        if(codTelefono == "" || codTelefono == null) {
            codTelefono = resources.getStringArray(R.array.codsPais).get(0)
        }

        var pair = mapOf<String, String>(
            "cod" to codTelefono,
            "num" to numTelefono
        )
        return pair
    }

    private fun showTimePicker() {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H) // Usa TimeFormat.CLOCK_12H para formato de 12 horas
            .setHour(6) // Hora inicial
            .setMinute(0) // Minuto inicial
            .setTitleText("Selecciona la hora")
            .build()

        timePicker.show(supportFragmentManager, "TIME_PICKER")
        
        timePicker.addOnPositiveButtonClickListener {

            var amPm = if (timePicker.hour < 12) "AM" else "PM"
            horaContacto = "${timePicker.hour}:${timePicker.minute} ${amPm}"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                var fechaActual = LocalDate.now()
                var apiDate = LocalDateTime.of(fechaActual.year, fechaActual.month, fechaActual.dayOfMonth, timePicker.hour, timePicker.minute)
                apiDate.format(DateTimeFormatter.ISO_DATE_TIME)
                horaContacto = apiDate.toString()
            }

            binding.etHoraContacto.setText(horaContacto)
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