package com.kokouliss.shelfanything.core.data.metadata

import com.kokouliss.shelfanything.core.domain.model.ItemDraft
import com.kokouliss.shelfanything.core.domain.model.Shelf
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.xml.sax.SAXException

/**
 * Board games, from BoardGameGeek's XML API. Since 2025 it needs a registered application and a
 * bearer token (see the README), and the search gives only ids and names, so the details come from
 * a second call for the first few matches.
 */
internal object Bgg {
    const val SOURCE = "bgg"

    fun searchUrl(query: String): String = "https://boardgamegeek.com/xmlapi2/search?query=${query.encode()}&type=boardgame"

    fun thingUrl(ids: List<String>): String = "https://boardgamegeek.com/xmlapi2/thing?id=${ids.joinToString(",")}&stats=1"

    fun headers(token: String) = mapOf("Authorization" to "Bearer $token")

    /** The ids of the matches, best first as BGG orders them. */
    fun parseSearch(body: String): List<String> =
        body.toXml()?.elements("item")?.mapNotNull { it.getAttribute("id").takeIf(String::isNotBlank) }.orEmpty()

    fun parseThings(body: String): List<ItemDraft> = body.toXml()?.elements("item").orEmpty().mapNotNull { item ->
        val id = item.getAttribute("id").takeIf(String::isNotBlank) ?: return@mapNotNull null
        val names = item.elements("name")
        val title = (names.firstOrNull { it.getAttribute("type") == "primary" } ?: names.firstOrNull())
            ?.getAttribute("value") ?: return@mapNotNull null
        fun links(type: String) = item.elements("link").filter { it.getAttribute("type") == type }.map { it.getAttribute("value") }
        fun value(tag: String) = item.elements(tag).firstOrNull()?.getAttribute("value")?.takeIf(String::isNotBlank)
        val designers = links("boardgamedesigner")
        val players = listOfNotNull(value("minplayers"), value("maxplayers")).distinct().joinToString(" to ")
        val rating = item.elements("statistics").firstOrNull()?.elements("ratings")?.firstOrNull()
            ?.elements("average")?.firstOrNull()?.getAttribute("value")?.toDoubleOrNull()
        ItemDraft(
            shelf = Shelf.BOARD_GAMES,
            title = title,
            creator = designers.take(3).joinToString(", "),
            year = value("yearpublished")?.toIntOrNull(),
            coverUrl = item.elements("image").firstOrNull()?.textContent?.trim(),
            source = SOURCE,
            sourceId = id,
            description = item.elements("description").firstOrNull()?.textContent.stripHtml(),
            details = details(
                "Designers" to designers,
                "Publishers" to links("boardgamepublisher").take(3),
                "Players" to players,
                "Playing time" to value("playingtime")?.let { "$it min" },
                "Age" to value("minage")?.let { "$it+" },
                "Categories" to links("boardgamecategory"),
                "Mechanisms" to links("boardgamemechanic").take(6),
                "Rating" to rating?.let { "%.1f / 10".format(it) },
                "BoardGameGeek" to "https://boardgamegeek.com/boardgame/$id",
            ),
        )
    }

    private fun String.toXml(): Element? = try {
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(byteInputStream()).documentElement
    } catch (e: SAXException) {
        null
    }

    /** The direct children named [tag]. */
    private fun Element.elements(tag: String): List<Element> {
        val nodes = childNodes
        return (0 until nodes.length).mapNotNull { nodes.item(it) as? Element }.filter { it.tagName == tag }
    }
}
