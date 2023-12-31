package dev.romio.remock.ui.viewmodel

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import dev.romio.remock.ReMockGraph
import dev.romio.remock.data.room.dto.MockResponseWithHeaders
import dev.romio.remock.data.store.ReMockStore
import dev.romio.remock.ui.nav.RouteNavigator
import dev.romio.remock.ui.screen.ResponseDetailsRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RequestDetailsViewModel(
    private val reMockStore: ReMockStore,
    private val routeNavigator: RouteNavigator,
    savedStateHandle: SavedStateHandle
): ViewModel(), RouteNavigator by routeNavigator {

    private val requestId = checkNotNull(savedStateHandle.get<String>("requestId")).toLong()

    private val _state = MutableStateFlow(RequestDetailsState())
    val state: StateFlow<RequestDetailsState>
        get() = _state

    init {
        viewModelScope.launch {
            val request = reMockStore.getRequestByRequestId(requestId) ?: return@launch
            reMockStore.getResponseFlowByRequestId(requestId).collect {
                _state.value = RequestDetailsState(
                    requestMethod = request.requestMethod,
                    requestUrl = request.url,
                    responseList = it
                )
            }
        }
    }

    fun navigateToResponseDetails(requestId: Long, responseId: Long?) {
        navigateToRoute(ResponseDetailsRoute.createRoute(requestId, responseId))
    }

    fun navigateToAddResponse() {
        navigateToRoute(ResponseDetailsRoute.createRoute(requestId, null))
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
                    return RequestDetailsViewModel(requestStore, routeNavigator, handle) as T
                }

            }
    }
}

data class RequestDetailsState(
    val requestMethod: String = "",
    val requestUrl: String = "",
    val responseList: List<MockResponseWithHeaders> = emptyList()
)