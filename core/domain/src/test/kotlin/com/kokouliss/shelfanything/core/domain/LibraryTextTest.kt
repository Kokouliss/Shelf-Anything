package com.kokouliss.shelfanything.core.domain

import com.kokouliss.shelfanything.core.domain.export.LibraryText
import com.kokouliss.shelfanything.core.domain.model.Item
import com.kokouliss.shelfanything.core.domain.model.Shelf
import kotlin.test.Test
import kotlin.test.assertEquals

class LibraryTextTest {

    private val dune = Item("1", Shelf.BOOKS, "Dune", "Frank Herbert", 1965, own = true)
    private val alien = Item("2", Shelf.MOVIES, "Alien, the \"director's cut\"", "Ridley Scott", 1979)

    @Test
    fun csvQuotesCommasAndQuotes() {
        val lines = LibraryText.csv(listOf(dune, alien)).lines()
        assertEquals("shelf,title,creator,year,own,want,done,edition,format,watched_on,season,episode,rewatches,barcode,source,source_id,cover,notes,added", lines[0])
        assertEquals("books,Dune,Frank Herbert,1965,true,false,false,,,,0,0,0,,,,,,0", lines[1])
        assertEquals("movies,\"Alien, the \"\"director's cut\"\"\",Ridley Scott,1979,false,false,false,,,,0,0,0,,,,,,0", lines[2])
    }

    @Test
    fun shareGroupsByShelfInShelfOrder() {
        val text = LibraryText.share(listOf(alien, dune)) { it.name.lowercase() }
        assertEquals("books\n- Dune (1965), Frank Herbert\n\nmovies\n- Alien, the \"director's cut\" (1979), Ridley Scott", text)
    }

    @Test
    fun lineLeavesOutWhatIsUnknown() {
        assertEquals("Dune", LibraryText.line(Item("3", Shelf.BOOKS, "Dune")))
    }
}
