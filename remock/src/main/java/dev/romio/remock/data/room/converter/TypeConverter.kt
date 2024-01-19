package dev.romio.remock.data.room.converter

import androidx.room.TypeConverter
import dev.romio.remock.domain.model.ResponseContentType
import okhttp3.Protocol

internal class TypeConverter {

    @TypeConverter
    fun stringToResponseType(string: String?): ResponseContentType? {
        return ResponseContentType.entries.find { it.name == string }
    }

    @TypeConverter
    fun responseTypeToString(responseType: ResponseContentType?): String? {
        return responseType?.name
    }

    @TypeConverter
    fun stringToProtocol(string: String?): Protocol? {
        return string?.let { Protocol.get(it) }
    }

    @TypeConverter
    fun protocolToString(protocol: Protocol?): String? {
        return protocol?.toString()
    }
}