package com.kokouliss.shelfanything.feature.shelves

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kokouliss.shelfanything.core.domain.model.Item
import com.kokouliss.shelfanything.core.domain.model.Shelf
import com.kokouliss.shelfanything.core.domain.model.ShelfGroup
import com.kokouliss.shelfanything.core.ui.component.AppIcon
import com.kokouliss.shelfanything.core.ui.component.AppIcons
import com.kokouliss.shelfanything.core.ui.component.Cover
import com.kokouliss.shelfanything.core.ui.component.LoadingBox
import com.kokouliss.shelfanything.core.ui.component.Plank
import com.kokouliss.shelfanything.core.ui.component.label
import com.kokouliss.shelfanything.core.ui.component.pageWidth
import com.kokouliss.shelfanything.core.ui.theme.ShelfAnythingTheme

@Composable
internal fun HomeRoute(
    onOpenShelf: (Shelf) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: ShelvesViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val shelves by viewModel.enabledShelves.collectAsStateWithLifecycle()
    HomeScreen(items = items, shelves = shelves, onOpenShelf = onOpenShelf, onOpenSettings = onOpenSettings)
}

@Composable
internal fun HomeScreen(
    items: List<Item>?,
    shelves: Set<Shelf>,
    onOpenShelf: (Shelf) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val title = stringResource(R.string.feature_shelves_title)
    Scaffold(
        modifier = Modifier.semantics { paneTitle = title },
        topBar = {
            TopAppBar(
                title = { Text(title) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        AppIcon(AppIcons.Settings, stringResource(R.string.feature_shelves_settings))
                    }
                },
            )
        },
    ) { padding ->
        if (items == null) {
            LoadingBox(Modifier.padding(padding))
            return@Scaffold
        }
        val byShelf = items.groupBy { it.shelf }
        LazyColumn(Modifier.padding(padding).pageWidth(), contentPadding = PaddingValues(bottom = 24.dp)) {
            ShelfGroup.entries.forEach { group ->
                val onGroup = Shelf.entries.filter { it.group == group && it in shelves }
                if (onGroup.isEmpty()) return@forEach
                item(key = group.name) {
                    Text(
                        stringResource(group.label()),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp).semantics { heading() },
                    )
                }
                items(onGroup, key = { it.id }) { shelf ->
                    ShelfRow(shelf, byShelf[shelf].orEmpty(), onClick = { onOpenShelf(shelf) })
                }
            }
        }
    }
}

/** The name of the shelf, how much is on it, and its newest covers standing on a plank. */
@Composable
private fun ShelfRow(shelf: Shelf, items: List<Item>, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClickLabel = stringResource(R.string.feature_shelves_open), onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(shelf.label()), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Text(
                pluralStringResource(R.plurals.feature_shelves_item_count, items.size, items.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        // An empty shelf shows the plank alone.
        Row(
            Modifier.padding(top = 8.dp).height(96.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            items.take(6).forEach { Cover(it.coverUrl, it.title, Modifier.fillMaxHeight()) }
        }
        Plank()
    }
}

@PreviewLightDark
@Composable
private fun HomeScreenPreview() {
    ShelfAnythingTheme {
        HomeScreen(
            items = listOf(Item(id = "1", shelf = Shelf.BOOKS, title = "Dune", creator = "Frank Herbert", year = 1965, own = true)),
            shelves = Shelf.DEFAULT,
            onOpenShelf = {},
            onOpenSettings = {},
        )
    }
}
