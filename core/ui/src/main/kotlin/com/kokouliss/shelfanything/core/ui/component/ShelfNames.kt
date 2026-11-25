package com.kokouliss.shelfanything.core.ui.component

import androidx.annotation.StringRes
import com.kokouliss.shelfanything.core.domain.model.Edition
import com.kokouliss.shelfanything.core.domain.model.MovieFormat
import com.kokouliss.shelfanything.core.domain.model.Shelf
import com.kokouliss.shelfanything.core.domain.model.ShelfGroup
import com.kokouliss.shelfanything.core.ui.R

/** The name of each shelf, in the app's language. */
@StringRes
fun Shelf.label(): Int = when (this) {
    Shelf.BOOKS -> R.string.core_ui_shelf_books
    Shelf.COMICS -> R.string.core_ui_shelf_comics
    Shelf.MANGA -> R.string.core_ui_shelf_manga
    Shelf.AUDIOBOOKS -> R.string.core_ui_shelf_audiobooks
    Shelf.MOVIES -> R.string.core_ui_shelf_movies
    Shelf.SERIES -> R.string.core_ui_shelf_series
    Shelf.ANIME -> R.string.core_ui_shelf_anime
    Shelf.CDS -> R.string.core_ui_shelf_cds
    Shelf.VINYL -> R.string.core_ui_shelf_vinyl
    Shelf.CASSETTES -> R.string.core_ui_shelf_cassettes
    Shelf.VIDEO_GAMES -> R.string.core_ui_shelf_video_games
    Shelf.BOARD_GAMES -> R.string.core_ui_shelf_board_games
}

@StringRes
fun ShelfGroup.label(): Int = when (this) {
    ShelfGroup.READ -> R.string.core_ui_group_read
    ShelfGroup.WATCH -> R.string.core_ui_group_watch
    ShelfGroup.LISTEN -> R.string.core_ui_group_listen
    ShelfGroup.PLAY -> R.string.core_ui_group_play
}

/** "Read", "Watched", "Listened" or "Played": the done flag in the words of the shelf's group. */
@StringRes
fun Shelf.doneLabel(): Int = when (group) {
    ShelfGroup.READ -> R.string.core_ui_done_read
    ShelfGroup.WATCH -> R.string.core_ui_done_watched
    ShelfGroup.LISTEN -> R.string.core_ui_done_listened
    ShelfGroup.PLAY -> R.string.core_ui_done_played
}

@StringRes
fun Edition.label(): Int = when (this) {
    Edition.PHYSICAL -> R.string.core_ui_edition_physical
    Edition.DIGITAL -> R.string.core_ui_edition_digital
}

@StringRes
fun MovieFormat.label(): Int = when (this) {
    MovieFormat.DVD -> R.string.core_ui_format_dvd
    MovieFormat.BLU_RAY -> R.string.core_ui_format_blu_ray
    MovieFormat.UHD -> R.string.core_ui_format_4k
}
