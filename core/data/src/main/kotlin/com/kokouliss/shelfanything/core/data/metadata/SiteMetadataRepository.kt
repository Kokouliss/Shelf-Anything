package com.kokouliss.shelfanything.core.data.metadata

import com.kokouliss.shelfanything.core.data.BuildConfig
import com.kokouliss.shelfanything.core.domain.model.ItemDraft
import com.kokouliss.shelfanything.core.domain.model.Shelf
import com.kokouliss.shelfanything.core.domain.model.ShelfGroup
import com.kokouliss.shelfanything.core.domain.repository.MetadataRepository
import com.kokouliss.shelfanything.core.domain.repository.SearchResult
import javax.inject.Inject
import javax.inject.Singleton

/** Routes each shelf to its site. The keys come from the build (see core/data/build.gradle.kts). */
@Singleton
internal class SiteMetadataRepository @Inject constructor(
    private val http: Http,
) : MetadataRepository {

    private val tmdbKey = BuildConfig.TMDB_API_KEY
    private val rawgKey = BuildConfig.RAWG_API_KEY
    private val bggToken = BuildConfig.BGG_API_TOKEN

    override suspend fun search(shelf: Shelf, query: String): SearchResult {
        val q = query.trim()
        if (q.isEmpty()) return SearchResult.Found(emptyList())
        return when (shelf) {
            Shelf.BOOKS, Shelf.COMICS, Shelf.MANGA, Shelf.AUDIOBOOKS ->
                fetch(OpenLibrary.searchUrl(q)) { OpenLibrary.parse(it, shelf) }
            Shelf.MOVIES -> withKey(tmdbKey) { fetch(Tmdb.searchUrl(q, it)) { body -> Tmdb.parse(body) } }
            Shelf.SERIES -> fetch(Tvmaze.searchUrl(q)) { Tvmaze.parse(it) }
            Shelf.ANIME -> fetch(Jikan.searchUrl(q)) { Jikan.parse(it) }
            Shelf.CDS, Shelf.VINYL, Shelf.CASSETTES -> fetch(MusicBrainz.searchUrl(q, shelf)) { MusicBrainz.parse(it, shelf) }
            Shelf.VIDEO_GAMES -> withKey(rawgKey) { fetch(Rawg.searchUrl(q, it)) { body -> Rawg.parse(body) } }
            Shelf.BOARD_GAMES -> withKey(bggToken) { searchBgg(q, it) }
        }
    }

    override suspend fun lookupBarcode(shelf: Shelf, barcode: String): SearchResult {
        val code = barcode.filter(Char::isDigit)
        if (code.isEmpty()) return SearchResult.Found(emptyList())
        return when (shelf.group) {
            ShelfGroup.READ -> fetch(OpenLibrary.barcodeUrl(code)) { OpenLibrary.parse(it, shelf) }
            ShelfGroup.LISTEN -> fetch(MusicBrainz.barcodeUrl(code)) { MusicBrainz.parse(it, shelf) }
            // Series and anime have no barcodes to speak of; a disc box set is looked up like a movie's.
            ShelfGroup.WATCH, ShelfGroup.PLAY -> {
                val body = http.get(UpcItemDb.lookupUrl(code)) ?: return SearchResult.Failed
                val title = UpcItemDb.parseTitle(body)?.let(UpcItemDb::workTitle)?.takeIf { it.isNotBlank() }
                    ?: return SearchResult.Found(emptyList())
                when (val found = search(shelf, title)) {
                    // The barcode belongs to the scanned edition, so the first match keeps it.
                    is SearchResult.Found -> SearchResult.Found(
                        found.drafts.mapIndexed { i, d -> if (i == 0) d.copy(barcode = code) else d },
                    )
                    else -> found
                }
            }
        }
    }

    private suspend fun searchBgg(query: String, token: String): SearchResult {
        val headers = Bgg.headers(token)
        val ids = http.get(Bgg.searchUrl(query), headers)?.let(Bgg::parseSearch) ?: return SearchResult.Failed
        if (ids.isEmpty()) return SearchResult.Found(emptyList())
        return fetch(Bgg.thingUrl(ids.take(12)), headers) { Bgg.parseThings(it) }
    }

    private suspend fun fetch(url: String, headers: Map<String, String> = emptyMap(), parse: (String) -> List<ItemDraft>): SearchResult {
        val body = http.get(url, headers) ?: return SearchResult.Failed
        return SearchResult.Found(parse(body))
    }

    private inline fun withKey(key: String, block: (String) -> SearchResult): SearchResult =
        if (key.isBlank()) SearchResult.NeedsKey else block(key)
}
