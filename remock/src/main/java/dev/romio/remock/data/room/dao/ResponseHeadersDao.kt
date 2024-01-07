package dev.romio.remock.data.room.dao

import androidx.room.Dao
import dev.romio.remock.data.room.entity.MockResponseHeadersEntity

@Dao
abstract class ResponseHeadersDao: BaseDao<MockResponseHeadersEntity> {

}