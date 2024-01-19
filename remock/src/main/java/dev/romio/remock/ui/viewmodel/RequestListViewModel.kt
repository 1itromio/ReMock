package dev.romio.remock.ui.viewmodel

import android.webkit.URLUtil
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.romio.remock.ReMockGraph
import dev.romio.remock.data.room.entity.RequestEntity
import dev.romio.remock.data.store.ReMockStore
import dev.romio.remock.ui.nav.RouteNavigator
import dev.romio.remock.ui.screen.RequestDetailsRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.URL

internal class RequestListViewModel(
    private val requestStore: ReMockStore = ReMockGraph.reMockStore,
    private val routeNavigator: RouteNavigator = ReMockGraph.routeNavigator
): ViewModel(), RouteNavigator by routeNavigator {

    private val _state = MutableStateFlow(RequestListState())

    var inValidUrlError by mutableStateOf("")
        private set

    val state: StateFlow<RequestListState>
        get() = _state

    init {
        viewModelScope.launch {
            requestStore.getAllRequests().collect {
                _state.value = RequestListState(it)
            }
        }
    }

    // TODO: Validate for the request Url and method
    fun addNewRequest(
        selectedRequestMethod: String,
        requestUrl: String
    ) {
        viewModelScope.launch {
            val requestEntity = RequestEntity(
                url = requestUrl.trim(),
                urlHash = requestUrl.trim().hashCode(),
                requestMethod = selectedRequestMethod.trim()
            )
            val requestId = requestStore.addNewRequest(requestEntity)
            navigateToRequestDetails(requestId)
        }
    }

    fun isValidRequest(
        selectedRequestMethod: String,
        requestUrl: String
    ): Boolean {
        val isValidUrl = URLUtil.isValidUrl(requestUrl)
        inValidUrlError = if(!isValidUrl) {
            "Invalid Url"
        } else {
            ""
        }
        return isValidUrl
    }

    fun navigateToRequestDetails(requestId: Long) {
        navigateToRoute(RequestDetailsRoute.createRoute(requestId))
    }

    fun removeRequest(request: RequestEntity) {
        viewModelScope.launch {
            requestStore.removeRequest(request)
        }
    }

}

internal data class RequestListState(
    val requestList: List<RequestEntity> = emptyList()
)