package com.kokouliss.shelfanything.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kokouliss.shelfanything.core.ui.R

/** Manrope, the face of the AnyKind wordmark (OFL, with Greek letters), for headings and titles. Body text stays in the phone's sans. */
private val Manrope = FontFamily(
    Font(R.font.core_ui_manrope_semibold, FontWeight.SemiBold),
    Font(R.font.core_ui_manrope_extrabold, FontWeight.ExtraBold),
)

private val AppTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = Manrope, fontWeight = FontWeight.ExtraBold),
        displayMedium = displayMedium.copy(fontFamily = Manrope, fontWeight = FontWeight.ExtraBold),
        displaySmall = displaySmall.copy(fontFamily = Manrope, fontWeight = FontWeight.ExtraBold),
        headlineLarge = headlineLarge.copy(fontFamily = Manrope, fontWeight = FontWeight.ExtraBold),
        headlineMedium = headlineMedium.copy(fontFamily = Manrope, fontWeight = FontWeight.ExtraBold),
        headlineSmall = headlineSmall.copy(fontFamily = Manrope, fontWeight = FontWeight.ExtraBold),
        titleLarge = titleLarge.copy(fontFamily = Manrope, fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontFamily = Manrope, fontWeight = FontWeight.SemiBold),
        titleSmall = titleSmall.copy(fontFamily = Manrope, fontWeight = FontWeight.SemiBold),
        labelLarge = labelLarge.copy(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp),
    )
}

/** Corners like the tile of the app icon: 24 of 100, so 12 dp on a 48 dp control. */
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

/** The graphite look. Always these colours: the wallpaper colours of Android 12 would replace the paper. */
@Composable
fun ShelfAnythingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
