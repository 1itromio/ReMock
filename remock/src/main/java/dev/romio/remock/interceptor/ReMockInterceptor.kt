package dev.romio.remock.interceptor

import android.content.Context
import dev.romio.remock.ReMockGraph
import dev.romio.remock.data.room.dto.MockResponseWithHeaders
import dev.romio.remock.data.room.entity.RequestEntity
import dev.romio.remock.data.store.ReMockStore
import dev.romio.remock.matcher.PathMatcher
import dev.romio.remock.matcher.ReMockPathMatcher
import dev.romio.remock.util.ReMockUtils
import dev.romio.remock.util.ReMockUtils.castValuesToPrimitive
import kotlinx.serialization.json.Json
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.net.URLDecoder


open class ReMockInterceptor(
    context: Context,
    private val pathMatcher: PathMatcher = ReMockPathMatcher(),
    private val requestStore: ReMockStore = ReMockGraph.reMockStore
): Interceptor {

    init {
        ReMockGraph.initialize(context.applicationContext)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requests = requestStore.getAllRequestsByRequestMethod(request.method)
        val requestUrl = request.url.host + request.url.encodedPath
        val decodedRequestUrl = URLDecoder.decode(requestUrl, "UTF-8")
        val matchingRequest = requests.find {
            pathMatcher.match(it.url, decodedRequestUrl)
        } ?: return chain.proceed(request)
        val requestWithMockResponse =
            requestStore.getRequestWithMockResponseByRequestIdBlocking(matchingRequest.id)
        if(requestWithMockResponse == null ||
            requestWithMockResponse.mockResponseList.isEmpty()) {
            return chain.proceed(request)
        }
        val requestCopy = request.newBuilder().build()
        val evaluationMap = createEvaluationMap(requestWithMockResponse.request, requestCopy)
        val chosenResponse = getBestPossibleResponseEntity(
            evaluationMap,
            requestWithMockResponse.mockResponseList
        ) ?: return chain.proceed(request)
        val transformedResponse = transformMockResponseToResponse(request, chosenResponse)
        delay(chosenResponse.mockResponse.responseDelay ?: 0)
        return transformedResponse
    }

    open fun createEvaluationMap(
        requestEntity: RequestEntity,
        request: Request
    ): Map<String, Any> {

        val requestUrl = request.url.host + request.url.encodedPath
        val decodedRequestUrl = URLDecoder.decode(requestUrl, "UTF-8")
        val headerMap = request.headers.toMap().castValuesToPrimitive()
        val queryMap = request.url.queryParameterNames.associateBy({
            it
        }, {
            request.url.queryParameter(it)
        }).castValuesToPrimitive()
        val paramMap = pathMatcher.extractUriTemplateVariables(
            requestEntity.url,
            decodedRequestUrl
        ).castValuesToPrimitive()
        val contentSubtype = request.body?.contentType()?.subtype
        val bodyMap = if(contentSubtype?.equals("json", ignoreCase = true) == true) {
            val buffer = Buffer()
            request.body?.writeTo(buffer)
            val bodyStringContent = buffer.readUtf8()
            ReMockUtils.toMapOrList(Json.parseToJsonElement(bodyStringContent))
                ?: emptyMap<String, Any>()
        } else emptyMap<String, Any>()
        return mapOf(
            "header" to headerMap,
            "query" to queryMap,
            "param" to paramMap,
            "body" to bodyMap
        )
    }

    open fun getBestPossibleResponseEntity(
        evaluationMap: Map<String, Any>,
        responseList: List<MockResponseWithHeaders>
    ): MockResponseWithHeaders? {
        return responseList.firstOrNull {
            val expression = it.mockResponse.whenExpression ?: return@firstOrNull false
            ReMockUtils.isExpressionEvaluationTrue(expression, evaluationMap)
        } ?: responseList.firstOrNull { it.mockResponse.whenExpression == null }
    }

    open fun transformMockResponseToResponse(
        request: Request,
        mockResponse: MockResponseWithHeaders
    ): Response {

        val headers = Headers.Builder().also { builder ->
            mockResponse.mockResponseHeaders.forEach {
                builder.add(it.headerKey, it.headerValue)
            }
        }.build()
        val responseBody = mockResponse.mockResponse
            .responseBody
            ?.toResponseBody(ReMockUtils.getMediaType(mockResponse.mockResponse.responseType))
        return Response.Builder()
            .request(request)
            .code(mockResponse.mockResponse.responseCode)
            .also { builder ->
                mockResponse.mockResponse.message?.let {
                    builder.message(it)
                }
            }
            .headers(headers)
            .body(responseBody)
            .build()
    }

    open fun delay(delay: Long) {
        if (delay > 0) {
            try {
                Thread.sleep(delay)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

}