package dev.romio.remock.repository

import dev.romio.remock.data.room.dao.RequestDao
import dev.romio.remock.domain.IReMockRepository

class ReMockRepository(
    private val requestDao: RequestDao
): IReMockRepository {
}