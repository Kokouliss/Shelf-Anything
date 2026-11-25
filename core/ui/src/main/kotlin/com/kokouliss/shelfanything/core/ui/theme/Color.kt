package com.kokouliss.shelfanything.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.kokouliss.shelfanything.core.domain.design.Palette.Dark
import com.kokouliss.shelfanything.core.domain.design.Palette.Light

// Graphite paper and ink with amber as the lead colour: the darker amber for text, links and the
// main button, the bright amber of the logo square as a fill under ink text (the add button, the
// active chip). Fixed colours on purpose, so the app keeps the family look instead of following the
// wallpaper. The values and their contrast test live in core/domain.
internal val LightColors = lightColorScheme(
    primary = Color(Light.LEAD_TEXT),
    onPrimary = Color(Light.SURFACE),
    primaryContainer = Color(Light.LEAD),
    onPrimaryContainer = Color(Light.ON_LEAD),
    secondary = Color(Light.MUTED),
    onSecondary = Color(Light.SURFACE),
    secondaryContainer = Color(Light.SURFACE_2),
    onSecondaryContainer = Color(Light.INK),
    // The triangle teal of the logos is 4.3 to 1 on paper, so text gets this darker step of it.
    tertiary = Color(0xFF2A7384),
    onTertiary = Color(Light.SURFACE),
    tertiaryContainer = Color(Light.LEAD_SOFT),
    onTertiaryContainer = Color(Light.LEAD_TEXT),
    error = Color(Light.DANGER),
    onError = Color(Light.SURFACE),
    errorContainer = Color(0xFFF6E9E6),
    onErrorContainer = Color(Light.DANGER),
    background = Color(Light.BACKGROUND),
    onBackground = Color(Light.INK),
    surface = Color(Light.BACKGROUND),
    onSurface = Color(Light.INK),
    surfaceVariant = Color(Light.SURFACE_2),
    onSurfaceVariant = Color(Light.MUTED),
    outline = Color(Light.OUTLINE),
    outlineVariant = Color(Light.LINE),
    surfaceDim = Color(0xFFE4E6EC),
    surfaceBright = Color(Light.SURFACE),
    // Cards are white, so they stand out from the paper under them.
    surfaceContainerLowest = Color(Light.SURFACE),
    surfaceContainerLow = Color(Light.SURFACE),
    surfaceContainer = Color(Light.SURFACE_2),
    surfaceContainerHigh = Color(0xFFE4E6EC),
    surfaceContainerHighest = Color(Light.LINE),
    inverseSurface = Color(Dark.SURFACE),
    inverseOnSurface = Color(Dark.INK),
    inversePrimary = Color(Dark.LEAD_TEXT),
)

internal val DarkColors = darkColorScheme(
    primary = Color(Dark.LEAD_TEXT),
    onPrimary = Color(Dark.ON_LEAD),
    primaryContainer = Color(Dark.LEAD),
    onPrimaryContainer = Color(Dark.ON_LEAD),
    secondary = Color(Dark.MUTED),
    onSecondary = Color(Dark.BACKGROUND),
    secondaryContainer = Color(Dark.SURFACE_2),
    onSecondaryContainer = Color(Dark.INK),
    tertiary = Color(0xFF7FB8C6),
    onTertiary = Color(Dark.BACKGROUND),
    tertiaryContainer = Color(Dark.LEAD_SOFT),
    onTertiaryContainer = Color(Dark.LEAD_TEXT),
    error = Color(Dark.DANGER),
    onError = Color(Dark.BACKGROUND),
    errorContainer = Color(0xFF3E2829),
    onErrorContainer = Color(Dark.DANGER),
    background = Color(Dark.BACKGROUND),
    onBackground = Color(Dark.INK),
    surface = Color(Dark.BACKGROUND),
    onSurface = Color(Dark.INK),
    surfaceVariant = Color(Dark.SURFACE_2),
    onSurfaceVariant = Color(Dark.MUTED),
    outline = Color(Dark.OUTLINE),
    outlineVariant = Color(Dark.LINE),
    surfaceDim = Color(Dark.BACKGROUND),
    surfaceBright = Color(Dark.SURFACE_2),
    surfaceContainerLowest = Color(0xFF12141A),
    surfaceContainerLow = Color(Dark.SURFACE),
    surfaceContainer = Color(Dark.SURFACE_2),
    surfaceContainerHigh = Color(0xFF353A46),
    surfaceContainerHighest = Color(Dark.LINE),
    inverseSurface = Color(Light.SURFACE_2),
    inverseOnSurface = Color(Light.INK),
    inversePrimary = Color(Light.LEAD_TEXT),
)

/** The three shapes of the AnyKind logos, for drawings only, never for text. */
object KindColors {
    val circle = Color(Light.KIND_CIRCLE)
    val square = Color(Light.KIND_SQUARE)
    val triangle = Color(Light.KIND_TRIANGLE)
}
