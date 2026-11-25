package com.kokouliss.shelfanything.feature.shelves

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kokouliss.shelfanything.core.domain.model.Shelf
import com.kokouliss.shelfanything.core.ui.component.ShelfChecklist
import com.kokouliss.shelfanything.core.ui.component.pageWidth
import com.kokouliss.shelfanything.core.ui.theme.ShelfAnythingTheme

/** The first launch: which shelves to show. The app swaps it for the home screen once a choice is saved. */
@Composable
fun ShelfPickerRoute() {
    val viewModel: ShelvesViewModel = hiltViewModel()
    var picked by rememberSaveable { mutableStateOf(Shelf.DEFAULT) }
    ShelfPickerScreen(
        picked = picked,
        onToggle = { shelf -> picked = if (shelf in picked) picked - shelf else picked + shelf },
        onStart = { viewModel.setEnabledShelves(picked) },
    )
}

@Composable
internal fun ShelfPickerScreen(picked: Set<Shelf>, onToggle: (Shelf) -> Unit, onStart: () -> Unit) {
    val title = stringResource(R.string.feature_shelves_pick_title)
    Surface(Modifier.fillMaxSize().semantics { paneTitle = title }) {
        Column(Modifier.safeDrawingPadding().pageWidth().verticalScroll(rememberScrollState()).padding(vertical = 24.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 16.dp).semantics { heading() },
            )
            Text(
                stringResource(R.string.feature_shelves_pick_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            ShelfChecklist(picked, onToggle)
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(stringResource(R.string.feature_shelves_start))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ShelfPickerScreenPreview() {
    ShelfAnythingTheme { ShelfPickerScreen(picked = Shelf.DEFAULT, onToggle = {}, onStart = {}) }
}
