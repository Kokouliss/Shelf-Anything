package com.kokouliss.shelfanything.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kokouliss.shelfanything.core.domain.export.LibraryText
import com.kokouliss.shelfanything.core.domain.model.Shelf
import com.kokouliss.shelfanything.core.domain.model.User
import com.kokouliss.shelfanything.core.domain.repository.AuthRepository
import com.kokouliss.shelfanything.core.domain.repository.ItemRepository
import com.kokouliss.shelfanything.core.domain.repository.ShelfRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Where deleting the account stands. Success needs no state: the app leaves this screen. */
internal enum class DeleteState { Idle, Deleting, Failed }

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val shelves: ShelfRepository,
    private val items: ItemRepository,
) : ViewModel() {

    /** Null for a moment while loading, and again right after signing out, when the app leaves this screen. */
    val user: StateFlow<User?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val enabledShelves: StateFlow<Set<Shelf>> = shelves.enabledShelves.map { it ?: Shelf.DEFAULT }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Shelf.DEFAULT)

    val itemCount: StateFlow<Int> = items.items.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** A shelf that is switched off keeps its items; they show again when it is switched back on. */
    fun toggleShelf(shelf: Shelf) {
        val now = enabledShelves.value
        val next = if (shelf in now) now - shelf else now + shelf
        if (next.isEmpty()) return
        viewModelScope.launch { shelves.setEnabledShelves(next) }
    }

    /** The whole collection as a CSV file. */
    suspend fun exportCsv(): String = LibraryText.csv(items.items.first())

    /** The whole collection as a readable list, for a message. */
    suspend fun shareText(shelfName: (Shelf) -> String): String = LibraryText.share(items.items.first(), shelfName)

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }

    private val _deleteState = MutableStateFlow(DeleteState.Idle)
    val deleteState: StateFlow<DeleteState> = _deleteState.asStateFlow()

    /** [idToken] is null when Google did not hand over a fresh sign-in token. */
    fun deleteAccount(idToken: String?) = delete { idToken != null && authRepository.deleteAccount(idToken) }

    fun deleteAccountWithPassword(password: String) = delete { authRepository.deleteAccountWithPassword(password) }

    private fun delete(attempt: suspend () -> Boolean) {
        viewModelScope.launch {
            _deleteState.value = DeleteState.Deleting
            _deleteState.value = if (attempt()) DeleteState.Idle else DeleteState.Failed
        }
    }

    private val _verificationSent = MutableStateFlow(false)
    val verificationSent: StateFlow<Boolean> = _verificationSent.asStateFlow()

    fun sendVerificationEmail() {
        viewModelScope.launch { _verificationSent.value = authRepository.sendVerificationEmail() }
    }

    init {
        // Opening Settings after tapping the link shows the address as confirmed right away.
        viewModelScope.launch { authRepository.refreshEmailVerification() }
    }
}
