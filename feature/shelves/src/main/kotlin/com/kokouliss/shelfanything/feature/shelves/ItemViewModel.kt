package com.kokouliss.shelfanything.feature.shelves

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kokouliss.shelfanything.core.domain.model.Comment
import com.kokouliss.shelfanything.core.domain.model.Item
import com.kokouliss.shelfanything.core.domain.repository.ItemRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** [item] is null before the store answered ([loaded] false) and after the item was deleted ([loaded] true). */
internal data class ItemState(val item: Item?, val loaded: Boolean)

@HiltViewModel(assistedFactory = ItemViewModel.Factory::class)
internal class ItemViewModel @AssistedInject constructor(
    @Assisted private val id: String,
    private val items: ItemRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(id: String): ItemViewModel
    }

    val state: StateFlow<ItemState> = items.item(id).map { ItemState(it, loaded = true) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ItemState(null, loaded = false))

    fun update(item: Item) {
        viewModelScope.launch { items.update(item) }
    }

    fun delete() {
        viewModelScope.launch { items.delete(id) }
    }

    fun addComment(item: Item, text: String) =
        update(item.copy(comments = item.comments + Comment(text.trim(), System.currentTimeMillis())))

    fun deleteComment(item: Item, comment: Comment) = update(item.copy(comments = item.comments - comment))
}
