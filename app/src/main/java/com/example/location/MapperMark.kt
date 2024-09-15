package com.example.location

import com.example.location.data.roomrepo.MarkEntity
import com.example.location.domain.Mark

fun MarkEntity.mapToMark()=
        Mark(
            id,
            coordinateLong,
            coordinateLat
        )
fun Mark.mapToMarkEntity()=
    MarkEntity(
        id = id,
        coordinateLong,
        coordinateLat
    )