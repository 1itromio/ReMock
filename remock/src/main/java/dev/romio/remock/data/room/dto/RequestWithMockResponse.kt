package dev.romio.remock.data.room.dto

import androidx.room.Embedded
import androidx.room.Relation
import dev.romio.remock.data.room.entity.MockResponseEntity
import dev.romio.remock.data.room.entity.RequestEntity

data class RequestWithMockResponse(
    @Embedded val request: RequestEntity,
    @Relation(
        entity = MockResponseEntity::class,
        parentColumn = "id",
        entityColumn = "request_id"
    )
    val mockResponseList: List<MockResponseWithHeaders>
)