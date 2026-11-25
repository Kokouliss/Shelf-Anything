package com.kokouliss.shelfanything.core.domain

import com.kokouliss.shelfanything.core.domain.design.Palette
import com.kokouliss.shelfanything.core.domain.design.Palette.Dark
import com.kokouliss.shelfanything.core.domain.design.Palette.Light
import kotlin.test.Test
import kotlin.test.assertTrue

/** The palette is chosen by hand, so this keeps a later change of one colour readable. */
class PaletteTest {

    private fun assertReadable(text: Long, paper: Long, atLeast: Double = 4.5) {
        val ratio = Palette.contrast(text, paper)
        assertTrue(ratio >= atLeast, "${text.toString(16)} on ${paper.toString(16)} is $ratio")
    }

    @Test
    fun lightTextPairsKeepAtLeast4_5to1() = with(Light) {
        assertReadable(INK, BACKGROUND)
        assertReadable(INK, SURFACE)
        assertReadable(INK, SURFACE_2)
        assertReadable(MUTED, BACKGROUND)
        assertReadable(MUTED, SURFACE)
        assertReadable(LEAD_TEXT, BACKGROUND)
        assertReadable(LEAD_TEXT, SURFACE)
        assertReadable(LEAD_TEXT, LEAD_SOFT)
        assertReadable(ON_LEAD, LEAD)
        assertReadable(DANGER, SURFACE)
    }

    @Test
    fun darkTextPairsKeepAtLeast4_5to1() = with(Dark) {
        assertReadable(INK, BACKGROUND)
        assertReadable(INK, SURFACE)
        assertReadable(INK, SURFACE_2)
        assertReadable(MUTED, BACKGROUND)
        assertReadable(MUTED, SURFACE)
        assertReadable(LEAD_TEXT, BACKGROUND)
        assertReadable(LEAD_TEXT, SURFACE)
        assertReadable(LEAD_TEXT, LEAD_SOFT)
        assertReadable(ON_LEAD, LEAD)
        assertReadable(DANGER, SURFACE)
    }

    @Test
    fun outlinesAndFillsKeepAtLeast3to1() {
        assertReadable(Light.OUTLINE, Light.SURFACE, 3.0)
        assertReadable(Light.LEAD, Light.SURFACE, 3.0)
        assertReadable(Dark.OUTLINE, Dark.SURFACE, 3.0)
        assertReadable(Dark.LEAD, Dark.SURFACE, 3.0)
    }

    @Test
    fun contrastOfBlackOnWhiteIs21() {
        assertTrue(Palette.contrast(0xFF000000, 0xFFFFFFFF) > 20.9)
    }
}
