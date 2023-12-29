package dev.romio.remock.util

import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ReMockUtilsTest {

    @Test
    fun tokenizeToStringArrayTest() {
        assertThat(ReMockUtils.tokenizeToStringArray(null, "/", true, true)).isEmpty()
        assertThat(ReMockUtils.tokenizeToStringArray("abc", "/", false, false)).containsExactly("abc")
        assertThat(ReMockUtils.tokenizeToStringArray("abc/bcd", "/", false, false)).containsExactly("abc", "bcd")
        assertThat(ReMockUtils.tokenizeToStringArray("abc/ bcd", "/", true, false)).containsExactly("abc", "bcd")
        assertThat(ReMockUtils.tokenizeToStringArray("abc/ bcd", "/", false, false)).containsExactly("abc", " bcd")
        assertThat(ReMockUtils.tokenizeToStringArray("abc/ bcd/ ", "/", true, true)).containsExactly("abc", "bcd")
        assertThat(ReMockUtils.tokenizeToStringArray("abc/ bcd/ ", "/", false, true)).containsExactly("abc", " bcd", " ")
        assertThat(ReMockUtils.tokenizeToStringArray("abc/ bcd/ ", "/", false, false)).containsExactly("abc", " bcd", " ")
        assertThat(ReMockUtils.tokenizeToStringArray("abc/ bcd/ ", "/", true, false)).containsExactly("abc", "bcd", "")
        assertThat(ReMockUtils.tokenizeToStringArray("abc/ bcd/ ", ",", true, false)).containsExactly("abc/ bcd/")
    }

    @Test
    fun testIfExpressionEvaluationTrue() {
        val body = "{\"contactNumber\":\"123456\",\"name\":\"Bhupender Jogi\",\"age\":30,\"address\":{\"city\":\"Bangalore\",\"apartments\":[\"a1\",\"a2\",\"a3\"]}}"
        val bodyMap = ReMockUtils.toMapOrList(Json.parseToJsonElement(body))
        val values = mapOf(
            "header" to mapOf(
                "X-Farm-Request-Id" to "abc"
            ),
            "query" to mapOf(
                "count" to "1"
            ),
            "param" to mapOf(
                "userId" to "a1b1c1d1"
            ),
            "body" to bodyMap
        )
        assertThat(ReMockUtils.isExpressionEvaluationTrue("header.\"X-Farm-Request-Id\" == \"abc\"", values)).isTrue()
        assertThat(ReMockUtils.isExpressionEvaluationTrue("body.age == 30", values)).isTrue()
        assertThat(ReMockUtils.isExpressionEvaluationTrue("body.age > 20", values)).isTrue()
        assertThat(ReMockUtils.isExpressionEvaluationTrue("(body.address.apartments[1]) == \"a2\"", values)).isTrue()
        assertThat(ReMockUtils.isExpressionEvaluationTrue("(body.some.random == \"a2\"", values)).isFalse()
    }

}