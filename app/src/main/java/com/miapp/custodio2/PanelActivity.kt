package com.miapp.custodio2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.miapp.custodio2.Utils.ExtraUtils
import com.miapp.custodio2.Utils.Preferencias
import com.miapp.custodio2.Utils.RequestPermissions
import com.miapp.custodio2.databinding.ActivityPanelBinding

class PanelActivity : AppCompatActivity() {

    lateinit var binding: ActivityPanelBinding
    val preferencias = Preferencias()
    val preferenciasFbResponse = ExtraUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var mensaje = preferenciasFbResponse

        binding.tvInfo.setText(preferencias.getGlobalData(this, "mPanel"))
        var url = preferencias.getGlobalData(this, "urlPanel")
        Glide.with(this).load(url).into(binding.ivUrl)

        binding.btnCerrar.setOnClickListener {
            preferencias.updateGlobalData(this, "Panel", "false")
            preferencias.updateGlobalData(this, "mPanel", "")
            preferencias.updateGlobalData(this, "urlPanel", "")
            startActivity(Intent(this, BotonesActivity::class.java))
            this.finishAffinity()
        }
    }


}