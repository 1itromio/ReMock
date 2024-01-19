package dev.romio.remock.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.romio.remock.data.room.dto.MockResponseWithHeaders
import dev.romio.remock.data.room.entity.MockResponseEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal abstract class ResponseDao: BaseDao<MockResponseEntity> {

    @Transaction
    @Query("SELECT * FROM mock_response WHERE request_id = :requestId AND id = :responseId")
    abstract suspend fun getMockResponseWithHeaders(requestId: Long, responseId: Long): MockResponseWithHeaders?

    @Transaction
    @Query("SELECT * FROM mock_response WHERE request_id = :requestId")
    abstract fun getResponseFlowByRequestId(requestId: Long): Flow<List<MockResponseWithHeaders>>

    @Query("DELETE FROM mock_response WHERE id = :responseId")
    abstract suspend fun deleteResponseByResponseId(responseId: Long)
}