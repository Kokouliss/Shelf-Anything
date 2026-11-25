package com.kokouliss.shelfanything.core.data.metadata

import com.kokouliss.shelfanything.core.domain.model.Shelf
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Each site's answer, cut down to the fields the app reads, in the shape the site sent on 2026-11-25. */
class ParsersTest {

    @Test
    fun openLibraryGivesTitleAuthorYearCoverAndIsbn() {
        val body = """{"docs":[{"author_name":["Frank Herbert"],"cover_i":11481354,"edition_count":155,"first_publish_year":1965,
            "isbn":["0340960191","9780340960196"],"key":"/works/OL893415W","title":"Dune","publisher":["Ace","Hodder"],
            "number_of_pages_median":604,"language":["eng","ger"],"subject":["Science fiction","Deserts"]},{"key":"/works/x"}]}"""
        val books = OpenLibrary.parse(body, Shelf.BOOKS)
        assertEquals(1, books.size)
        val dune = books[0]
        assertEquals("Dune", dune.title)
        assertEquals("Frank Herbert", dune.creator)
        assertEquals(1965, dune.year)
        assertEquals("https://covers.openlibrary.org/b/id/11481354-M.jpg", dune.coverUrl)
        assertEquals("9780340960196", dune.barcode)
        assertEquals("/works/OL893415W", dune.sourceId)
        assertEquals("604", dune.details["Pages"])
        assertEquals("Science fiction, Deserts", dune.details["Subjects"])
    }

    @Test
    fun tvmazeGivesNetworkYearAndPlainSummary() {
        val body = """[{"score":0.9,"show":{"id":123,"url":"https://www.tvmaze.com/shows/123/lost","name":"Lost","genres":["Drama"],
            "status":"Ended","averageRuntime":60,"premiered":"2004-09-22","ended":"2010-05-23","rating":{"average":8.2},
            "network":{"name":"ABC"},"webChannel":null,"externals":{"imdb":"tt0411008"},
            "image":{"medium":"https://static.tvmaze.com/m.jpg"},"summary":"<p>Out of the <b>blackness</b>.</p>"}}]"""
        val lost = Tvmaze.parse(body).single()
        assertEquals("Lost", lost.title)
        assertEquals("ABC", lost.creator)
        assertEquals(2004, lost.year)
        assertEquals("Out of the blackness.", lost.description)
        assertEquals("https://www.imdb.com/title/tt0411008/", lost.details["IMDb"])
        assertNull(lost.details["Web channel"])
    }

    @Test
    fun musicBrainzGivesArtistsLabelsAndCoverArtLink() {
        val body = """{"releases":[{"id":"abc-123","title":"Abbey Road","date":"1969-09-26","country":"GB","barcode":"5099969943515",
            "status":"Official","packaging":"Jewel Case","track-count":17,"artist-credit":[{"name":"The Beatles"}],
            "label-info":[{"catalog-number":"PCS 7088","label":{"name":"Apple Records"}}],"media":[{"format":"12\" Vinyl"}]}]}"""
        val album = MusicBrainz.parse(body, Shelf.VINYL).single()
        assertEquals("The Beatles", album.creator)
        assertEquals(1969, album.year)
        assertEquals("https://coverartarchive.org/release/abc-123/front-250", album.coverUrl)
        assertEquals("5099969943515", album.barcode)
        assertEquals("Apple Records", album.details["Labels"])
    }

    @Test
    fun musicBrainzSearchFiltersByTheShelfFormat() {
        assertTrue(MusicBrainz.searchUrl("abbey road", Shelf.CASSETTES).contains("Cassette"))
    }

    @Test
    fun tmdbSkipsResultsWithoutATitle() {
        val body = """{"results":[{"id":348,"title":"Alien","original_title":"Alien","release_date":"1979-05-25",
            "overview":"Nostromo.","poster_path":"/p.jpg","vote_average":8.157,"vote_count":15000,"original_language":"en"},{"id":1}]}"""
        val alien = Tmdb.parse(body).single()
        assertEquals("https://image.tmdb.org/t/p/w342/p.jpg", alien.coverUrl)
        assertEquals(1979, alien.year)
    }

    @Test
    fun bggThingsGiveDesignersPlayersAndRating() {
        val body = """<?xml version="1.0" encoding="utf-8"?><items><item type="boardgame" id="13">
            <thumbnail>https://cf.geekdo-images.com/t.jpg</thumbnail><image>https://cf.geekdo-images.com/i.jpg</image>
            <name type="alternate" value="Catan" /><name type="primary" value="CATAN" />
            <description>Settle &amp; trade.</description><yearpublished value="1995" />
            <minplayers value="3" /><maxplayers value="4" /><playingtime value="120" /><minage value="10" />
            <link type="boardgamedesigner" id="11" value="Klaus Teuber" /><link type="boardgamepublisher" id="1" value="KOSMOS" />
            <statistics page="1"><ratings><average value="7.09" /></ratings></statistics></item></items>"""
        val catan = Bgg.parseThings(body).single()
        assertEquals("CATAN", catan.title)
        assertEquals("Klaus Teuber", catan.creator)
        assertEquals(1995, catan.year)
        assertEquals("3 to 4", catan.details["Players"])
        assertEquals("7.1 / 10", catan.details["Rating"])
        assertEquals("Settle & trade.", catan.description)
        assertEquals(listOf("13"), Bgg.parseSearch("""<items><item id="13"><name value="Catan"/></item></items>"""))
    }

    @Test
    fun upcTitleLosesTheDiscAndEditionWords() {
        assertEquals("Alien", UpcItemDb.workTitle("Alien (Blu-ray) [Region B] - Steelbook Edition"))
        assertEquals("The Legend of Zelda", UpcItemDb.workTitle("The Legend of Zelda - Nintendo Switch"))
    }
}
