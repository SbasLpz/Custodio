package com.miapp.custodio2.Utils

import android.widget.TextView

interface Checker {
    fun updateTextView()

    fun getTxtVersion(): TextView
}