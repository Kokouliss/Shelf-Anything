package com.kokouliss.shelfanything.core.domain.model

/** What you do with the things on a group of shelves. The home screen is split by it. */
enum class ShelfGroup { READ, WATCH, LISTEN, PLAY }

/**
 * One kind of thing you collect. The [id] is stored in Firestore, so it never changes once
 * released; the names shown on screen live in the resources of core:ui.
 */
enum class Shelf(val id: String, val group: ShelfGroup) {
    BOOKS("books", ShelfGroup.READ),
    COMICS("comics", ShelfGroup.READ),
    MANGA("manga", ShelfGroup.READ),
    AUDIOBOOKS("audiobooks", ShelfGroup.READ),
    MOVIES("movies", ShelfGroup.WATCH),
    SERIES("series", ShelfGroup.WATCH),
    ANIME("anime", ShelfGroup.WATCH),
    CDS("cds", ShelfGroup.LISTEN),
    VINYL("vinyl", ShelfGroup.LISTEN),
    CASSETTES("cassettes", ShelfGroup.LISTEN),
    VIDEO_GAMES("video_games", ShelfGroup.PLAY),
    BOARD_GAMES("board_games", ShelfGroup.PLAY),
    ;

    /** True for a shelf whose items come in a physical or a digital edition, which the shelf filters by. */
    val hasEdition: Boolean get() = this != BOARD_GAMES

    /** Series and anime keep where they are watched and how far along the person is. */
    val tracksEpisodes: Boolean get() = this == SERIES || this == ANIME

    companion object {
        fun fromId(id: String): Shelf? = entries.firstOrNull { it.id == id }

        /** The shelves a new account starts with, before the person picks their own. */
        val DEFAULT: Set<Shelf> = setOf(BOOKS, MOVIES, SERIES, VINYL, VIDEO_GAMES, BOARD_GAMES)
    }
}

/** Physical or digital, the filter inside every shelf. [MovieFormat] refines physical for movies. */
enum class Edition(val id: String) {
    PHYSICAL("physical"),
    DIGITAL("digital"),
    ;

    companion object {
        fun fromId(id: String?): Edition? = entries.firstOrNull { it.id == id }
    }
}

/** The disc a movie is on. Digital movies have no disc, so the value is null then. */
enum class MovieFormat(val id: String) {
    DVD("dvd"),
    BLU_RAY("blu_ray"),
    UHD("4k"),
    ;

    companion object {
        fun fromId(id: String?): MovieFormat? = entries.firstOrNull { it.id == id }
    }
}
