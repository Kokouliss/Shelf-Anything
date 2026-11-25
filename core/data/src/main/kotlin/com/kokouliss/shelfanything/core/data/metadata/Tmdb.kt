package com.kokouliss.shelfanything.core.data.metadata

import com.kokouliss.shelfanything.core.domain.model.ItemDraft
import com.kokouliss.shelfanything.core.domain.model.Shelf

/** Movies, from The Movie Database. Needs an API key (free for non-commercial use; see the README). */
internal object Tmdb {
    const val SOURCE = "tmdb"

    fun searchUrl(query: String, key: String): String =
        "https://api.themoviedb.org/3/search/movie?api_key=${key.encode()}&query=${query.encode()}&include_adult=false"

    fun parse(body: String): List<ItemDraft> = body.toJson()["results"].objects().mapNotNull { movie ->
        val title = movie["title"].string() ?: return@mapNotNull null
        val id = movie["id"].int() ?: return@mapNotNull null
        val original = movie["original_title"].string()
        ItemDraft(
            shelf = Shelf.MOVIES,
            title = title,
            year = movie["release_date"].string().year(),
            coverUrl = movie["poster_path"].string()?.let { "https://image.tmdb.org/t/p/w342$it" },
            source = SOURCE,
            sourceId = id.toString(),
            description = movie["overview"].string().orEmpty(),
            details = details(
                "Original title" to original?.takeIf { it != title },
                "Released" to movie["release_date"].string(),
                "Language" to movie["original_language"].string(),
                "Rating" to movie["vote_average"].double()?.let { "%.1f / 10".format(it) },
                "Votes" to movie["vote_count"].int(),
                "TMDB" to "https://www.themoviedb.org/movie/$id",
            ),
        )
    }
}
