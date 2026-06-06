package com.miapp.custodio2.ClasesRequest.Models

import android.net.Uri
import com.miapp.custodio2.ClasesRequest.Foto

data class Photo(val tipo: TypePhoto, val uri: Uri, val fotoReq: Foto)
