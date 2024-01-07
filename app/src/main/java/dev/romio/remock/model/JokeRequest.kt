package dev.romio.remock.model


import com.google.gson.annotations.SerializedName

data class JokeRequest(
    @SerializedName("category")
    val category: String = "Misc",
    @SerializedName("flags")
    val flags: Flags = Flags(),
    @SerializedName("formatVersion")
    val formatVersion: Int = 3,
    @SerializedName("joke")
    val joke: String = "Test",
    @SerializedName("lang")
    val lang: String = "en",
    @SerializedName("type")
    val type: String = "single"
)

data class Flags(
    @SerializedName("explicit")
    val explicit: Boolean = false,
    @SerializedName("nsfw")
    val nsfw: Boolean = false,
    @SerializedName("political")
    val political: Boolean = false,
    @SerializedName("racist")
    val racist: Boolean = false,
    @SerializedName("religious")
    val religious: Boolean = false,
    @SerializedName("sexist")
    val sexist: Boolean = false
)