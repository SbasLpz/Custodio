package com.miapp.custodio2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.miapp.custodio2.ClasesRequest.Extraccion
import com.miapp.custodio2.ClasesRequest.Foto
import com.miapp.custodio2.Utils.Preferencias
import com.miapp.custodio2.Utils.RequestPermissions
import com.miapp.custodio2.databinding.ActivityBotonesBinding
import com.miapp.custodio2.databinding.ActivityInfoBinding
import kotlinx.coroutines.launch

class InfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfoBinding
    val preferencias = Preferencias()
    val utils = RequestPermissions()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        utils.startLocationUpdates(this)

        binding.tvNombreCustodio.text = preferencias.getGlobalData(this, "NombreCustodio")

        var d1 = preferencias.getGlobalData(this, "PrimerUso")
        var d2 = preferencias.getGlobalData(this, "UltimoUso")
        var d3 = preferencias.getGlobalData(this, "TotalServiciosApp")
        var d4 = preferencias.getGlobalData(this, "TotalServiciosMes")

        println("D1: "+d1)
        println("D2: "+d1)
        println("D3: "+d1)
        println("D4: "+d4)

        binding.tvInfoPrimerUso.setText(d1)
        binding.tvInfoUltimoUso.setText(d2)
        binding.tvInfoTotalUsos.setText(d3)
        binding.tvInfoTotalUsosMes.setText(d4)

        binding.btnRegresar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        binding.btnReqExtraccion.setOnClickListener {
            var solExtraccion = Extraccion(utils.latitude, utils.longitude, true, preferencias.getGlobalData(this, "TM"))
            var gson = Gson().toJson(solExtraccion)
            println(gson)
            lifecycleScope.launch {
                utils.doRequest(solExtraccion, this@InfoActivity)
                if (utils.infoExtraccion != null) {
                    if (utils.infoExtraccion!!.Success === "true") {
                        Toast.makeText(this@InfoActivity, "Solicitud EXTRACCION Enviada", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@InfoActivity, "No se pudo enviar: ${utils.infoExtraccion!!.Message}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@InfoActivity, "Fallo la solicitud, revise que tenga señal y conexión a internet.", Toast.LENGTH_LONG).show()
                }
            }
            //println("MI SOLI EXTRACCION |->  ${solExtraccion.Longitud} ${solExtraccion.Latitud} ${solExtraccion.Reconcentrar} ${solExtraccion.Token}")
        }

        binding.btnCancelExtraccion.setOnClickListener {
            var solCancelExtraccion = Extraccion(utils.latitude, utils.longitude, false, preferencias.getGlobalData(this, "TM"))
            var gson = Gson().toJson(solCancelExtraccion)
            println(gson)
            lifecycleScope.launch {
                utils.doRequest(solCancelExtraccion, this@InfoActivity)
                if (utils.infoExtraccion != null) {
                    if (utils.infoExtraccion!!.Success === "true") {
                        Toast.makeText(this@InfoActivity, "Solicitud CANCELAR Extracción Enviada", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@InfoActivity, "No se pudo enviar la cancelación: ${utils.infoExtraccion!!.Message}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@InfoActivity, "Fallo la solicitud de Cancelar, revise que tenga señal y conexión a internet.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        utils.stopLocationUpdates()
    }
}