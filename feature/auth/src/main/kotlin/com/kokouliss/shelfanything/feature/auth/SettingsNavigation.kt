package com.kokouliss.shelfanything.feature.auth

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object SettingsKey : NavKey

fun EntryProviderScope<NavKey>.settingsEntries(onBack: () -> Unit) {
    entry<SettingsKey> { SettingsRoute(onBack = onBack) }
}
