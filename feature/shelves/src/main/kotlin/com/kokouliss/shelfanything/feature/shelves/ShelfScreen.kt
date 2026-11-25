package com.kokouliss.shelfanything.feature.shelves

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kokouliss.shelfanything.core.domain.model.Edition
import com.kokouliss.shelfanything.core.domain.model.Item
import com.kokouliss.shelfanything.core.domain.model.MAX_TEXT
import com.kokouliss.shelfanything.core.domain.model.Shelf
import com.kokouliss.shelfanything.core.ui.R as CoreR
import com.kokouliss.shelfanything.core.ui.component.AppIcon
import com.kokouliss.shelfanything.core.ui.component.AppIcons
import com.kokouliss.shelfanything.core.ui.component.BackButton
import com.kokouliss.shelfanything.core.ui.component.Cover
import com.kokouliss.shelfanything.core.ui.component.EmptyState
import com.kokouliss.shelfanything.core.ui.component.LoadingBox
import com.kokouliss.shelfanything.core.ui.component.Plank
import com.kokouliss.shelfanything.core.ui.component.doneLabel
import com.kokouliss.shelfanything.core.ui.component.label
import com.kokouliss.shelfanything.core.ui.component.pageWidth
import com.kokouliss.shelfanything.core.ui.theme.ShelfAnythingTheme

/** The flag filter above a shelf. */
private enum class Flag { ALL, OWN, WANT, DONE }

@Composable
internal fun ShelfRoute(
    shelf: Shelf,
    onBack: () -> Unit,
    onAddItem: () -> Unit,
    onOpenItem: (String) -> Unit,
    viewModel: ShelvesViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    ShelfScreen(shelf, items?.filter { it.shelf == shelf }, onBack, onAddItem, onOpenItem)
}

@Composable
internal fun ShelfScreen(
    shelf: Shelf,
    items: List<Item>?,
    onBack: () -> Unit,
    onAddItem: () -> Unit,
    onOpenItem: (String) -> Unit,
) {
    val name = stringResource(shelf.label())
    var query by rememberSaveable { mutableStateOf("") }
    var flag by rememberSaveable { mutableStateOf(Flag.ALL) }
    var edition by rememberSaveable { mutableStateOf<Edition?>(null) }
    Scaffold(
        modifier = Modifier.semantics { paneTitle = name },
        topBar = { TopAppBar(title = { Text(name) }, navigationIcon = { BackButton(onBack) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddItem,
                icon = { AppIcon(AppIcons.Add, null) },
                text = { Text(stringResource(R.string.feature_shelves_add)) },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).pageWidth()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it.take(MAX_TEXT) },
                label = { Text(stringResource(R.string.feature_shelves_search_shelf)) },
                singleLine = true,
                leadingIcon = { AppIcon(AppIcons.Search, null) },
                trailingIcon = if (query.isEmpty()) null else {
                    { IconButton(onClick = { query = "" }) { AppIcon(AppIcons.Close, stringResource(R.string.feature_shelves_clear)) } }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Flag.entries.forEach { f ->
                    val label = when (f) {
                        Flag.ALL -> R.string.feature_shelves_all
                        Flag.OWN -> CoreR.string.core_ui_own
                        Flag.WANT -> CoreR.string.core_ui_want
                        Flag.DONE -> shelf.doneLabel()
                    }
                    FilterChip(selected = flag == f, onClick = { flag = f }, label = { Text(stringResource(label)) })
                }
                if (shelf.hasEdition) {
                    Edition.entries.forEach { e ->
                        FilterChip(
                            selected = edition == e,
                            onClick = { edition = if (edition == e) null else e },
                            label = { Text(stringResource(e.label())) },
                        )
                    }
                }
            }
            if (items == null) {
                LoadingBox()
                return@Column
            }
            val shown = items.filter { item ->
                when (flag) {
                    Flag.ALL -> true
                    Flag.OWN -> item.own
                    Flag.WANT -> item.want
                    Flag.DONE -> item.done
                } &&
                    (edition == null || item.edition == edition) &&
                    (query.isBlank() || item.title.contains(query, ignoreCase = true) || item.creator.contains(query, ignoreCase = true))
            }
            if (shown.isEmpty()) {
                if (items.isEmpty()) {
                    EmptyState(AppIcons.Add, stringResource(R.string.feature_shelves_empty_title), stringResource(R.string.feature_shelves_empty_body))
                } else {
                    EmptyState(AppIcons.Search, stringResource(R.string.feature_shelves_no_match), stringResource(R.string.feature_shelves_no_match_body))
                }
            } else {
                BoxWithConstraints {
                    val columns = (maxWidth / 132.dp).toInt().coerceIn(3, 6)
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        items(shown.chunked(columns)) { row -> CoverRow(row, columns, onOpenItem) }
                    }
                }
            }
        }
    }
}

/** The covers of one row standing on a plank, with their titles under it. */
@Composable
private fun CoverRow(row: List<Item>, columns: Int, onOpenItem: (String) -> Unit) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
            row.forEach { item ->
                Cover(
                    item.coverUrl,
                    item.title,
                    Modifier.weight(1f).clickable(role = Role.Button, onClick = { onOpenItem(item.id) }),
                    contentDescription = item.title,
                )
            }
            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
        }
        Plank()
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            row.forEach { item ->
                Column(Modifier.weight(1f).padding(top = 6.dp)) {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        flagsText(item),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

@PreviewLightDark
@Composable
private fun ShelfScreenPreview() {
    ShelfAnythingTheme {
        ShelfScreen(
            shelf = Shelf.BOOKS,
            items = listOf(
                Item(id = "1", shelf = Shelf.BOOKS, title = "Dune", creator = "Frank Herbert", year = 1965, own = true, done = true),
                Item(id = "2", shelf = Shelf.BOOKS, title = "The Left Hand of Darkness", creator = "Ursula K. Le Guin", want = true),
            ),
            onBack = {},
            onAddItem = {},
            onOpenItem = {},
        )
    }
}
