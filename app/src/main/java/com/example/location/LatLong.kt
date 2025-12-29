package com.example.location

import java.io.Serializable

data class LatLong(
    var lat: Double = 0.0,
    var long: Double = 0.0
) : Serializable