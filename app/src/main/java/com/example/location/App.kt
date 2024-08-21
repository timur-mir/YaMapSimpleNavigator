package com.example.location

import android.app.Application
import android.content.Context
import com.yandex.mapkit.location.Location

class App:Application() {
    companion object{
        var appContext: Context? =null
    }

    override fun onCreate() {
        super.onCreate()
        appContext=applicationContext
    }
    object LocalHelp {
        var loc: Location? = null
        var offOn=false
        var markAdd=false
        var locMark: Location? = null
    }
}