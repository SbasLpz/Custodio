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