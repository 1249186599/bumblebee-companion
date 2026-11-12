package com.example.bumblebee.pa4453393.platform

import android.app.Application
import com.pico.spatial.ui.foundation.dsl.launch
import com.example.bumblebee.pa4453393.mainApp

class SpatialApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        launch(::mainApp)
    }
}
