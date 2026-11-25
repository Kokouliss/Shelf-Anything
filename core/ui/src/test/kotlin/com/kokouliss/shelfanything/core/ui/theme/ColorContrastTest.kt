package com.kokouliss.shelfanything.core.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Test
import kotlin.test.assertTrue

/** The Material roles are mapped by hand from the palette, so this keeps every mapping readable. */
class ColorContrastTest {

    private fun contrast(a: Color, b: Color): Float {
        val (light, dark) = listOf(a.luminance(), b.luminance()).sortedDescending()
        return (light + 0.05f) / (dark + 0.05f)
    }

    @Test
    fun textAndIconsKeepAtLeast4_5to1() {
        for (colors in listOf(LightColors, DarkColors)) {
            val pairs = with(colors) {
                listOf(
                    onPrimary to primary,
                    onPrimaryContainer to primaryContainer,
                    onSecondaryContainer to secondaryContainer,
                    onTertiaryContainer to tertiaryContainer,
                    onErrorContainer to errorContainer,
                    inverseOnSurface to inverseSurface,
                    onSurface to surface,
                    onSurface to surfaceContainerLow,
                    onSurface to surfaceContainerHighest,
                    onSurfaceVariant to surface,
                    onSurfaceVariant to surfaceContainerLow,
                    primary to surface,
                    primary to surfaceContainerLow,
                    tertiary to surface,
                    error to surface,
                    error to surfaceContainerLow,
                )
            }
            pairs.forEach { (text, paper) ->
                assertTrue(contrast(text, paper) >= 4.5f, "$text on $paper is ${contrast(text, paper)}")
            }
        }
    }

    @Test
    fun outlinesAndFillsKeepAtLeast3to1() {
        for (colors in listOf(LightColors, DarkColors)) {
            assertTrue(contrast(colors.outline, colors.surface) >= 3f)
            assertTrue(contrast(colors.outline, colors.surfaceContainerLow) >= 3f)
            assertTrue(contrast(colors.primaryContainer, colors.surface) >= 3f)
        }
    }
}
