package dev.romio.remock

import android.content.Context
import androidx.room.Room
import dev.romio.remock.data.room.ReMockDatabase
import dev.romio.remock.data.room.dao.RequestDao
import dev.romio.remock.data.room.dao.ResponseDao
import dev.romio.remock.data.room.dao.ResponseHeadersDao
import dev.romio.remock.data.store.ReMockStore
import dev.romio.remock.ui.nav.ReMockRouteNavigator
import dev.romio.remock.ui.nav.RouteNavigator

internal object ReMockGraph {

    private lateinit var database: ReMockDatabase

    private val requestDao: RequestDao
        get() = database.requestDao()

    private val responseDao: ResponseDao
        get() = database.responseDao()

    private val responseHeadersDao: ResponseHeadersDao
        get() = database.responseHeadersDao()

    internal val reMockStore by lazy {
        ReMockStore(requestDao, responseDao, responseHeadersDao)
    }

    internal val routeNavigator: RouteNavigator by lazy {
        ReMockRouteNavigator()
    }

    fun initialize(context: Context) {
        if(ReMockGraph::database.isInitialized.not()) {
            synchronized(this) {
                if(ReMockGraph::database.isInitialized.not()) {
                    database = Room.databaseBuilder(
                        context = context.applicationContext,
                        klass = ReMockDatabase::class.java,
                        name = "remock-db.db"
                    ).createFromAsset("database/remock-db.db")
                        .build()
                }
            }
        }
    }
}