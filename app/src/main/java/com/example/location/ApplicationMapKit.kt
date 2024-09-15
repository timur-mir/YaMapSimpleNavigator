package com.example.location

import android.app.Application
import android.content.Context
import com.example.location.data.roomrepo.MarksDatabaseImpl
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.Location

class ApplicationMapKit: Application(){
    init {
        instance = this
    }

    companion object {
        private var instance: ApplicationMapKit? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        val appContext: Context = ApplicationMapKit.applicationContext()
        MarksDatabaseImpl.initDatabase(this)
        MapKitFactory.setApiKey("b8ef48ad-5b72-4c3d-8f28-9256d0692cd4")
        MapKitFactory.initialize(this)
        MapKitFactory.getInstance().onStart()
    }
    object LocalHelp {
        var myLocation: Point? = null
        var loc: Location? = null
        var offOn=false
        var offOnUserLayer=false
        var markAdd=false
        var locMark: Location? = null
        var latitudeActitvity=55.751574
        var longitudeActivity=37.573856
    }
}
