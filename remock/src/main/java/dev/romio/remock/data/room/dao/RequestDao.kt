package dev.romio.remock.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.romio.remock.data.room.dto.RequestWithMockResponse
import dev.romio.remock.data.room.entity.RequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal abstract class RequestDao: BaseDao<RequestEntity> {

    @Query("SELECT * FROM request")
    abstract fun getRequests(): Flow<List<RequestEntity>>

    @Query("SELECT * FROM request WHERE request_method = :requestMethod")
    abstract fun getRequests(requestMethod: String): List<RequestEntity>

    @Transaction
    @Query("SELECT * FROM request WHERE id = :id")
    abstract fun getRequestWithMockResponseByRequestIdBlocking(id: Long): RequestWithMockResponse?

    @Transaction
    @Query("SELECT * FROM request WHERE id = :id")
    abstract suspend fun getRequestWithMockResponseByRequestId(id: Long): RequestWithMockResponse?

    @Query("SELECT * FROM request WHERE id = :id")
    abstract suspend fun getById(id: Long): RequestEntity?
}