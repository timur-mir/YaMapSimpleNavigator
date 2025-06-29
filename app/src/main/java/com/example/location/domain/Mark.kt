package com.example.location.domain

import com.yandex.mapkit.geometry.Point

data class Mark(
    var id:Int,
    var coordinateLong:Double,
    var coordinateLat:Double,
    var photoFileName: String ="Place_$id.jpg"

)
