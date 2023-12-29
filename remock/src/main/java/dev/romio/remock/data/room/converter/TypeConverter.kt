package dev.romio.remock.data.room.converter

import androidx.room.TypeConverter
import dev.romio.remock.domain.model.ResponseContentType

class TypeConverter {

    @TypeConverter
    fun stringToResponseType(string: String?): ResponseContentType? {
        return ResponseContentType.entries.find { it.name == string }
    }

    @TypeConverter
    fun responseTypeToString(responseType: ResponseContentType?): String? {
        return responseType?.name
    }
}