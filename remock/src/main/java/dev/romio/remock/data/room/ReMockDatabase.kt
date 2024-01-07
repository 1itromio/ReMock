package dev.romio.remock.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.romio.remock.data.room.converter.TypeConverter
import dev.romio.remock.data.room.dao.RequestDao
import dev.romio.remock.data.room.dao.ResponseDao
import dev.romio.remock.data.room.dao.ResponseHeadersDao
import dev.romio.remock.data.room.entity.MockResponseEntity
import dev.romio.remock.data.room.entity.MockResponseHeadersEntity
import dev.romio.remock.data.room.entity.RequestEntity

@Database(
    entities = [
        RequestEntity::class,
        MockResponseEntity::class,
        MockResponseHeadersEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(TypeConverter::class)
abstract class ReMockDatabase: RoomDatabase() {

    abstract fun requestDao(): RequestDao

    abstract fun responseDao(): ResponseDao

    abstract fun responseHeadersDao(): ResponseHeadersDao
}