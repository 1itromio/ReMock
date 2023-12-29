package dev.romio.remock.data.room.dto

import androidx.room.Embedded
import androidx.room.Relation
import dev.romio.remock.data.room.entity.MockResponseEntity
import dev.romio.remock.data.room.entity.MockResponseHeadersEntity

data class MockResponseWithHeaders(
    @Embedded val mockResponse: MockResponseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "response_id"
    )
    val mockResponseHeaders: List<MockResponseHeadersEntity>
)
