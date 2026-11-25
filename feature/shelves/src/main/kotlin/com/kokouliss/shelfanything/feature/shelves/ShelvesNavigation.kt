package com.kokouliss.shelfanything.feature.shelves

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.kokouliss.shelfanything.core.domain.model.Shelf
import kotlinx.serialization.Serializable

/** The shelves that are switched on, grouped by what you do with them. */
@Serializable
data object HomeKey : NavKey

/** One shelf with everything on it. */
@Serializable
data class ShelfKey(val shelf: Shelf) : NavKey

/** Search a site, scan a barcode or type an item for [shelf]. */
@Serializable
data class AddItemKey(val shelf: Shelf) : NavKey

@Serializable
data class ItemKey(val id: String) : NavKey

fun EntryProviderScope<NavKey>.shelvesEntries(
    onBack: () -> Unit,
    onOpenShelf: (Shelf) -> Unit,
    onAddItem: (Shelf) -> Unit,
    onOpenItem: (String) -> Unit,
    /** A new item is on its shelf; its page takes the place of the add screen. */
    onItemAdded: (String) -> Unit,
    onOpenSettings: () -> Unit,
) {
    entry<HomeKey> { HomeRoute(onOpenShelf = onOpenShelf, onOpenSettings = onOpenSettings) }
    entry<ShelfKey> { key ->
        ShelfRoute(shelf = key.shelf, onBack = onBack, onAddItem = { onAddItem(key.shelf) }, onOpenItem = onOpenItem)
    }
    entry<AddItemKey> { key ->
        AddItemRoute(
            viewModel = hiltViewModel<AddItemViewModel, AddItemViewModel.Factory> { it.create(key.shelf) },
            onBack = onBack,
            onAdded = onItemAdded,
        )
    }
    entry<ItemKey> { key ->
        ItemRoute(viewModel = hiltViewModel<ItemViewModel, ItemViewModel.Factory> { it.create(key.id) }, onBack = onBack)
    }
}
