package com.kokouliss.shelfanything

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.kokouliss.shelfanything.core.data.isBackendConfigured
import com.kokouliss.shelfanything.core.ui.theme.ShelfAnythingTheme
import dagger.hilt.android.AndroidEntryPoint

// AppCompatActivity, because it is what applies the language chosen in Settings on Android 9 to 12.
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Checked before any screen asks Hilt for Firebase, which would crash without a configuration.
        val isConfigured = isBackendConfigured(this)
        setContent {
            ShelfAnythingTheme {
                if (isConfigured) ShelfAnythingApp() else SetupNeededScreen()
            }
        }
    }
}
