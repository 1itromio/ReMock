package dev.romio.remock.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mock_response_headers",
    indices = [
        Index(
            name = "mock_response_headers_response_id",
            value = ["response_id"]
        )
    ],
    foreignKeys = [
        ForeignKey(
            entity = MockResponseEntity::class,
            parentColumns = ["id"],
            childColumns = ["response_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
internal data class MockResponseHeadersEntity(
    @ColumnInfo(name = "response_id")
    val responseId: Long,

    @ColumnInfo("header_key")
    val headerKey: String,

    @ColumnInfo("header_value")
    val headerValue: String,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)