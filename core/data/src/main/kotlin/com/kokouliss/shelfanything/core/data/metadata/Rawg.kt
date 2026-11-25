package com.kokouliss.shelfanything.core.data.metadata

import com.kokouliss.shelfanything.core.domain.model.ItemDraft
import com.kokouliss.shelfanything.core.domain.model.Shelf

/** Video games, from RAWG. Needs an API key (free tier; see the README). */
internal object Rawg {
    const val SOURCE = "rawg"

    fun searchUrl(query: String, key: String): String =
        "https://api.rawg.io/api/games?key=${key.encode()}&search=${query.encode()}&page_size=20"

    fun parse(body: String): List<ItemDraft> = body.toJson()["results"].objects().mapNotNull { game ->
        val name = game["name"].string() ?: return@mapNotNull null
        val id = game["id"].int() ?: return@mapNotNull null
        val platforms = game["platforms"].objects().mapNotNull { it["platform"]["name"].string() }
        ItemDraft(
            shelf = Shelf.VIDEO_GAMES,
            title = name,
            creator = platforms.take(3).joinToString(", "),
            year = game["released"].string().year(),
            coverUrl = game["background_image"].string(),
            source = SOURCE,
            sourceId = id.toString(),
            details = details(
                "Released" to game["released"].string(),
                "Platforms" to platforms,
                "Genres" to game["genres"].objects().mapNotNull { it["name"].string() },
                "Rating" to game["rating"].double()?.let { "$it / 5" },
                "Metacritic" to game["metacritic"].int(),
                "Playtime" to game["playtime"].int()?.takeIf { it > 0 }?.let { "$it h" },
                "Age rating" to game["esrb_rating"]["name"].string(),
                "RAWG" to game["slug"].string()?.let { "https://rawg.io/games/$it" },
            ),
        )
    }
}
