package com.miapp.custodio2.Utils

import android.location.Location

interface CallbackUbi {
    fun onLocationReceived(location: Location)
    fun onLocationError(error: String)
}