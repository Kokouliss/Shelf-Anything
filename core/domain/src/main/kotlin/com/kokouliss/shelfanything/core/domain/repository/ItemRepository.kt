package com.kokouliss.shelfanything.core.domain.repository

import com.kokouliss.shelfanything.core.domain.model.Item
import com.kokouliss.shelfanything.core.domain.model.ItemDraft
import com.kokouliss.shelfanything.core.domain.model.Shelf
import kotlinx.coroutines.flow.Flow

/** The signed-in person's collection. Lives in the cloud, with a copy on the phone, so it works offline. */
interface ItemRepository {
    /** Everything on every shelf, newest first. Updates live. */
    val items: Flow<List<Item>>

    fun item(id: String): Flow<Item?>

    /** Puts [draft] on its shelf with the three flags as given, and returns the new item's id. */
    suspend fun add(draft: ItemDraft, own: Boolean, want: Boolean, done: Boolean): String

    suspend fun update(item: Item)

    suspend fun delete(id: String)
}

/** Which shelves the person switched on. Picked at first launch, changed in Settings. */
interface ShelfRepository {
    /** Null until the person picked, so the first launch can ask. */
    val enabledShelves: Flow<Set<Shelf>?>

    suspend fun setEnabledShelves(shelves: Set<Shelf>)
}
