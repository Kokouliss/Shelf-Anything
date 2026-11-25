package com.kokouliss.shelfanything.feature.shelves

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.kokouliss.shelfanything.core.domain.model.ItemDraft
import com.kokouliss.shelfanything.core.domain.model.Shelf
import com.kokouliss.shelfanything.core.domain.repository.ItemRepository
import com.kokouliss.shelfanything.core.domain.repository.MetadataRepository
import com.kokouliss.shelfanything.core.domain.repository.SearchResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

internal sealed interface SearchState {
    data object Idle : SearchState
    data object Searching : SearchState

    /** [barcode] is the scanned code the results came from, kept for an item added by hand. */
    data class Results(val drafts: List<ItemDraft>, val barcode: String? = null) : SearchState
    data object NeedsKey : SearchState
    data object Failed : SearchState
}

@HiltViewModel(assistedFactory = AddItemViewModel.Factory::class)
internal class AddItemViewModel @AssistedInject constructor(
    @Assisted val shelf: Shelf,
    private val metadata: MetadataRepository,
    private val items: ItemRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(shelf: Shelf): AddItemViewModel
    }

    private val _state = MutableStateFlow<SearchState>(SearchState.Idle)
    val state: StateFlow<SearchState> = _state.asStateFlow()

    /** The id of the item just added, so the screen can open it. */
    private val _added = MutableStateFlow<String?>(null)
    val added: StateFlow<String?> = _added.asStateFlow()

    private var job: Job? = null

    fun search(query: String) = run { metadata.search(shelf, query) }

    /** Opens Google's scanner sheet; a scanned code is looked up at once. */
    fun scan(context: Context) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E)
            .enableAutoZoom()
            .build()
        job?.cancel()
        _state.value = SearchState.Searching
        job = viewModelScope.launch {
            val code = try {
                GmsBarcodeScanning.getClient(context, options).startScan().await().rawValue
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Closing the sheet, or a phone without the scanner module, both land here.
                Log.w(TAG, "Scan did not finish", e)
                null
            }
            if (code.isNullOrBlank()) _state.value = SearchState.Idle else run(code) { metadata.lookupBarcode(shelf, code) }
        }
    }

    fun add(draft: ItemDraft, own: Boolean, want: Boolean, done: Boolean) {
        viewModelScope.launch { _added.value = items.add(draft, own, want, done) }
    }

    private fun run(barcode: String? = null, fetch: suspend () -> SearchResult) {
        job?.cancel()
        _state.value = SearchState.Searching
        job = viewModelScope.launch {
            _state.value = when (val result = fetch()) {
                is SearchResult.Found -> SearchState.Results(result.drafts, barcode)
                SearchResult.NeedsKey -> SearchState.NeedsKey
                SearchResult.Failed -> SearchState.Failed
            }
        }
    }

    private companion object {
        const val TAG = "ShelfAnythingScan"
    }
}
