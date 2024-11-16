package com.miapp.custodio2.ClasesRequest

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST

interface APIService {

    @POST("Autenticar")
    suspend fun endInicionSesion(@Body requestBody: RequestBody): retrofit2.Response<ResponseBody>

    @POST("NuevaMision")
    suspend fun endNuevaMision(@Body requestBody: RequestBody): retrofit2.Response<ResponseBody>

    @POST("Buttons")
    suspend fun endBotones(@Body requestBody: RequestBody): retrofit2.Response<ResponseBody>

    @POST("GrabarMonitoreo")
    suspend fun endAcciones(@Body requestBody: RequestBody): retrofit2.Response<ResponseBody>

    @POST("DireccionEnvio")
    suspend fun endGetDireccion(@Body requestBody: RequestBody): retrofit2.Response<ResponseBody>

    @POST("GrabarFoto")
    suspend fun endSaveFoto(@Body requestBody: RequestBody): retrofit2.Response<ResponseBody>

    @POST("GrabarFotoTipo")
    suspend fun endGrabarFotoTipo(@Body requestBody: RequestBody): retrofit2.Response<ResponseBody>

    @POST("DataMisionUpdate")
    suspend fun endUpdate(@Body requestBody: RequestBody): retrofit2.Response<ResponseBody>

    @POST("ActualizarPassword")
    suspend fun createNewPassword(@Body requestBody: RequestBody): retrofit2.Response<ResponseBody>

    @POST("CheckMision")
    suspend fun endCheckMission(@Body requestBody: RequestBody): retrofit2.Response<ResponseBody>

    /** Cambiar Finalizar por el nombre de la API para el Endpoint **/
    @POST("Finalizar")
    suspend fun endFinalizar(@Body requestBody: RequestBody): retrofit2.Response<ResponseBody>
    @POST("CheckFinalizar")
    suspend fun endCheckFinalizar(@Body requestBody: RequestBody): retrofit2.Response<ResponseBody>


}