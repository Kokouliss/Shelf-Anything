package com.kokouliss.shelfanything

import android.app.Application
import com.kokouliss.shelfanything.feature.auth.restoreNightMode
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ShelfAnythingApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Before any screen exists, so the first one already has the chosen light or dark look.
        restoreNightMode(this)
    }
}
