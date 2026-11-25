package com.kokouliss.shelfanything.core.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kokouliss.shelfanything.core.domain.model.Shelf
import com.kokouliss.shelfanything.core.domain.model.ShelfGroup

/** Every shelf under its group, with a checkbox. Used at first launch and in Settings. */
@Composable
fun ShelfChecklist(enabled: Set<Shelf>, onToggle: (Shelf) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        ShelfGroup.entries.forEach { group ->
            Text(
                stringResource(group.label()),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp).semantics { heading() },
            )
            Shelf.entries.filter { it.group == group }.forEach { shelf ->
                val on = shelf in enabled
                Row(
                    Modifier
                        .fillMaxWidth()
                        // The last shelf stays on: the app needs one to show.
                        .toggleable(value = on, enabled = !on || enabled.size > 1, role = Role.Checkbox) { onToggle(shelf) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // The whole row is the control; a second target inside it would be read out twice.
                    Checkbox(checked = on, onCheckedChange = null)
                    Text(stringResource(shelf.label()), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 16.dp))
                }
            }
        }
    }
}
