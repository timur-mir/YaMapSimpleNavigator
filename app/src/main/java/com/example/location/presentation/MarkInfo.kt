package com.example.location.presentation

import com.yandex.mapkit.geometry.Point

data class MarkInfo(
    val coord: Point,
    val resourseUri: Int,
    val aboutMark: String
)
