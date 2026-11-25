package com.kokouliss.shelfanything.feature.shelves

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
import androidx.compose.ui.unit.dp
import com.kokouliss.shelfanything.core.domain.model.Item
import com.kokouliss.shelfanything.core.ui.R as CoreR
import com.kokouliss.shelfanything.core.ui.component.doneLabel

/** One of the three flags on an item, as a row with a checkbox. */
@Composable
internal fun FlagRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onChange)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp))
    }
}

/** "Own · Want · Read": the flags that are on, in words. Empty when none is. */
@Composable
internal fun flagsText(item: Item): String = listOfNotNull(
    stringResource(CoreR.string.core_ui_own).takeIf { item.own },
    stringResource(CoreR.string.core_ui_want).takeIf { item.want },
    stringResource(item.shelf.doneLabel()).takeIf { item.done },
).joinToString(" · ")
