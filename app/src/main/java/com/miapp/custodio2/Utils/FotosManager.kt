package com.miapp.custodio2.Utils

import com.miapp.custodio2.ClasesRequest.Foto
import com.miapp.custodio2.ClasesRequest.Models.MissionPhotos
import com.miapp.custodio2.ClasesRequest.Models.Photo
import com.miapp.custodio2.ClasesRequest.Models.TypePhoto

class FotosManager(
    //Constructor
    //val photos: MissionPhotos
){
    var photos: MissionPhotos = MissionPhotos(null, null, null)

    fun assignPhoto(tipoFoto: TypePhoto, photo: Photo?){
        if (tipoFoto == TypePhoto.MARCHAMO){
            photos.photoMarchamo = photo
        } else if (tipoFoto == TypePhoto.CABEZAL){
            photos.photoCabezal = photo
        } else if (tipoFoto == TypePhoto.COLA){
            photos.photoCola = photo
        }
    }

    fun photosList(): MutableList<Photo?> {
        var lista = mutableListOf<Photo?>()
        lista.add(photos.photoCabezal)
        lista.add(photos.photoMarchamo)
        lista.add(photos.photoCola)
        return lista
    }

    fun photosToFotoList(): MutableList<Foto> {
        var lista = photosList()

        var fList = mutableListOf<Foto>()
        for (photo in lista){
            val p: Photo = photo ?: continue
            fList.add(p.fotoReq)
        }

        return fList
    }

    fun clearAllPhotos(){
        photos = MissionPhotos(null, null, null)
    }
}