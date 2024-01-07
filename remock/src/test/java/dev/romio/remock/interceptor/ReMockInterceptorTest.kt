package dev.romio.remock.interceptor

import android.content.Context
import dev.romio.remock.ReMockGraph
import dev.romio.remock.data.room.dto.MockResponseWithHeaders
import dev.romio.remock.data.room.dto.RequestWithMockResponse
import dev.romio.remock.data.room.entity.MockResponseEntity
import dev.romio.remock.data.room.entity.MockResponseHeadersEntity
import dev.romio.remock.data.room.entity.RequestEntity
import dev.romio.remock.data.store.ReMockStore
import dev.romio.remock.domain.model.ResponseContentType
import dev.romio.remock.matcher.ReMockPathMatcher
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class ReMockInterceptorTest {

    private val pathMatcher by lazy {
        ReMockPathMatcher()
    }
    private lateinit var reMockStore: ReMockStore
    private lateinit var context: Context
    private lateinit var interceptor: ReMockInterceptor

    private val allRequests = listOf(
        RequestEntity("https://example.com/users/{userId}/get", 123, "GET", 0),
        RequestEntity("https://example.com/users/{userId}/post", 123, "POST", 1),
        RequestEntity("https://example.com/payment/{paymentId}/get", 123, "GET", 2),
        RequestEntity("https://example.com/order/{orderId}/get", 123, "GET", 3),
    )
//
    private val allMockResponses = listOf(
        MockResponseEntity(0, ResponseContentType.JSON, 200, "OK", null, "{\"name\":\"response1\"}", Protocol.HTTP_2, 200, 0),
        MockResponseEntity(0, ResponseContentType.JSON, 200, "OK", "header.reqHeader1 == \"value1\" && header.reqHeader2 == \"value2\" && param.userId == \"U1234\" && query.userId == \"U1234\"", "{\"name\":{\"first\": \"test\",\"last\": \"test2\"}}", Protocol.HTTP_2, 200, 1),
        MockResponseEntity(1, ResponseContentType.JSON, 200, "OK", "header.reqHeader1 == \"value1\" && header.reqHeader2 == \"value2\" && param.userId == \"U1234\" && query.age > 28 && body.name.first == \"test\"", "{\"name\":{\"first\": \"test\",\"last\": \"test2\"}}", Protocol.HTTP_2,  200, 2),
        MockResponseEntity(2, ResponseContentType.JSON, 200, "OK", "header.reqHeader1 == \"value1\" && header.reqHeader2 == \"value2\" && param.userId == \"U1234\" && query.age > 28 && body.name.first == \"test\"", "{\"name\":{\"first\": \"test\",\"last\": \"test2\"}}", Protocol.HTTP_2, 200, 3),
    )

    private val allMockResponseHeaders = listOf(
        MockResponseHeadersEntity(0, "header1", "value1", 0),
        MockResponseHeadersEntity(0, "header2", "value2", 1),
        MockResponseHeadersEntity(1, "header3", "value3", 2),
        MockResponseHeadersEntity(1, "header4", "value4", 3),
        MockResponseHeadersEntity(2, "header5", "value5", 2),
        MockResponseHeadersEntity(2, "header6", "value6", 3),
    )

    private val allRequestWithResponse by lazy {
        allRequests.map { request ->
            RequestWithMockResponse(
                request,
                allMockResponses.filter { response ->
                    request.id == response.requestId
                }.map { response ->
                    MockResponseWithHeaders(response, allMockResponseHeaders.filter { it.responseId == response.id })
                }
            )
        }
    }

    @Before
    fun setUp() {
        reMockStore = mockk()
        context = mockk()
        every {
            context.applicationContext
        } returns context
        mockkObject(ReMockGraph)
        every {
            ReMockGraph.initialize(any())
        } answers {
            println("Initializing ReMockGraph")
        }
        interceptor = ReMockInterceptor(context, pathMatcher, reMockStore)
    }

    @Test
    fun testBasicInterception() {
        val request = Request.Builder()
            .url("https://example.com/users/U1234/get")
            .get()
            .build()

        val chain = mockk<Interceptor.Chain>()

        every {
            chain.request()
        } returns request

        val methodSlot = slot<String>()
        every {
            reMockStore.getAllRequestsByRequestMethod(capture(methodSlot))
        } answers {
            allRequests.filter { it.requestMethod == methodSlot.captured }
        }

        val requestIdSlot = slot<Long>()
        every {
            reMockStore.getRequestWithMockResponseByRequestIdBlocking(capture(requestIdSlot))
        } answers {
            allRequestWithResponse.firstOrNull { it.request.id == requestIdSlot.captured }
        }

        val startTime = System.currentTimeMillis()
        val response = interceptor.intercept(chain)
        val diff = System.currentTimeMillis() - startTime
        assertThat(response.body?.string() == "{\"name\":\"response1\"}").isTrue()
        assertThat(response.code == 200).isTrue()
        assertThat(response.message == "OK").isTrue()
        assertThat(response.headers.size == 2).isTrue()
        assertThat(response.header("header1", null) == "value1").isTrue()
        assertThat(response.header("header2", null) == "value2").isTrue()
        assertThat(diff in 200..210).isTrue()
    }

    @Test
    fun testForRequestNotInResponse() {
        val request = Request.Builder()
            .url("https://example.com/test-url/U1234/get")
            .get()
            .build()

        val chain = mockk<Interceptor.Chain>()

        every {
            chain.request()
        } returns request

        val methodSlot = slot<String>()
        every {
            reMockStore.getAllRequestsByRequestMethod(capture(methodSlot))
        } answers {
            allRequests.filter { it.requestMethod == methodSlot.captured }
        }

        every {
            chain.proceed(any())
        } returns Response.Builder()
            .request(request)
            .code(200)
            .message("OK")
            .body("{\"response\":\"ok\"}".toResponseBody("application/json".toMediaTypeOrNull()))
            .protocol(Protocol.HTTP_1_1)
            .build()

        val response = interceptor.intercept(chain)
        assertThat(response.body?.string() == "{\"response\":\"ok\"}").isTrue()
        assertThat(response.code == 200).isTrue()
        assertThat(response.message == "OK").isTrue()
        assertThat(response.headers.size == 2).isFalse()
        assertThat(response.header("header1", null) == "value1").isFalse()
        assertThat(response.header("header2", null) == "value2").isFalse()
    }

    @Test
    fun testEvaluatedResponseIsReturned() {
        val request = Request.Builder()
            .url("https://example.com/users/U1234/get?userId=U1234")
            .header("reqHeader1", "value1")
            .header("reqHeader2", "value2")
            .get()
            .build()

        val chain = mockk<Interceptor.Chain>()

        every {
            chain.request()
        } returns request

        val methodSlot = slot<String>()
        every {
            reMockStore.getAllRequestsByRequestMethod(capture(methodSlot))
        } answers {
            allRequests.filter { it.requestMethod == methodSlot.captured }
        }

        val requestIdSlot = slot<Long>()
        every {
            reMockStore.getRequestWithMockResponseByRequestIdBlocking(capture(requestIdSlot))
        } answers {
            allRequestWithResponse.firstOrNull { it.request.id == requestIdSlot.captured }
        }

        val startTime = System.currentTimeMillis()
        val response = interceptor.intercept(chain)
        val diff = System.currentTimeMillis() - startTime
        assertThat(response.body?.string() == "{\"name\":{\"first\": \"test\",\"last\": \"test2\"}}").isTrue()
        assertThat(response.code == 200).isTrue()
        assertThat(response.message == "OK").isTrue()
        assertThat(response.headers.size == 2).isTrue()
        assertThat(response.header("header3", null) == "value3").isTrue()
        assertThat(response.header("header4", null) == "value4").isTrue()
        print(diff)
        assertThat(diff in 200..210).isTrue()
    }

    @Test
    fun testResponseWhenEvaluationDoesNotMatch() {
        val request = Request.Builder()
            .url("https://example.com/users/U1234/get")
            .header("reqHeader1", "value1")
            .header("reqHeader2", "value2")
            .get()
            .build()

        val chain = mockk<Interceptor.Chain>()

        every {
            chain.request()
        } returns request

        val methodSlot = slot<String>()
        every {
            reMockStore.getAllRequestsByRequestMethod(capture(methodSlot))
        } answers {
            allRequests.filter { it.requestMethod == methodSlot.captured }
        }

        val requestIdSlot = slot<Long>()
        every {
            reMockStore.getRequestWithMockResponseByRequestIdBlocking(capture(requestIdSlot))
        } answers {
            allRequestWithResponse.firstOrNull { it.request.id == requestIdSlot.captured }
        }

        val startTime = System.currentTimeMillis()
        val response = interceptor.intercept(chain)
        val diff = System.currentTimeMillis() - startTime
        assertThat(response.body?.string() == "{\"name\":\"response1\"}").isTrue()
        assertThat(response.code == 200).isTrue()
        assertThat(response.message == "OK").isTrue()
        assertThat(response.headers.size == 2).isTrue()
        assertThat(response.header("header1", null) == "value1").isTrue()
        assertThat(response.header("header2", null) == "value2").isTrue()
        assertThat(diff in 200..210).isTrue()
    }

    @Test
    fun testEvaluatedResponseIsReturnedWhenBodyIsPresent() {
        val request = Request.Builder()
            .url("https://example.com/users/U1234/post?age=29")
            .header("reqHeader1", "value1")
            .header("reqHeader2", "value2")
            .post("{\"name\":{\"first\": \"test\",\"last\": \"test2\"}}".toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        val chain = mockk<Interceptor.Chain>()

        every {
            chain.request()
        } returns request

        val methodSlot = slot<String>()
        every {
            reMockStore.getAllRequestsByRequestMethod(capture(methodSlot))
        } answers {
            allRequests.filter { it.requestMethod == methodSlot.captured }
        }

        val requestIdSlot = slot<Long>()
        every {
            reMockStore.getRequestWithMockResponseByRequestIdBlocking(capture(requestIdSlot))
        } answers {
            allRequestWithResponse.firstOrNull { it.request.id == requestIdSlot.captured }
        }

        val startTime = System.currentTimeMillis()
        val response = interceptor.intercept(chain)
        val diff = System.currentTimeMillis() - startTime
        assertThat(response.body?.string() == "{\"name\":{\"first\": \"test\",\"last\": \"test2\"}}").isTrue()
        assertThat(response.code == 200).isTrue()
        assertThat(response.message == "OK").isTrue()
        assertThat(response.headers.size == 2).isTrue()
        assertThat(response.header("header5", null) == "value5").isTrue()
        assertThat(response.header("header6", null) == "value6").isTrue()
        assertThat(diff in 200..210).isTrue()
    }

    @Test
    fun testWhenNoResponseIdMappedToARequest() {
        val request = Request.Builder()
            .url("https://example.com/payment/{paymentId}/get")
            .get()
            .build()

        val chain = mockk<Interceptor.Chain>()

        every {
            chain.request()
        } returns request

        val methodSlot = slot<String>()
        every {
            reMockStore.getAllRequestsByRequestMethod(capture(methodSlot))
        } answers {
            allRequests.filter { it.requestMethod == methodSlot.captured }
        }

        val requestIdSlot = slot<Long>()
        every {
            reMockStore.getRequestWithMockResponseByRequestIdBlocking(capture(requestIdSlot))
        } answers {
            allRequestWithResponse.firstOrNull { it.request.id == requestIdSlot.captured }
        }

        every {
            chain.proceed(any())
        } returns Response.Builder()
            .request(request)
            .code(200)
            .message("OK")
            .body("{\"response\":\"ok\"}".toResponseBody("application/json".toMediaTypeOrNull()))
            .protocol(Protocol.HTTP_1_1)
            .build()

        val response = interceptor.intercept(chain)
        assertThat(response.body?.string() == "{\"response\":\"ok\"}").isTrue()
        assertThat(response.code == 200).isTrue()
        assertThat(response.message == "OK").isTrue()
        assertThat(response.headers.size == 2).isFalse()
        assertThat(response.header("header1", null) == "value1").isFalse()
        assertThat(response.header("header2", null) == "value2").isFalse()
        verify(exactly = 1) { reMockStore.getRequestWithMockResponseByRequestIdBlocking(2) }
        verify(exactly = 1) { chain.proceed(request) }
    }

    @Test
    fun testWhenBestPossibleResponseNotFoundForTheRequest() {
        val request = Request.Builder()
            .url("https://example.com/order/{orderId}/get")
            .get()
            .build()

        val chain = mockk<Interceptor.Chain>()

        every {
            chain.request()
        } returns request

        val methodSlot = slot<String>()
        every {
            reMockStore.getAllRequestsByRequestMethod(capture(methodSlot))
        } answers {
            allRequests.filter { it.requestMethod == methodSlot.captured }
        }

        val requestIdSlot = slot<Long>()
        every {
            reMockStore.getRequestWithMockResponseByRequestIdBlocking(capture(requestIdSlot))
        } answers {
            allRequestWithResponse.firstOrNull { it.request.id == requestIdSlot.captured }
        }

        every {
            chain.proceed(any())
        } returns Response.Builder()
            .request(request)
            .code(200)
            .message("OK")
            .body("{\"response\":\"ok\"}".toResponseBody("application/json".toMediaTypeOrNull()))
            .protocol(Protocol.HTTP_1_1)
            .build()

        val response = interceptor.intercept(chain)
        assertThat(response.body?.string() == "{\"response\":\"ok\"}").isTrue()
        assertThat(response.code == 200).isTrue()
        assertThat(response.message == "OK").isTrue()
        assertThat(response.headers.size == 2).isFalse()
        assertThat(response.header("header1", null) == "value1").isFalse()
        assertThat(response.header("header2", null) == "value2").isFalse()
        verify(exactly = 1) { reMockStore.getRequestWithMockResponseByRequestIdBlocking(3) }
        verify(exactly = 1) { chain.proceed(request) }
    }

}