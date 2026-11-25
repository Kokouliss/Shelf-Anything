package com.kokouliss.shelfanything

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.kokouliss.shelfanything.core.domain.repository.AuthRepository
import com.kokouliss.shelfanything.core.domain.repository.ShelfRepository
import com.kokouliss.shelfanything.core.ui.component.AppIcons
import com.kokouliss.shelfanything.core.ui.component.EmptyState
import com.kokouliss.shelfanything.core.ui.component.LoadingBox
import com.kokouliss.shelfanything.core.ui.theme.ShelfAnythingTheme
import com.kokouliss.shelfanything.feature.auth.SettingsKey
import com.kokouliss.shelfanything.feature.auth.SignInRoute
import com.kokouliss.shelfanything.feature.auth.settingsEntries
import com.kokouliss.shelfanything.feature.shelves.AddItemKey
import com.kokouliss.shelfanything.feature.shelves.HomeKey
import com.kokouliss.shelfanything.feature.shelves.ItemKey
import com.kokouliss.shelfanything.feature.shelves.ShelfKey
import com.kokouliss.shelfanything.feature.shelves.ShelfPickerRoute
import com.kokouliss.shelfanything.feature.shelves.shelvesEntries
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal enum class AuthState { Loading, SignedOut, SignedIn }

@HiltViewModel
internal class AppViewModel @Inject constructor(authRepository: AuthRepository, shelves: ShelfRepository) : ViewModel() {
    val authState: StateFlow<AuthState> = authRepository.currentUser
        .map { user -> if (user == null) AuthState.SignedOut else AuthState.SignedIn }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuthState.Loading)

    /** Null until the store answered; false on the first launch, before any shelf was picked. */
    val shelvesPicked: StateFlow<Boolean?> = shelves.enabledShelves.map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@Composable
internal fun ShelfAnythingApp(viewModel: AppViewModel = hiltViewModel()) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val shelvesPicked by viewModel.shelvesPicked.collectAsStateWithLifecycle()
    when (authState) {
        AuthState.Loading -> Surface { LoadingBox() }
        AuthState.SignedOut -> SignInRoute()
        AuthState.SignedIn -> when (shelvesPicked) {
            null -> Surface { LoadingBox() }
            false -> ShelfPickerRoute()
            true -> SignedInNavigation()
        }
    }
}

@Composable
private fun SignedInNavigation() {
    val backStack = rememberNavBackStack(HomeKey)
    val goBack: () -> Unit = { backStack.removeLastOrNull() }

    NavDisplay(
        backStack = backStack,
        onBack = goBack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            shelvesEntries(
                onBack = goBack,
                onOpenShelf = { shelf -> backStack.add(ShelfKey(shelf)) },
                onAddItem = { shelf -> backStack.add(AddItemKey(shelf)) },
                onOpenItem = { id -> backStack.add(ItemKey(id)) },
                onItemAdded = { id ->
                    // The new item takes the place of the add screen, so back goes to the shelf.
                    backStack.removeLastOrNull()
                    backStack.add(ItemKey(id))
                },
                onOpenSettings = { backStack.add(SettingsKey) },
            )
            settingsEntries(onBack = goBack)
        },
    )
}

/** Shown by builds made without `app/google-services.json`, instead of crashing on the first Firebase call. */
@Composable
internal fun SetupNeededScreen() {
    Surface {
        EmptyState(
            icon = AppIcons.Refresh,
            title = stringResource(R.string.setup_needed_title),
            body = stringResource(R.string.setup_needed_body),
        )
    }
}

@PreviewLightDark
@Composable
private fun SetupNeededScreenPreview() {
    ShelfAnythingTheme { SetupNeededScreen() }
}
