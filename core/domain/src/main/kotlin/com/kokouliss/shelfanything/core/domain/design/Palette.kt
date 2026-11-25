package com.kokouliss.shelfanything.core.domain.design

import kotlin.math.pow

/**
 * The app's palette, as ARGB values. The theme reads these; they live here so a unit test can check
 * every text pair against WCAG AA without an Android runtime.
 *
 * From Panos's AnyKind graphite kit (app-palette.css): ink and paper neutrals shared by the whole
 * family, and amber as this app's lead colour. The bright amber of the logo square is a fill
 * (buttons, the active chip) under ink text; as text on paper it is the darker LEAD_TEXT, which the
 * kit sets so that it reads at 4.5 to 1. The three shape colours mean the same kinds in every app.
 */
object Palette {
    object Light {
        const val BACKGROUND = 0xFFF7F7FA
        const val SURFACE = 0xFFFFFFFF
        const val SURFACE_2 = 0xFFEDEEF2
        const val LINE = 0xFFDCDEE5
        const val INK = 0xFF171A21
        const val MUTED = 0xFF6B6C73
        const val OUTLINE = 0xFF74767E
        const val LEAD = 0xFFB58428
        const val ON_LEAD = 0xFF171A21
        const val LEAD_SOFT = 0xFFF6F0E5
        const val LEAD_TEXT = 0xFF896626
        const val DANGER = 0xFFB4472E
        const val KIND_CIRCLE = 0xFFB4472E
        const val KIND_SQUARE = 0xFFB08128
        const val KIND_TRIANGLE = 0xFF2F7F92
    }

    object Dark {
        const val BACKGROUND = 0xFF171A21
        const val SURFACE = 0xFF20242D
        const val SURFACE_2 = 0xFF2B303B
        const val LINE = 0xFF3A404C
        const val INK = 0xFFF2F3F6
        const val MUTED = 0xFF95979C
        const val OUTLINE = 0xFF8B8E95
        const val LEAD = 0xFFB58428
        const val ON_LEAD = 0xFF171A21
        const val LEAD_SOFT = 0xFF443B2C
        const val LEAD_TEXT = 0xFFC7A25C
        const val DANGER = 0xFFCE8675
        const val KIND_CIRCLE = 0xFFBD5D47
        const val KIND_SQUARE = 0xFFB58428
        const val KIND_TRIANGLE = 0xFF358395
    }

    /** The WCAG contrast ratio of two opaque ARGB colours, from 1 (the same) to 21 (black on white). */
    fun contrast(a: Long, b: Long): Double {
        val (light, dark) = listOf(luminance(a), luminance(b)).sortedDescending()
        return (light + 0.05) / (dark + 0.05)
    }

    private fun luminance(argb: Long): Double {
        fun channel(shift: Int): Double {
            val c = ((argb shr shift) and 0xFF) / 255.0
            return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
    }
}
