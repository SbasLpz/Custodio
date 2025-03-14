package com.miapp.custodio2.ClasesRequest

//PARA RECIBIR
data class Autenticar(val AsignadoVisto:String, val CambioPassword: String, val Codigo: Int, val Escopeta: String, val FechaContacto: String, val FechaPosicionamiento:String, val FechaSalida:String,
                    val FechaSolicitada:String, val LugarInicio:String, val LugarSalida:String, val Marchamo: String, val MarchamoFiscal: String,
                      val MarchamoGPS:String, val Mensaje: String, val Nombramiento: String, val NombreCustodio:String, val NombreUsuario: String, val Notas: String,
                      val NumeroContenedor:String, val NumeroViaje: String, val PasswordUsuario: String, val Piloto:String, val Placa: String, val PlacaTC: String, val PrimerUso: String,
                      var Rui:Int, val Sellado: String, val Success: String, val TelefonoEmergencia:String, val TelefonoPiloto: String, val Token: String, val TotalServiciosApp: String,
                      val TotalServiciosMes: String, val UltimoUso: String)
