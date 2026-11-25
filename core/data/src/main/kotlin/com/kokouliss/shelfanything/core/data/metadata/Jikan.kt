package com.kokouliss.shelfanything.core.data.metadata

import com.kokouliss.shelfanything.core.domain.model.ItemDraft
import com.kokouliss.shelfanything.core.domain.model.Shelf

/** Anime, from Jikan (MyAnimeList's data). No key. */
internal object Jikan {
    const val SOURCE = "myanimelist"

    fun searchUrl(query: String): String = "https://api.jikan.moe/v4/anime?q=${query.encode()}&limit=20&sfw=true"

    fun parse(body: String): List<ItemDraft> = body.toJson()["data"].objects().mapNotNull { anime ->
        val title = anime["title_english"].string() ?: anime["title"].string() ?: return@mapNotNull null
        val id = anime["mal_id"].int() ?: return@mapNotNull null
        val studios = anime["studios"].objects().mapNotNull { it["name"].string() }
        ItemDraft(
            shelf = Shelf.ANIME,
            title = title,
            creator = studios.joinToString(", "),
            year = anime["year"].int() ?: anime["aired"]["from"].string().year(),
            coverUrl = anime["images"]["jpg"]["image_url"].string(),
            source = SOURCE,
            sourceId = id.toString(),
            description = anime["synopsis"].string().orEmpty(),
            details = details(
                "Japanese title" to anime["title_japanese"].string(),
                "Type" to anime["type"].string(),
                "Episodes" to anime["episodes"].int(),
                "Status" to anime["status"].string(),
                "Aired" to anime["aired"]["string"].string(),
                "Season" to listOfNotNull(anime["season"].string(), anime["year"].int()?.toString()),
                "Studios" to studios,
                "Genres" to anime["genres"].objects().mapNotNull { it["name"].string() },
                "Score" to anime["score"].double()?.let { "$it / 10" },
                "Age rating" to anime["rating"].string(),
                "MyAnimeList" to anime["url"].string(),
            ),
        )
    }
}
