package com.kokouliss.shelfanything.core.domain.export

import com.kokouliss.shelfanything.core.domain.model.Item
import com.kokouliss.shelfanything.core.domain.model.Shelf

/**
 * The collection as text: a CSV file for a spreadsheet, and a readable list for sharing.
 * [shelfName] gives the name of a shelf in the app's language.
 */
object LibraryText {

    private val COLUMNS = listOf(
        "shelf", "title", "creator", "year", "own", "want", "done", "edition", "format", "watched_on",
        "season", "episode", "rewatches", "barcode", "source", "source_id", "cover", "notes", "added",
    )

    fun csv(items: List<Item>): String = buildString {
        appendLine(COLUMNS.joinToString(","))
        items.forEach { item ->
            appendLine(
                listOf(
                    item.shelf.id, item.title, item.creator, item.year?.toString().orEmpty(),
                    item.own.toString(), item.want.toString(), item.done.toString(),
                    item.edition?.id.orEmpty(), item.movieFormat?.id.orEmpty(), item.watchedOn,
                    item.season.toString(), item.episode.toString(), item.rewatches.toString(),
                    item.barcode, item.source, item.sourceId, item.coverUrl.orEmpty(), item.notes,
                    item.createdAt.toString(),
                ).joinToString(",", transform = ::csvCell),
            )
        }
    }

    /** A cell is quoted when it holds a comma, a quote or a line break; a quote inside is doubled. */
    fun csvCell(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }

    /** One line per item under the name of its shelf, for a message or a note. */
    fun share(items: List<Item>, shelfName: (Shelf) -> String): String = buildString {
        items.groupBy { it.shelf }.toSortedMap(compareBy { it.ordinal }).forEach { (shelf, onShelf) ->
            if (isNotEmpty()) appendLine()
            appendLine(shelfName(shelf))
            onShelf.sortedBy { it.title.lowercase() }.forEach { appendLine("- " + line(it)) }
        }
    }.trimEnd()

    /** "Title (2001), Creator". The year and the creator only when known. */
    fun line(item: Item): String = buildString {
        append(item.title)
        if (item.year != null) append(" (").append(item.year).append(")")
        if (item.creator.isNotBlank()) append(", ").append(item.creator)
    }
}
