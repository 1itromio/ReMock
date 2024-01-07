package dev.romio.remock

import android.app.Application
import dev.romio.remock.di.Graph

class ReMockApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Graph.initialize(applicationContext)
    }
}