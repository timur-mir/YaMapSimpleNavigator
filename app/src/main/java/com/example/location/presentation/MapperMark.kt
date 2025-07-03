package com.example.location.presentation

import com.example.location.data.roomrepo.MarkEntity
import com.example.location.domain.Mark

fun MarkEntity.mapToMark()=
        Mark(
            id,
            coordinateLong,
            coordinateLat,
            photoFileName
        )
fun Mark.mapToMarkEntity()=
    MarkEntity(
        id = id,
        coordinateLong,
        coordinateLat,
        photoFileName
    )