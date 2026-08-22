package com.miapp.custodio2.ClasesRequest

//data class Foto(val Id: Int, val Accion:String, val Fecha: String, val Foto: String,
//                val Latitud:String, val Longitud: String, val Token: String)
data class Foto(val Token: String, val Accion:String, val Latitud:String, val Longitud: String,
                val Foto: String, val TipoFotografia: Int)
