package dev.romio.remock

import com.google.gson.JsonObject
import dev.romio.remock.model.JokeRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface Service {

    @GET("joke/Any")
    suspend fun getAnyJoke(
        @Query("blacklistFlags") blacklistFlags: String? = null,
        @Query("type") type: String? = null,
        @Query("contains") contains: String? = null,
        @Query("amount") amount: Int? = null,
        @HeaderMap headers: Map<String, String> = emptyMap()
    ): Response<JsonObject>

    @GET("joke/{category}")
    suspend fun getJokeByCategory(
        @Path("category") category: String,
        @Query("blacklistFlags") blacklistFlags: String? = null,
        @Query("type") type: String? = null,
        @Query("contains") contains: String? = null,
        @Query("amount") amount: Int? = null,
        @HeaderMap headers: Map<String, String> = emptyMap()
    ): Response<JsonObject>

    @POST("submit")
    suspend fun submit(
        @Body jokeRequest: JokeRequest,
        @HeaderMap headers: Map<String, String>
    ): Response<JsonObject>
}