package dev.romio.remock.ui.viewmodel

import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import dev.romio.remock.ReMockGraph
import dev.romio.remock.data.room.dto.MockResponseWithHeaders
import dev.romio.remock.data.room.entity.MockResponseEntity
import dev.romio.remock.data.store.ReMockStore
import dev.romio.remock.domain.model.ResponseContentType
import dev.romio.remock.ui.nav.RouteNavigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Protocol

class ResponseDetailsViewModel(
    private val reMockStore: ReMockStore,
    private val routeNavigator: RouteNavigator,
    private val savedStateHandle: SavedStateHandle,
    private val json: Json = Json {
        isLenient = true
        prettyPrint = true
        ignoreUnknownKeys = true
    }
): ViewModel(), RouteNavigator by routeNavigator {

    private val requestId: Long = checkNotNull(savedStateHandle.get<String>("requestId")).toLong()
    private var responseId: Long? = savedStateHandle.get<String>("responseId")?.toLong()

    var state by mutableStateOf(ResponseDetailsState())
        private set

    val formState = ResponseDetailsFormState()

    init {
        viewModelScope.launch {
            refreshState()
        }
    }

    private suspend fun refreshState() {
        state = state.copy(isAddResponseFlow = responseId == null)
        val request = reMockStore.getRequestByRequestId(requestId) ?: return
        state = state.copy(
            requestMethod = request.requestMethod,
            requestUrl = request.url
        )
        val finalResponseId = responseId ?: return
        val responseWithHeaders =
            reMockStore.getMockResponseWithHeaders(
                requestId,
                finalResponseId
            ) ?: return
        setResponseInForm(responseWithHeaders)
    }

    private suspend fun setResponseInForm(responseWithHeaders: MockResponseWithHeaders) {
        val response = responseWithHeaders.mockResponse
        formState.responseCode = response.responseCode.toString()
        formState.responseMessage = response.message ?: ""
        formState.whenExpression = response.whenExpression ?: ""
        formState.responseType = response.responseType.name
        formState.responseBody = response.responseBody ?: ""
        formState.responseDelay = response.responseDelay?.toString() ?: "0"
        formState.responseHeaders = if(responseWithHeaders.mockResponseHeaders.isNotEmpty()) {
            responseWithHeaders.mockResponseHeaders.joinToString(separator = "\n") {
                "${it.headerKey}:${it.headerValue}"
            }
        } else ""

        if(response.responseType != ResponseContentType.JSON) {
            return
        }

        val responseBody = response.responseBody ?: return

        val prettyBody = withContext(Dispatchers.Default) {
            runCatching {
                val bodyParseResult = json.parseToJsonElement(responseBody)
                json.encodeToString(bodyParseResult)
            }.getOrNull()
        } ?: return

        formState.responseBody = prettyBody
    }

    fun onSaveResponse() {
        viewModelScope.launch {
            val prettyBody = if(formState.responseType == ResponseContentType.JSON.name) {
                withContext(Dispatchers.Default) {
                    runCatching {
                        val bodyParseResult = json.parseToJsonElement(formState.responseBody)
                        json.encodeToString(bodyParseResult)
                    }.getOrNull()
                } ?: formState.responseBody
            } else formState.responseBody

            val responseEntity = MockResponseEntity(
                requestId = requestId,
                responseType = ResponseContentType.valueOf(formState.responseType),
                responseCode = formState.responseCode.toInt(),
                message = formState.responseMessage.ifBlank { null },
                whenExpression = formState.whenExpression.ifBlank { null },
                responseBody = prettyBody.ifBlank { null },
                protocol = Protocol.get(formState.protocol),
                responseDelay = formState.responseDelay.ifBlank { null }?.toLong(),
                id = responseId ?: 0L
            )
            responseId = reMockStore.saveResponse(responseEntity)
            savedStateHandle["responseId"] = responseId?.toString()
            refreshState()
        }
    }

    fun onDeleteResponse() {
        this@ResponseDetailsViewModel.responseId?.let { responseId ->
            viewModelScope.launch {
                reMockStore.deleteResponseById(responseId)
                navigateUp()
            }
        }
    }


    companion object {

        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            requestStore: ReMockStore = ReMockGraph.reMockStore,
            routeNavigator: RouteNavigator = ReMockGraph.routeNavigator,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null,
        ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return ResponseDetailsViewModel(requestStore, routeNavigator, handle) as T
                }

            }
    }

}

data class ResponseDetailsState(
    val isAddResponseFlow: Boolean = false,
    val requestMethod: String = "",
    val requestUrl: String = ""
)

class ResponseDetailsFormState {
    var responseCode by mutableStateOf("200")
    var responseMessage by mutableStateOf("OK")
    var whenExpression by mutableStateOf("")
    var responseType by mutableStateOf("JSON")
    var responseBody by mutableStateOf("")
    var protocol by mutableStateOf(Protocol.HTTP_1_1.toString())
    var responseDelay by mutableStateOf("0")
    var responseHeaders by mutableStateOf("")
}