package com.miapp.custodio2.Utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class ServicioUbicacion(private val context: Context, private val client: FusedLocationProviderClient): LocationClient {
    override fun getLocationUpdates(intervalo: Long): Flow<Location> {

            return callbackFlow {
                println("WENAS TARDESSSSSSSSSSSSSSSSSSS....")
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(context, "Permisos no esta dados", Toast.LENGTH_LONG).show()
                    println("/////////// NO HAY PERMISOS GRANTED")
                }

                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

                val gpsHabilitado = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val connHabilitada = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                if (!gpsHabilitado && !connHabilitada){
                    Toast.makeText(context, "GPS no esta habilitado", Toast.LENGTH_LONG).show()
                    println("/////////// GPS NO DISPONIBLE")
                }

                val request = LocationRequest.create()
                    .setInterval(intervalo)
                    .setFastestInterval(intervalo)

                val locationCallback = object : LocationCallback(){
                    override fun onLocationResult(result: LocationResult) {
                        super.onLocationResult(result)
                        result.locations.lastOrNull()?.let { location ->
                            launch {
                                send(location)
                                if (location == null){
                                    println("/////////// LOC EN NULL")
                                } else {
                                    println("////////// LAT: ${location.longitude.toString()}")
                                }
                            }
                        }
                    }
                }

                client.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())

                awaitClose {
                    println("ALTO POLICIA !!!!!!!")
                    client.removeLocationUpdates(locationCallback)
                    println("Se detuvieron las actualizaciones de ubicación")
                }

            }
        }

}