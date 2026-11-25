package com.kokouliss.shelfanything.core.data.metadata

import com.kokouliss.shelfanything.core.domain.model.ItemDraft
import com.kokouliss.shelfanything.core.domain.model.Shelf
import java.net.URLEncoder

/** Books, comics, manga and audiobooks. No key; covers from covers.openlibrary.org. */
internal object OpenLibrary {
    const val SOURCE = "openlibrary"

    private const val FIELDS = "key,title,author_name,first_publish_year,isbn,cover_i,publisher,number_of_pages_median,language,subject,edition_count"

    fun searchUrl(query: String): String =
        "https://openlibrary.org/search.json?q=${query.encode()}&limit=20&fields=$FIELDS"

    fun barcodeUrl(isbn: String): String =
        "https://openlibrary.org/search.json?isbn=${isbn.encode()}&limit=5&fields=$FIELDS"

    fun parse(body: String, shelf: Shelf): List<ItemDraft> = body.toJson()["docs"].objects().mapNotNull { doc ->
        val title = doc["title"].string() ?: return@mapNotNull null
        val authors = doc["author_name"].strings()
        val isbns = doc["isbn"].strings()
        val coverId = doc["cover_i"].int()
        ItemDraft(
            shelf = shelf,
            title = title,
            creator = authors.take(3).joinToString(", "),
            year = doc["first_publish_year"].int(),
            coverUrl = coverId?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" },
            source = SOURCE,
            sourceId = doc["key"].string().orEmpty(),
            barcode = isbns.firstOrNull { it.length == 13 } ?: isbns.firstOrNull().orEmpty(),
            details = details(
                "Authors" to authors,
                "First published" to doc["first_publish_year"].int(),
                "Publishers" to doc["publisher"].strings().take(5),
                "Pages" to doc["number_of_pages_median"].int(),
                "Languages" to doc["language"].strings().take(5),
                "Subjects" to doc["subject"].strings().take(10),
                "Editions" to doc["edition_count"].int(),
                "ISBN" to isbns.take(3),
                "Open Library" to doc["key"].string()?.let { "https://openlibrary.org$it" },
            ),
        )
    }
}

internal fun String.encode(): String = URLEncoder.encode(this, "UTF-8")
