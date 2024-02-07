package com.miapp.custodio2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.miapp.custodio2.Utils.Preferencias
import com.miapp.custodio2.databinding.ActivityPerfilBinding

class PerfilActivity : AppCompatActivity() {

    lateinit var binding: ActivityPerfilBinding
    val preferencias = Preferencias()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCerrarSesion.setOnClickListener {
            preferencias.setGlobalData(this, "Sesion", "primera")
            finishAffinity()
        }
    }
}