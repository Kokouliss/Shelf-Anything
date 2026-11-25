package com.kokouliss.shelfanything.feature.shelves

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kokouliss.shelfanything.core.domain.export.LibraryText
import com.kokouliss.shelfanything.core.domain.model.Comment
import com.kokouliss.shelfanything.core.domain.model.Edition
import com.kokouliss.shelfanything.core.domain.model.Item
import com.kokouliss.shelfanything.core.domain.model.MAX_NOTE
import com.kokouliss.shelfanything.core.domain.model.MAX_TEXT
import com.kokouliss.shelfanything.core.domain.model.MovieFormat
import com.kokouliss.shelfanything.core.domain.model.Shelf
import com.kokouliss.shelfanything.core.domain.model.ShelfGroup
import com.kokouliss.shelfanything.core.ui.R as CoreR
import com.kokouliss.shelfanything.core.ui.component.AppIcon
import com.kokouliss.shelfanything.core.ui.component.AppIcons
import com.kokouliss.shelfanything.core.ui.component.BackButton
import com.kokouliss.shelfanything.core.ui.component.ConfirmDialog
import com.kokouliss.shelfanything.core.ui.component.Cover
import com.kokouliss.shelfanything.core.ui.component.LoadingBox
import com.kokouliss.shelfanything.core.ui.component.TextInputDialog
import com.kokouliss.shelfanything.core.ui.component.destructiveMenuItemColors
import com.kokouliss.shelfanything.core.ui.component.doneLabel
import com.kokouliss.shelfanything.core.ui.component.label
import com.kokouliss.shelfanything.core.ui.component.pageWidth
import com.kokouliss.shelfanything.core.ui.theme.ShelfAnythingTheme
import java.util.Date

/** Seasons, episodes and rewatches stop here; nobody counts higher by hand. */
private const val MAX_COUNT = 9999

@Composable
internal fun ItemRoute(viewModel: ItemViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val item = state.item
    when {
        !state.loaded -> LoadingBox()
        // Deleted here or on another phone: nothing is left to show.
        item == null -> LaunchedEffect(Unit) { onBack() }
        else -> {
            val shelfName = stringResource(item.shelf.label())
            val flags = flagsText(item)
            val shareTitle = stringResource(R.string.feature_shelves_share)
            ItemScreen(
                item = item,
                onChange = viewModel::update,
                onShare = { shareItem(context, item, shelfName, flags, shareTitle) },
                onDelete = {
                    viewModel.delete()
                    onBack()
                },
                onAddComment = { viewModel.addComment(item, it) },
                onDeleteComment = { viewModel.deleteComment(item, it) },
                onBack = onBack,
            )
        }
    }
}

private fun shareItem(context: Context, item: Item, shelfName: String, flags: String, title: String) {
    val text = buildString {
        appendLine(LibraryText.line(item))
        appendLine(shelfName)
        if (flags.isNotBlank()) appendLine(flags)
        item.details.values.firstOrNull { it.startsWith("http") }?.let { appendLine(it) }
    }.trim()
    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
    context.startActivity(Intent.createChooser(send, title))
}

