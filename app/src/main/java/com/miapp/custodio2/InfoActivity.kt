package com.miapp.custodio2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.miapp.custodio2.Utils.Preferencias
import com.miapp.custodio2.databinding.ActivityBotonesBinding
import com.miapp.custodio2.databinding.ActivityInfoBinding

class InfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfoBinding
    val preferencias = Preferencias()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
    }
}