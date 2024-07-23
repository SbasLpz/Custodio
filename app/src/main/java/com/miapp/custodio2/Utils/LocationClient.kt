package com.miapp.custodio2.Utils

import android.location.Location
import java.util.concurrent.Flow

interface LocationClient {
    fun getLocationUpdates(intervalo: Long): kotlinx.coroutines.flow.Flow<Location>

    class LocationException(mensaje: String): Exception()
}