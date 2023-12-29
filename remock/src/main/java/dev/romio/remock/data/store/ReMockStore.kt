package dev.romio.remock.data.store

import dev.romio.remock.data.room.dao.RequestDao
import dev.romio.remock.data.room.dao.ResponseDao
import dev.romio.remock.data.room.dto.MockResponseWithHeaders
import dev.romio.remock.data.room.dto.RequestWithMockResponse
import dev.romio.remock.data.room.entity.MockResponseEntity
import dev.romio.remock.data.room.entity.RequestEntity
import kotlinx.coroutines.flow.Flow

class ReMockStore(
    private val requestDao: RequestDao,
    private val responseDao: ResponseDao
) {

    fun getAllRequests(): Flow<List<RequestEntity>> {
        return requestDao.getRequests()
    }

    fun getAllRequestsByRequestMethod(requestMethod: String): List<RequestEntity> {
        return requestDao.getRequests(requestMethod)
    }

    fun getRequestWithMockResponseByRequestIdBlocking(id: Long): RequestWithMockResponse? {
        return requestDao.getRequestWithMockResponseByRequestIdBlocking(id)
    }

    suspend fun getRequestWithMockResponseByRequestId(id: Long): RequestWithMockResponse? {
        return requestDao.getRequestWithMockResponseByRequestId(id)
    }

    suspend fun getMockResponseWithHeaders(requestId: Long, responseId: Long): MockResponseWithHeaders? {
        return responseDao.getMockResponseWithHeaders(requestId, responseId)
    }

    suspend fun getRequestByRequestId(requestId: Long): RequestEntity? {
        return requestDao.getById(requestId)
    }

    suspend fun addNewRequest(requestEntity: RequestEntity): Long {
        return requestDao.insert(requestEntity)
    }

    suspend fun removeRequest(request: RequestEntity): Int {
        return requestDao.delete(request)
    }

    suspend fun saveResponse(responseEntity: MockResponseEntity): Long {
        return responseDao.insert(responseEntity)
    }

    fun getResponseFlowByRequestId(requestId: Long): Flow<List<MockResponseWithHeaders>> {
        return responseDao.getResponseFlowByRequestId(requestId)
    }

    suspend fun deleteResponseById(responseId: Long) {
        return responseDao.deleteResponseByResponseId(responseId)
    }
}