package com.kokouliss.shelfanything.core.data.metadata

import com.kokouliss.shelfanything.core.domain.model.ItemDraft
import com.kokouliss.shelfanything.core.domain.model.Shelf

/** Series, from TVmaze. No key. */
internal object Tvmaze {
    const val SOURCE = "tvmaze"

    fun searchUrl(query: String): String = "https://api.tvmaze.com/search/shows?q=${query.encode()}"

    fun parse(body: String): List<ItemDraft> = body.toJson().objects().mapNotNull { hit ->
        val show = hit["show"] ?: return@mapNotNull null
        val name = show["name"].string() ?: return@mapNotNull null
        val id = show["id"].int() ?: return@mapNotNull null
        val network = show["network"]["name"].string() ?: show["webChannel"]["name"].string()
        ItemDraft(
            shelf = Shelf.SERIES,
            title = name,
            creator = network.orEmpty(),
            year = show["premiered"].string().year(),
            coverUrl = show["image"]["medium"].string(),
            source = SOURCE,
            sourceId = id.toString(),
            description = show["summary"].string().stripHtml(),
            details = details(
                "Network" to network,
                "Status" to show["status"].string(),
                "Premiered" to show["premiered"].string(),
                "Ended" to show["ended"].string(),
                "Genres" to show["genres"].strings(),
                "Language" to show["language"].string(),
                "Episode length" to show["averageRuntime"].int()?.let { "$it min" },
                "Rating" to show["rating"]["average"].double()?.let { "$it / 10" },
                "IMDb" to show["externals"]["imdb"].string()?.let { "https://www.imdb.com/title/$it/" },
                "TVmaze" to show["url"].string(),
            ),
        )
    }
}
