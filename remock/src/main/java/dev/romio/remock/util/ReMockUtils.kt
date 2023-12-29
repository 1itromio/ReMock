package dev.romio.remock.util

import com.ezylang.evalex.Expression
import dev.romio.remock.domain.model.ResponseContentType
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import java.util.StringTokenizer


object ReMockUtils {

    fun tokenizeToStringArray(
        str: String?, delimiters: String, trimTokens: Boolean, ignoreEmptyTokens: Boolean
    ): Array<String> {
        if (str == null) {
            return emptyArray()
        }
        val st = StringTokenizer(str, delimiters)
        val tokens: MutableList<String> = ArrayList()
        while (st.hasMoreTokens()) {
            var token = st.nextToken()
            if (trimTokens) {
                token = token.trim { it <= ' ' }
            }
            if (!ignoreEmptyTokens || token.isNotEmpty()) {
                tokens.add(token)
            }
        }
        return tokens.toTypedArray()
    }

    fun isExpressionEvaluationTrue(expressionStr: String, values: Map<String, Any?>): Boolean {
        return try {
            val expression = Expression(expressionStr).withValues(values)
            val result = expression.evaluate()
            result.isBooleanValue && result.booleanValue
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }

    fun getMediaType(responseContentType: ResponseContentType): MediaType? {
        return when(responseContentType) {
            ResponseContentType.JSON -> "application/json".toMediaTypeOrNull()
            ResponseContentType.XML -> "application/xml".toMediaTypeOrNull()
            ResponseContentType.TEXT -> "application/text".toMediaTypeOrNull()
        }
    }

    fun Map<String, String?>.castValuesToPrimitive(): Map<String, Any?> {
        return this.entries.associate { it.key to castToPrimitive(it.value) }
    }

    fun castToPrimitive(value: String?): Any? {
        if(value == null) {
            return null
        }
        return value.toIntOrNull() ?:
        value.toLongOrNull() ?:
        value.toDoubleOrNull() ?:
        value.toBooleanStrictOrNull() ?: value
    }

    @Throws(JSONException::class)
    fun toMapOrList(element: JsonElement): Any? {
        return when(element) {
            is JsonArray -> {
                val list = mutableListOf<Any?>()
                val iterator = element.iterator()
                while(iterator.hasNext()) {
                    list.add(toMapOrList(iterator.next()))
                }
                list
            }
            is JsonObject -> {
                val map = mutableMapOf<String, Any?>()
                val keys = element.keys
                val iterator = keys.iterator()
                while(iterator.hasNext()) {
                    val key = iterator.next()
                    map[key] = toMapOrList(element[key] ?: JsonNull)
                }
                map
            }
            is JsonNull -> null
            is JsonPrimitive -> {
                element.intOrNull ?:
                element.longOrNull ?:
                element.doubleOrNull ?:
                element.booleanOrNull ?:
                element.contentOrNull
            }
            else -> null
        }
    }
}