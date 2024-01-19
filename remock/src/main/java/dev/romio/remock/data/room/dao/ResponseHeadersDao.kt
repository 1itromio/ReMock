package dev.romio.remock.data.room.dao

import androidx.room.Dao
import dev.romio.remock.data.room.entity.MockResponseHeadersEntity

@Dao
internal abstract class ResponseHeadersDao: BaseDao<MockResponseHeadersEntity> {

}