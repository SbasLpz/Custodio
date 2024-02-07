package com.miapp.custodio2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.miapp.custodio2.databinding.ActivityInfoBinding
import com.miapp.custodio2.databinding.ActivityInicioScreenBinding

class InicioScreen : AppCompatActivity() {

    private lateinit var binding: ActivityInicioScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnIngresar.setOnClickListener {
            startActivity(Intent(this, BotonesActivity::class.java))
            this.finish()
        }
    }
}