@Composable
internal fun ItemScreen(
    item: Item,
    onChange: (Item) -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onAddComment: (String) -> Unit,
    onDeleteComment: (Comment) -> Unit,
    onBack: () -> Unit,
) {
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var editingWatchedOn by rememberSaveable { mutableStateOf(false) }
    var editingNotes by rememberSaveable { mutableStateOf(false) }
    var addingComment by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.semantics { paneTitle = item.title },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { BackButton(onBack) },
                actions = {
                    IconButton(onClick = onShare) { AppIcon(AppIcons.Share, stringResource(R.string.feature_shelves_share)) }
                    IconButton(onClick = { menuOpen = true }) { AppIcon(AppIcons.MoreVert, stringResource(R.string.feature_shelves_more_actions)) }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.feature_shelves_delete_item)) },
                            leadingIcon = { AppIcon(AppIcons.Delete, null) },
                            colors = destructiveMenuItemColors(),
                            onClick = {
                                menuOpen = false
                                confirmDelete = true
                            },
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).pageWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Cover(item.coverUrl, item.title, Modifier.width(120.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
                    if (item.creator.isNotBlank()) Text(item.creator, style = MaterialTheme.typography.bodyLarge)
                    if (item.year != null) {
                        Text(item.year.toString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(stringResource(item.shelf.label()), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }

            Section(stringResource(R.string.feature_shelves_status)) {
                FlagRow(stringResource(CoreR.string.core_ui_own), item.own) { onChange(item.copy(own = it)) }
                FlagRow(stringResource(CoreR.string.core_ui_want), item.want) { onChange(item.copy(want = it)) }
                FlagRow(stringResource(item.shelf.doneLabel()), item.done) { onChange(item.copy(done = it)) }
                if (item.own && item.shelf.hasEdition) {
                    ChoiceRow(
                        label = stringResource(R.string.feature_shelves_edition),
                        options = Edition.entries,
                        selected = item.edition,
                        name = { stringResource(it.label()) },
                        onSelect = { onChange(item.copy(edition = it)) },
                    )
                }
                if (item.own && item.shelf == Shelf.MOVIES && item.edition == Edition.PHYSICAL) {
                    ChoiceRow(
                        label = stringResource(R.string.feature_shelves_format),
                        options = MovieFormat.entries,
                        selected = item.movieFormat,
                        name = { stringResource(it.label()) },
                        onSelect = { onChange(item.copy(movieFormat = it)) },
                    )
                }
                if (item.shelf.group == ShelfGroup.WATCH) {
                    EditableRow(
                        label = stringResource(R.string.feature_shelves_watched_on),
                        value = item.watchedOn,
                        hint = stringResource(R.string.feature_shelves_watched_on_hint),
                        onClick = { editingWatchedOn = true },
                    )
                    if (item.shelf.tracksEpisodes) {
                        Counter(stringResource(R.string.feature_shelves_season), item.season) { onChange(item.copy(season = it)) }
                        Counter(stringResource(R.string.feature_shelves_episode), item.episode) { onChange(item.copy(episode = it)) }
                    }
                    Counter(stringResource(R.string.feature_shelves_rewatches), item.rewatches) { onChange(item.copy(rewatches = it)) }
                }
            }

            Section(stringResource(R.string.feature_shelves_notes)) {
                EditableRow(
                    label = stringResource(R.string.feature_shelves_notes),
                    value = item.notes,
                    hint = stringResource(R.string.feature_shelves_notes_hint),
                    onClick = { editingNotes = true },
                )
            }

            if (item.description.isNotBlank()) {
                Section(stringResource(R.string.feature_shelves_about)) {
                    Text(item.description, style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (item.details.isNotEmpty()) {
                Section(stringResource(R.string.feature_shelves_details)) {
                    val uriHandler = LocalUriHandler.current
                    item.details.forEach { (key, value) ->
                        val isLink = value.startsWith("http")
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .then(if (isLink) Modifier.clickable(role = Role.Button) { uriHandler.openUri(value) } else Modifier)
                                .padding(vertical = 6.dp),
                        ) {
                            Text(
                                key,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.widthIn(min = 96.dp, max = 140.dp).padding(end = 12.dp),
                            )
                            Text(
                                value,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isLink) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            Section(stringResource(R.string.feature_shelves_comments)) {
                val context = LocalContext.current
                val dateFormat = DateFormat.getMediumDateFormat(context)
                item.comments.forEach { comment ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(comment.text, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                dateFormat.format(Date(comment.at)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { onDeleteComment(comment) }) {
                            AppIcon(AppIcons.Delete, stringResource(R.string.feature_shelves_delete_comment))
                        }
                    }
                }
                TextButton(onClick = { addingComment = true }) {
                    AppIcon(AppIcons.Add, null)
                    Text(stringResource(R.string.feature_shelves_add_comment), modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }

    if (editingWatchedOn) {
        TextInputDialog(
            title = stringResource(R.string.feature_shelves_watched_on),
            label = stringResource(R.string.feature_shelves_watched_on_hint),
            confirmLabel = stringResource(CoreR.string.core_ui_save),
            onConfirm = {
                editingWatchedOn = false
                onChange(item.copy(watchedOn = it))
            },
            onDismiss = { editingWatchedOn = false },
            initialValue = item.watchedOn,
            maxLength = MAX_TEXT,
        )
    }
    if (editingNotes) {
        TextInputDialog(
            title = stringResource(R.string.feature_shelves_notes),
            label = stringResource(R.string.feature_shelves_notes_hint),
            confirmLabel = stringResource(CoreR.string.core_ui_save),
            onConfirm = {
                editingNotes = false
                onChange(item.copy(notes = it))
            },
            onDismiss = { editingNotes = false },
            initialValue = item.notes,
            maxLength = MAX_NOTE,
            singleLine = false,
        )
    }
    if (addingComment) {
        TextInputDialog(
            title = stringResource(R.string.feature_shelves_add_comment),
            label = stringResource(R.string.feature_shelves_comment),
            confirmLabel = stringResource(R.string.feature_shelves_add),
            onConfirm = {
                addingComment = false
                onAddComment(it)
            },
            onDismiss = { addingComment = false },
            maxLength = MAX_NOTE,
            singleLine = false,
        )
    }
    if (confirmDelete) {
        ConfirmDialog(
            title = stringResource(R.string.feature_shelves_delete_item),
            text = stringResource(R.string.feature_shelves_delete_confirm, item.title),
            confirmLabel = stringResource(CoreR.string.core_ui_delete),
            onConfirm = {
                confirmDelete = false
                onDelete()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column {
        HorizontalDivider(Modifier.padding(bottom = 12.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp).semantics { heading() },
        )
        content()
    }
}

/** A label over a row of chips, one of which may be on. Tapping the one that is on clears the choice. */
@Composable
private fun <T> ChoiceRow(label: String, options: List<T>, selected: T?, name: @Composable (T) -> String, onSelect: (T?) -> Unit) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelect(if (option == selected) null else option) },
                    label = { Text(name(option)) },
                )
            }
        }
    }
}

/** A value with its label, or a hint while empty; tapping opens the editor. */
@Composable
private fun EditableRow(label: String, value: String, hint: String, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClickLabel = stringResource(CoreR.string.core_ui_edit), onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value.ifBlank { hint },
            style = MaterialTheme.typography.bodyLarge,
            color = if (value.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** A number with a minus and a plus beside it. */
@Composable
private fun Counter(label: String, value: Int, onChange: (Int) -> Unit) {
    val less = stringResource(R.string.feature_shelves_decrease, label)
    val more = stringResource(R.string.feature_shelves_increase, label)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        IconButton(onClick = { onChange(value - 1) }, enabled = value > 0, modifier = Modifier.semantics { contentDescription = less }) {
            Text("−", style = MaterialTheme.typography.titleLarge)
        }
        Text(
            value.toString(),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 40.dp),
        )
        IconButton(onClick = { onChange(value + 1) }, enabled = value < MAX_COUNT, modifier = Modifier.semantics { contentDescription = more }) {
            Text("+", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@PreviewLightDark
@Composable
private fun ItemScreenPreview() {
    ShelfAnythingTheme {
        ItemScreen(
            item = Item(
                id = "1", shelf = Shelf.SERIES, title = "Lost", creator = "ABC", year = 2004,
                own = true, done = true, watchedOn = "Netflix", season = 2, episode = 7,
                details = mapOf("Genres" to "Drama, Mystery", "IMDb" to "https://www.imdb.com/title/tt0411008/"),
                description = "Out of the blackness.",
                comments = listOf(Comment("Season 2 is the best.", 1_700_000_000_000)),
            ),
            onChange = {},
            onShare = {},
            onDelete = {},
            onAddComment = {},
            onDeleteComment = {},
            onBack = {},
        )
    }
}
