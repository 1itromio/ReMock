package dev.romio.remock.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.romio.remock.domain.model.ResponseContentType

@Entity(
    tableName = "mock_response",
    indices = [
        Index(
            name = "mock_response_request_id",
            value = ["request_id"]
        )
    ],
    foreignKeys = [
        ForeignKey(
            entity = RequestEntity::class,
            parentColumns = ["id"],
            childColumns = ["request_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MockResponseEntity(
    @ColumnInfo(name = "request_id")
    val requestId: Long,

    @ColumnInfo(name = "response_type")
    val responseType: ResponseContentType,

    @ColumnInfo(name = "response_code")
    val responseCode: Int,

    @ColumnInfo(name = "message")
    val message: String?,

    @ColumnInfo(name = "when_expression")
    val whenExpression: String? = null,

    @ColumnInfo(name = "response")
    val responseBody: String? = null,

    // In Milliseconds
    @ColumnInfo(name = "response_delay")
    val responseDelay: Long? = null,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)