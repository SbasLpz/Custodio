package com.miapp.custodio2.ClasesRequest

object ApiErrorHandler{
    var cod = ""
    var msg = ""
    var desc = ""

    fun createError (cod: String, msg: String, desc: String) {
        this.cod = cod
        this.msg = msg
        this.desc = desc
    }
}