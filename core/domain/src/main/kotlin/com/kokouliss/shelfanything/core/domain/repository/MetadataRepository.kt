package com.kokouliss.shelfanything.core.domain.repository

import com.kokouliss.shelfanything.core.domain.model.ItemDraft
import com.kokouliss.shelfanything.core.domain.model.Shelf

/** The sites that know about books, films, music and games. One client per site lives in core:data. */
interface MetadataRepository {
    /** Up to about twenty matches for [query] on [shelf]. */
    suspend fun search(shelf: Shelf, query: String): SearchResult

    /** What a scanned barcode (EAN or UPC) is, on [shelf]. */
    suspend fun lookupBarcode(shelf: Shelf, barcode: String): SearchResult
}

sealed interface SearchResult {
    data class Found(val drafts: List<ItemDraft>) : SearchResult

    /** The build has no key for the site this shelf uses. The README says how to add one. */
    data object NeedsKey : SearchResult

    /** The site could not be reached or answered with an error. */
    data object Failed : SearchResult
}
