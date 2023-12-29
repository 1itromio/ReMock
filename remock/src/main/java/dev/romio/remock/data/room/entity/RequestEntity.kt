package dev.romio.remock.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "request",
    indices = [
        Index(
            value = ["request_method", "url_hash"],
            name = "uk_request_method_url_hash",
            unique = true
        )
    ]
)
data class RequestEntity(
    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "url_hash")
    val urlHash: Int,

    @ColumnInfo(name = "request_method")
    val requestMethod: String,

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
)