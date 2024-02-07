package com.miapp.custodio2.Utils

import android.app.Activity
import android.content.Context

class Preferencias() {

    fun setGlobalData(act: Activity, key: String, value: String?){
        val sharedPreference =  act.getSharedPreferences("GLOBAL", Context.MODE_PRIVATE)
        var editor = sharedPreference.edit()
        //Token Mision
        if(value == null){
            editor.putString(key,"null")
        } else {
            editor.putString(key,value)
        }
        editor.apply()
    }

    fun getGlobalData(act: Activity, key: String): String{
        var preferences = act.getSharedPreferences("GLOBAL", Context.MODE_PRIVATE)
        return preferences.getString(key, "null").toString()
    }

    fun updateGlobalData(act: Activity, key: String, newValue: String){
        val sharedPreference =  act.getSharedPreferences("GLOBAL", Context.MODE_PRIVATE)
        var editor = sharedPreference.edit()

        //Eliminar los valores
        editor.remove(key)
        //Le asigna el nuevo valor para actualizar la preferencia
        editor.putString(key, newValue)
        editor.apply()
    }


    fun extSetGlobalData(act: ExtraUtils, key: String, value:String){
        val sharedPreference =  act.getSharedPreferences("GLOBAL", Context.MODE_PRIVATE)
        var editor = sharedPreference.edit()
        //Token Mision
        editor.putString(key,value)
        editor.apply()
    }
    fun extGetGlobalData(act: ExtraUtils, key: String): String{
        var preferences = act.getSharedPreferences("GLOBAL", Context.MODE_PRIVATE)
        return preferences.getString(key, "null").toString()
    }
    fun extUpdateGlobalData(act: ExtraUtils, key: String, newValue: String){
        val sharedPreference =  act.getSharedPreferences("GLOBAL", Context.MODE_PRIVATE)
        var editor = sharedPreference.edit()

        //Eliminar los valores
        editor.remove(key)
        //Le asigna el nuevo valor para actualizar la preferencia
        editor.putString(key, newValue)
        editor.apply()
    }

}