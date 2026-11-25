package com.kokouliss.shelfanything.core.data.metadata

import com.kokouliss.shelfanything.core.domain.model.ItemDraft
import com.kokouliss.shelfanything.core.domain.model.Shelf

/** CDs, vinyl and cassettes, from MusicBrainz, with covers from the Cover Art Archive. No key, one request a second. */
internal object MusicBrainz {
    const val SOURCE = "musicbrainz"

    private fun format(shelf: Shelf) = when (shelf) {
        Shelf.VINYL -> "Vinyl"
        Shelf.CASSETTES -> "Cassette"
        else -> "CD"
    }

    fun searchUrl(query: String, shelf: Shelf): String {
        // Lucene: the words anywhere in the release, and only releases in this shelf's format.
        val lucene = "${query.replace("\"", "")} AND format:${format(shelf)}"
        return "https://musicbrainz.org/ws/2/release/?query=${lucene.encode()}&fmt=json&limit=20"
    }

    fun barcodeUrl(barcode: String): String =
        "https://musicbrainz.org/ws/2/release/?query=${"barcode:$barcode".encode()}&fmt=json&limit=10"

    fun parse(body: String, shelf: Shelf): List<ItemDraft> = body.toJson()["releases"].objects().mapNotNull { release ->
        val title = release["title"].string() ?: return@mapNotNull null
        val id = release["id"].string() ?: return@mapNotNull null
        val artists = release["artist-credit"].objects().mapNotNull { it["name"].string() }
        val labels = release["label-info"].objects().mapNotNull { it["label"]["name"].string() }.distinct()
        val media = release["media"].objects()
        ItemDraft(
            shelf = shelf,
            title = title,
            creator = artists.joinToString(", "),
            year = release["date"].string().year(),
            coverUrl = "https://coverartarchive.org/release/$id/front-250",
            source = SOURCE,
            sourceId = id,
            barcode = release["barcode"].string().orEmpty(),
            details = details(
                "Artists" to artists,
                "Released" to release["date"].string(),
                "Country" to release["country"].string(),
                "Labels" to labels,
                "Catalogue number" to release["label-info"].objects().mapNotNull { it["catalog-number"].string() }.distinct(),
                "Format" to media.mapNotNull { it["format"].string() }.distinct(),
                "Discs" to media.size.takeIf { it > 1 },
                "Tracks" to release["track-count"].int(),
                "Status" to release["status"].string(),
                "Packaging" to release["packaging"].string(),
                "Note" to release["disambiguation"].string(),
                "MusicBrainz" to "https://musicbrainz.org/release/$id",
            ),
        )
    }
}
