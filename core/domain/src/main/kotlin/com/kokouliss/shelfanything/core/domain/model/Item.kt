package com.kokouliss.shelfanything.core.domain.model

/** A thing on a shelf, with everything the metadata site said about it and what the person did with it. */
data class Item(
    val id: String,
    val shelf: Shelf,
    val title: String,
    /** Author, director, artist, developer or publisher: whoever a shelf names under the title. */
    val creator: String = "",
    val year: Int? = null,
    val coverUrl: String? = null,
    /** Which site the metadata came from, and its id there, so the item can be looked up again. Empty when typed by hand. */
    val source: String = "",
    val sourceId: String = "",
    val barcode: String = "",
    /** Every field the site gave, as it gave it, in the order it gave it. Shown in full on the item page. */
    val details: Map<String, String> = emptyMap(),
    val description: String = "",
    // The three flags are independent: a book can be owned and unread, or read and not owned.
    val own: Boolean = false,
    val want: Boolean = false,
    /** Read, watched, listened to or played. */
    val done: Boolean = false,
    val edition: Edition? = null,
    val movieFormat: MovieFormat? = null,
    /** For series and anime: Netflix, Disney+, a disc... Free text. */
    val watchedOn: String = "",
    val season: Int = 0,
    val episode: Int = 0,
    /** How many times a movie was watched again, after the first time. */
    val rewatches: Int = 0,
    val notes: String = "",
    val comments: List<Comment> = emptyList(),
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
)

data class Comment(val text: String, val at: Long)

/**
 * A search result from a metadata site, or the fields typed by hand, before it is put on a shelf.
 * The same fields as [Item] minus what the person adds.
 */
data class ItemDraft(
    val shelf: Shelf,
    val title: String,
    val creator: String = "",
    val year: Int? = null,
    val coverUrl: String? = null,
    val source: String = "",
    val sourceId: String = "",
    val barcode: String = "",
    val details: Map<String, String> = emptyMap(),
    val description: String = "",
)

/** The longest a title, a creator, a note or a comment may be. The security rules say the same. */
const val MAX_TEXT = 500
const val MAX_NOTE = 4000
