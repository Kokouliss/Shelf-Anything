package com.kokouliss.shelfanything.feature.shelves

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kokouliss.shelfanything.core.domain.model.Item
import com.kokouliss.shelfanything.core.domain.model.Shelf
import com.kokouliss.shelfanything.core.domain.repository.ItemRepository
import com.kokouliss.shelfanything.core.domain.repository.ShelfRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The whole collection and the shelves that are switched on: the home, shelf and first-launch screens share it. */
@HiltViewModel
internal class ShelvesViewModel @Inject constructor(
    itemRepository: ItemRepository,
    private val shelfRepository: ShelfRepository,
) : ViewModel() {

    /** Null until the first answer from the store arrives. */
    val items: StateFlow<List<Item>?> = itemRepository.items.map<List<Item>, List<Item>?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val enabledShelves: StateFlow<Set<Shelf>> = shelfRepository.enabledShelves.map { it ?: Shelf.DEFAULT }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Shelf.DEFAULT)

    fun setEnabledShelves(shelves: Set<Shelf>) {
        viewModelScope.launch { shelfRepository.setEnabledShelves(shelves) }
    }
}
