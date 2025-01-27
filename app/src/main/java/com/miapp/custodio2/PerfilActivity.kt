package com.miapp.custodio2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.util.Linkify
import com.miapp.custodio2.Utils.LocationService
import com.miapp.custodio2.Utils.Preferencias
import com.miapp.custodio2.databinding.ActivityPerfilBinding

class PerfilActivity : AppCompatActivity() {

    lateinit var binding: ActivityPerfilBinding
    val preferencias = Preferencias()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Linkify.addLinks(binding.txtPoliticas, Linkify.ALL)

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

        binding.btnCerrarSesion.setOnClickListener {
            Intent(applicationContext, LocationService::class.java).apply {
                action = LocationService.SERVICE_STOP
                startService(this)
                //println("KKK    Termine las ubicaciones SERVICE.    KKK")
            }
            preferencias.setGlobalData(this, "Sesion", "primera")
            finishAffinity()
        }
    }
}