package dev.romio.remock

import android.content.Context
import androidx.room.Room
import dev.romio.remock.data.room.ReMockDatabase
import dev.romio.remock.data.room.dao.RequestDao
import dev.romio.remock.data.room.dao.ResponseDao
import dev.romio.remock.data.store.ReMockStore
import dev.romio.remock.ui.nav.ReMockRouteNavigator
import dev.romio.remock.ui.nav.RouteNavigator

internal object ReMockGraph {

    private lateinit var database: ReMockDatabase

    private val requestDao: RequestDao
        get() = database.requestDao()

    private val responseDao: ResponseDao
        get() = database.responseDao()

    internal val reMockStore by lazy {
        ReMockStore(requestDao, responseDao)
    }

    internal val routeNavigator: RouteNavigator by lazy {
        ReMockRouteNavigator()
    }

    fun initialize(context: Context) {
        if(ReMockGraph::database.isInitialized.not()) {
            synchronized(this) {
                if(ReMockGraph::database.isInitialized.not()) {
                    database = Room.databaseBuilder(
                        context.applicationContext,
                        ReMockDatabase::class.java, "remock-db"
                    ).build()
                }
            }
        }
    }
}