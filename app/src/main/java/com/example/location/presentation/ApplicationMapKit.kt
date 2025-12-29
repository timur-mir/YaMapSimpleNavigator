package com.example.location.presentation

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
        val appContext: Context = applicationContext()
        MarksDatabaseImpl.initDatabase(this)
        MapKitFactory.setApiKey("b8ef48ad-5b72-4c3d-8f28-9256d0692cd4")
        MapKitFactory.initialize(this)
        MapKitFactory.getInstance().onStart()
    }
    object LocalHelp {
        var currentArea=""
        var lastPoint:Point?=null
        var routeProcess=false
        var speachText=""
        var myLocation: Point? = null
        var loc: Location? = null
        var weatherRequest=false
        var actualLoc: Location? = null
        var actualLocationFlag=false
        var offOn=false
        var offOnUserLayer=true
        var markAdd=false
        var locMark: Location? = null
        var userLocationHide=false
        var latitudeActivity=55.751574
        var longitudeActivity=37.573856
        var latitudeDeviceOldPosition=0.0
        var longitudeDeviceOldPosition=0.0
        var marksSize=0
        var lastIdValue=0
        var activityClose=false

    }
}
