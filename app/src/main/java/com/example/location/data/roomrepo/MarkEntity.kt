package com.example.location.data.roomrepo

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yandex.mapkit.geometry.Point
@Entity(tableName ="marksTable")
data class MarkEntity (
 @PrimaryKey
    var id:Int,
    var coordinateLong:Double,
    var coordinateLat:Double,
    var photoFileName:String
        )
