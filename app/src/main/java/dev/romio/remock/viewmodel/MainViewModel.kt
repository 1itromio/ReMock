package dev.romio.remock.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.romio.remock.Service
import dev.romio.remock.di.Graph
import dev.romio.remock.model.Flags
import dev.romio.remock.model.JokeRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val service: Service = Graph.service): ViewModel() {

    private val _state = MutableStateFlow(State(emptyList()))

    val state: StateFlow<State>
        get() = _state

    fun startTest() {
        viewModelScope.launch {
            getJokes()
        }
    }

    private suspend fun getJokes() {
        val headers = mapOf("mockRequest" to "true")
        val apiCalls = listOf(
            suspend {
                service.getAnyJoke(headers = headers)
            },
            suspend {
                service.getAnyJoke(blacklistFlags = "nsfw", headers = headers)
            },
            suspend {
                service.getAnyJoke(type = "bad", headers = headers)
            },
            suspend {
                service.getJokeByCategory(category = "Programming", headers = headers)
            },
            suspend {
                service.getJokeByCategory(category = "Dark", headers = headers)
            },
            suspend {
                val request = JokeRequest()
                service.submit(request, headers = headers)
            },
            suspend {
                val request = JokeRequest(category = "Dark", flags = Flags(political = true))
                service.submit(request, headers = headers)
            },
            suspend {
                val request = JokeRequest(category = "Dark")
                service.submit(request, headers = headers)
            }
        )
        val jokesList = mutableListOf<JokeModel>()
        jokesList.add(JokeModel("Initialising Requests"))
        delay(200)
        apiCalls.forEach { apiCall ->
            Log.d("API CALL", "APi call goiing")
            val response = apiCall()
            val rawResponse = response.raw()
            val isMockedResponse = rawResponse.headers("X-Mock-Response").let {
                it.isNotEmpty() && it[0] == "true"
            }
            jokesList.add(JokeModel(
                heading = rawResponse.request.url.toString(),
                subheading = if(isMockedResponse) "Mocked response" else "Actual Response",
                headers = rawResponse.headers.toString(),
                message = "${rawResponse.code} ${rawResponse.message}",
                body = response.body()?.toString() ?: response.errorBody()?.string()
            ))
            _state.value = State(ArrayList(jokesList))
            delay(200)
        }
    }
}

data class State(
    val jokes: List<JokeModel>
)

data class JokeModel(
    val heading: String,
    val subheading: String? = null,
    val headers: String? = null,
    val message: String? = null,
    val body: String? = null
)