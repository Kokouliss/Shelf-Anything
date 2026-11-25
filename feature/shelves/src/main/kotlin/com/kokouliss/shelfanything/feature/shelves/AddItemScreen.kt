package com.kokouliss.shelfanything.feature.shelves

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kokouliss.shelfanything.core.domain.model.ItemDraft
import com.kokouliss.shelfanything.core.domain.model.MAX_TEXT
import com.kokouliss.shelfanything.core.domain.model.Shelf
import com.kokouliss.shelfanything.core.ui.R as CoreR
import com.kokouliss.shelfanything.core.ui.component.AppIcon
import com.kokouliss.shelfanything.core.ui.component.AppIcons
import com.kokouliss.shelfanything.core.ui.component.BackButton
import com.kokouliss.shelfanything.core.ui.component.Cover
import com.kokouliss.shelfanything.core.ui.component.LoadingBox
import com.kokouliss.shelfanything.core.ui.component.doneLabel
import com.kokouliss.shelfanything.core.ui.component.label
import com.kokouliss.shelfanything.core.ui.component.pageWidth
import com.kokouliss.shelfanything.core.ui.theme.ShelfAnythingTheme

/** The source written on items typed in by hand. */
private const val MANUAL_SOURCE = "manual"

@Composable
internal fun AddItemRoute(viewModel: AddItemViewModel, onBack: () -> Unit, onAdded: (String) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val added by viewModel.added.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(added) { added?.let(onAdded) }
    AddItemScreen(
        shelf = viewModel.shelf,
        state = state,
        onSearch = viewModel::search,
        onScan = { viewModel.scan(context) },
        onAdd = viewModel::add,
        onBack = onBack,
    )
}

@Composable
internal fun AddItemScreen(
    shelf: Shelf,
    state: SearchState,
    onSearch: (String) -> Unit,
    onScan: () -> Unit,
    onAdd: (draft: ItemDraft, own: Boolean, want: Boolean, done: Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val shelfName = stringResource(shelf.label())
    val title = stringResource(R.string.feature_shelves_add_to, shelfName)
    var query by rememberSaveable { mutableStateOf("") }
    // The draft being added, and whether its fields can be typed over (true for one made by hand).
    var adding by remember { mutableStateOf<Pair<ItemDraft, Boolean>?>(null) }
    val byHand = {
        val barcode = (state as? SearchState.Results)?.barcode.orEmpty()
        adding = ItemDraft(shelf = shelf, title = query.trim(), source = MANUAL_SOURCE, barcode = barcode) to true
    }
    Scaffold(
        modifier = Modifier.semantics { paneTitle = title },
        topBar = { TopAppBar(title = { Text(title) }, navigationIcon = { BackButton(onBack) }) },
    ) { padding ->
        Column(Modifier.padding(padding).pageWidth()) {
            Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it.take(MAX_TEXT) },
                    label = { Text(stringResource(R.string.feature_shelves_search_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onScan) { AppIcon(AppIcons.Barcode, stringResource(R.string.feature_shelves_scan)) }
            }
            when (state) {
                SearchState.Idle -> Note(stringResource(R.string.feature_shelves_search_intro), onByHand = byHand)
                SearchState.Searching -> LoadingBox()
                SearchState.NeedsKey -> Note(stringResource(R.string.feature_shelves_needs_key, shelfName), onByHand = byHand)
                SearchState.Failed -> Note(stringResource(R.string.feature_shelves_search_failed), onByHand = byHand, isError = true)
                is SearchState.Results -> if (state.drafts.isEmpty()) {
                    Note(
                        if (state.barcode == null) stringResource(R.string.feature_shelves_no_results)
                        else stringResource(R.string.feature_shelves_barcode_unknown, state.barcode),
                        onByHand = byHand,
                    )
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                        items(state.drafts) { draft ->
                            ListItem(
                                leadingContent = { Cover(draft.coverUrl, draft.title, Modifier.height(72.dp)) },
                                headlineContent = { Text(draft.title) },
                                supportingContent = { Text(subtitle(draft)) },
                                modifier = Modifier.clickable(role = Role.Button, onClick = { adding = draft to false }),
                            )
                        }
                        item { ByHandButton(byHand) }
                    }
                }
            }
        }
    }
    adding?.let { (draft, editable) ->
        AddDialog(
            draft = draft,
            editable = editable,
            onAdd = { d, own, want, done ->
                adding = null
                onAdd(d, own, want, done)
            },
            onDismiss = { adding = null },
        )
    }
}

/** "Creator · 1999", whichever is known. */
private fun subtitle(draft: ItemDraft): String =
    listOfNotNull(draft.creator.takeIf { it.isNotBlank() }, draft.year?.toString()).joinToString(" · ")

@Composable
private fun Note(text: String, onByHand: () -> Unit, isError: Boolean = false) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        ByHandButton(onByHand)
    }
}

@Composable
private fun ByHandButton(onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.padding(horizontal = 16.dp)) {
        AppIcon(AppIcons.Edit, null)
        Text(stringResource(R.string.feature_shelves_add_by_hand), modifier = Modifier.padding(start = 8.dp))
    }
}

/** Which flags the new item gets; with [editable], the title, creator and year too. */
@Composable
private fun AddDialog(
    draft: ItemDraft,
    editable: Boolean,
    onAdd: (ItemDraft, own: Boolean, want: Boolean, done: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var title by rememberSaveable { mutableStateOf(draft.title) }
    var creator by rememberSaveable { mutableStateOf(draft.creator) }
    var year by rememberSaveable { mutableStateOf(draft.year?.toString().orEmpty()) }
    var own by rememberSaveable { mutableStateOf(true) }
    var want by rememberSaveable { mutableStateOf(false) }
    var done by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editable) stringResource(R.string.feature_shelves_add_by_hand) else draft.title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (editable) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it.take(MAX_TEXT) },
                        label = { Text(stringResource(R.string.feature_shelves_item_title)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = creator,
                        onValueChange = { creator = it.take(MAX_TEXT) },
                        label = { Text(stringResource(R.string.feature_shelves_creator)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it.filter(Char::isDigit).take(4) },
                        label = { Text(stringResource(R.string.feature_shelves_year)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else if (subtitle(draft).isNotBlank()) {
                    Text(subtitle(draft), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FlagRow(stringResource(CoreR.string.core_ui_own), own) { own = it }
                FlagRow(stringResource(CoreR.string.core_ui_want), want) { want = it }
                FlagRow(stringResource(draft.shelf.doneLabel()), done) { done = it }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && (own || want || done),
                onClick = { onAdd(draft.copy(title = title.trim(), creator = creator.trim(), year = year.toIntOrNull()), own, want, done) },
            ) { Text(stringResource(R.string.feature_shelves_add)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(CoreR.string.core_ui_cancel)) } },
    )
}

@PreviewLightDark
@Composable
private fun AddItemScreenPreview() {
    ShelfAnythingTheme {
        AddItemScreen(
            shelf = Shelf.MOVIES,
            state = SearchState.Results(listOf(ItemDraft(shelf = Shelf.MOVIES, title = "Alien", creator = "Ridley Scott", year = 1979))),
            onSearch = {},
            onScan = {},
            onAdd = { _, _, _, _ -> },
            onBack = {},
        )
    }
}
