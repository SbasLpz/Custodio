package com.miapp.custodio2

import android.app.Activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.miapp.custodio2.Adapters.Adapter
import com.miapp.custodio2.Adapters.CardViewData
import com.miapp.custodio2.ClasesRequest.*
import com.miapp.custodio2.Utils.Preferencias
import com.miapp.custodio2.Utils.RequestPermissions
import com.miapp.custodio2.Utils.SegundoPlanoTasks
import com.miapp.custodio2.databinding.ActivityBotonesBinding
import com.techiness.progressdialoglibrary.ProgressDialog
import kotlinx.coroutines.*
import java.util.ArrayList

class BotonesActivity : AppCompatActivity(), Adapter.OnItemClickListener {
    //Para que funciones el Binding
    private lateinit var binding: ActivityBotonesBinding
    //Utils donde esta funciones genericas
    val utils = RequestPermissions()
    val preferencias = Preferencias()
    var botones: BotonesRes? = null
    //new
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBotonesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencias.setGlobalData(this, "verM", "false")

        binding.tvMisionRui.text = "RUI: "+preferencias.getGlobalData(this, "Rui")
        binding.tvMisionNomb.text = "NOMB: "+preferencias.getGlobalData(this, "Nomb")

//        GlobalScope.launch {
//            utils.startLocationUpdates(this@BotonesActivity)
//        }

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

//            val check = Check(preferencias.getGlobalData(this@BotonesActivity, "Rui"), preferencias.getGlobalData(this@BotonesActivity, "TM"))
//            utils.doRequest(check,this@BotonesActivity)
//
//            if (utils.infoCheck != null){
//                if (utils.infoCheck!!.Success){
//                    if(!utils.infoCheck!!.Activo){
//                        preferencias.setGlobalData(this@BotonesActivity, "Sesion", "primera")
//                        this@BotonesActivity.finishAffinity()
//                    } else {
//                        println("Check Message: "+utils.infoCheck!!.Message+"Check Activo: "+utils.infoCheck!!.Activo)
//                        Toast.makeText(this@BotonesActivity, "Check Message: "+utils.infoCheck!!.Message+"Check Activo: "+utils.infoCheck!!.Activo, Toast.LENGTH_LONG).show()
//                        utils.startLocationUpdates(this@BotonesActivity)
//                    }
//                }
//            }
        }
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
//        if(preferencias.getGlobalData(this, "Sesion") != "segunda"){
//            preferencias.setGlobalData(this, "Sesion", "reanudar")
//        }
        println("DESTRUIDA")
        preferencias.setGlobalData(this, "Sesion", "primera")
        val serviceIntent = Intent(this, SegundoPlanoTasks::class.java)
        this@BotonesActivity.startService(serviceIntent)
//        GlobalScope.launch(Dispatchers.IO){
//            println("HHHJJHHJJHHHh")
//
//        }
        //ContextCompat.startForegroundService(this, serviceIntent)
    }

    override fun onPause() {
        super.onPause()
        utils.stopLocationUpdates()
        if (!utils.stopLocationUpdates222(this)){
            preferencias.setGlobalData(this, "Sesion", "primera")
            finishAffinity()
        }
    }

    //Maneja los click a los botonoes
    override fun onItemClick(position: Int, name: String) {
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
                    //Ya se tiene la UBICACION
                    val registro = Registro(name, utils.getCurrentDate(), utils.latitude, utils.longitude, preferencias.getGlobalData(this@BotonesActivity, "TM"))

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
                    val registro = Registro(name, utils.getCurrentDate(), utils.latitude, utils.longitude, preferencias.getGlobalData(this@BotonesActivity, "TM"))
                    utils.doRequest(registro, this@BotonesActivity)
                    //preferencias.updateGlobalData(this, "Sesion", "segunda")
                    preferencias.setGlobalData(this@BotonesActivity, "Sesion", "segunda")

                    if(name == "Pánico"){
                        startActivity(Intent(this@BotonesActivity, InicioScreen::class.java))
                        this@BotonesActivity.finish()
                    } else {
                        this@BotonesActivity.finish()
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

    private fun sendButtonDataXoriginal(name: String, registro: Registro){
        when(name){
            "Dirección de entrega" ->{
                showDireccion()

            }
            "Llamada" -> utils.call(this@BotonesActivity, utils.tel)
            "Pánico" -> {
                lifecycleScope.launch {
                    cerrarAlert("Pánico")
                }
            }
            "Puesto de Registro" -> {
                lifecycleScope.launch {
                    cerrarAlert("Puesto de Registro")
                }
            }
            else ->{
                lifecycleScope.launch {
                    utils.doRequest(registro, this@BotonesActivity)
                }
            }
        }

        if(utils.infoRegistro == null){
            //println("VERRR_EE: "+utils.infoRegistro!!.Success)
            utils.progressDialog!!.dismiss()
            Toast.makeText(this@BotonesActivity, "Error: "+utils.errorString, Toast.LENGTH_SHORT).show()
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

}