package com.example.location

import android.app.Application
import android.content.Context
import com.yandex.mapkit.MapKitFactory

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
        MapKitFactory.setApiKey("b8ef48ad-5b72-4c3d-8f28-9256d0692cd4")
    }
}
