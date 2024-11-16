package com.miapp.custodio2

import android.graphics.Color
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.withCreated
import com.miapp.custodio2.ClasesRequest.CheckFinalizar
import com.miapp.custodio2.ClasesRequest.CheckFinalizarRes
import com.miapp.custodio2.ClasesRequest.Finalizar
import com.miapp.custodio2.ClasesRequest.FinalizarRes
import com.miapp.custodio2.ClasesRequest.Mision
import com.miapp.custodio2.Utils.RequestPermissions
import com.miapp.custodio2.databinding.ActivityFinalizarBinding
import com.techiness.progressdialoglibrary.ProgressDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FinalizarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFinalizarBinding
    val utils = RequestPermissions()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFinalizarBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnFinalizar.setOnClickListener {
            utils.progressDialog = ProgressDialog(this@FinalizarActivity)
            utils.progressDialog!!.theme = ProgressDialog.THEME_LIGHT
            utils.progressDialog!!.mode = ProgressDialog.MODE_INDETERMINATE
            utils.progressDialog!!.setMessage("Verificando...")
            utils.progressDialog!!.setTitle("Verificando información")
            utils.progressDialog!!.show()

            CoroutineScope(Dispatchers.Main).launch {
                val result = withContext(Dispatchers.IO) {
                    val check = CheckFinalizar(binding.etNumeroFinalizaciN.text.toString())
                    utils.doRequest(check, this@FinalizarActivity)
                    return@withContext utils.infoCheckFinalizar
                }

                utils.progressDialog!!.dismiss()

                handleResult(result)
            }
        }


        binding.btnFinalizar.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val result = withContext(Dispatchers.IO) {

                    val finalizar = Finalizar(if (binding.checkboxUsoPredio.isChecked) "1" else "0",
                        if (binding.checkboxTrifoliar.isChecked) "1" else "0")

                    utils.doRequest(finalizar, this@FinalizarActivity)

                    return@withContext utils.infoFinalizar
                }

                utils.progressDialog!!.dismiss()
                handleFinalizar(result)
                //handleResult(result)
            }
        }

    }

    private fun handleResult(result: CheckFinalizarRes?) {
        if(result == null) {
            binding.tvError.setText("No se pudo establecer conexión con el servidor. Rsultado null.")
            binding.tvError.visibility = View.VISIBLE
            return
        }

        /** NO FUE NULL **/
        if(!result.Success){
            binding.tvError.visibility = View.GONE
            binding.tvError.text = "Sin verificar: ${result.Mensaje}"
            binding.tvError.visibility = View.VISIBLE
            return
        } else {
            binding.tvError.setTextColor(Color.GREEN)
            binding.tvError.setText("Verficcación completa: ${result.Mensaje}")
            binding.checkboxUsoPredio.isEnabled = true
            binding.checkboxTrifoliar.isEnabled = true

            binding.btnFinalizar.visibility = View.VISIBLE
        }
    }

    private fun handleFinalizar(result: FinalizarRes?) {
        if (result === null){
            binding.tvError.setText("No se logro establecer conexión con el servidor. Rsultado null.")
            binding.tvError.visibility = View.VISIBLE
            return
        }

        if(!result.Success){
            binding.tvError.visibility = View.GONE
            binding.tvError.text = "Algo sucedio: ${result.Mensaje}"
            binding.tvError.visibility = View.VISIBLE
            return
        } else {
            this.finish()
        }
    }

    override fun onDestroy() {
        binding.checkboxUsoPredio.isEnabled = false
        binding.checkboxTrifoliar.isEnabled = false
        binding.tvError.visibility = View.GONE
        binding.btnFinalizar.visibility = View.GONE
        binding.etNumeroFinalizaciN.text.clear()
        //utils.progressDialog!!.dismiss()
        super.onDestroy()
    }
}