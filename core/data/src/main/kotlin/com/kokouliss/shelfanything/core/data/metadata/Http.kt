package com.kokouliss.shelfanything.core.data.metadata

import android.util.Log
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import okhttp3.OkHttpClient
import okhttp3.Request

/** The name the metadata sites see. MusicBrainz refuses requests without one that names the app and a contact. */
internal const val USER_AGENT = "ShelfAnything/0.1 (https://github.com/Kokouliss/Shelf-Anything)"

/** One GET at a time, as a string, or null when the site could not be reached or answered with an error. */
@Singleton
internal class Http @Inject constructor(private val client: OkHttpClient) {

    suspend fun get(url: String, headers: Map<String, String> = emptyMap()): String? = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).header("User-Agent", USER_AGENT).apply {
            headers.forEach { (name, value) -> header(name, value) }
        }.build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body.string()
                } else {
                    Log.w(TAG, "$url answered ${response.code}")
                    null
                }
            }
        } catch (e: IOException) {
            Log.w(TAG, "Could not reach $url", e)
            null
        }
    }

    private companion object {
        const val TAG = "ShelfAnythingHttp"
    }
}

/** Reads any JSON the sites send; unknown fields are kept as text for the details list. */
internal val json = Json { ignoreUnknownKeys = true; isLenient = true }

internal fun String.toJson(): JsonElement? = try {
    json.parseToJsonElement(this)
} catch (e: IllegalArgumentException) {
    null
}

internal val JsonElement?.obj: JsonObject? get() = (this as? JsonObject)
internal val JsonElement?.array: JsonArray? get() = (this as? JsonArray)
internal operator fun JsonElement?.get(key: String): JsonElement? = obj?.get(key)
internal fun JsonElement?.string(): String? = (this as? JsonPrimitive)?.contentOrNull?.takeIf { it != "null" }
internal fun JsonElement?.int(): Int? = (this as? JsonPrimitive)?.intOrNull
internal fun JsonElement?.double(): Double? = (this as? JsonPrimitive)?.doubleOrNull
internal fun JsonElement?.strings(): List<String> = array?.mapNotNull { it.string() }.orEmpty()
internal fun JsonElement?.objects(): List<JsonObject> = array?.mapNotNull { it.obj }.orEmpty()

/** The first four digits of a date like "2004-09-22", or a year on its own. */
internal fun String?.year(): Int? = this?.take(4)?.toIntOrNull()

/** The text of an HTML summary, for sites that send paragraphs with tags. */
internal fun String?.stripHtml(): String =
    this.orEmpty().replace(Regex("<[^>]+>"), "").replace("&amp;", "&").replace("&quot;", "\"").replace("&#39;", "'")
        .replace("&#10;", "\n").trim()

/** The details list keeps only what has a value, in the order given. */
internal fun details(vararg pairs: Pair<String, Any?>): Map<String, String> = buildMap {
    pairs.forEach { (key, value) ->
        val text = when (value) {
            null -> null
            is List<*> -> value.filterNotNull().joinToString(", ").takeIf { it.isNotBlank() }
            else -> value.toString().takeIf { it.isNotBlank() }
        }
        if (text != null) put(key, text)
    }
}
