package de.hdawg.rankinginfo.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ImportServiceTest {
    // ── extractPeriodFromFilename ────────────────────────────────────────────

    @Test
    fun `extracts date from Junioren filename`() {
        assertEquals(LocalDate.of(2018, 3, 31), ImportService.extractPeriodFromFilename("Junioren_20180331.csv"))
    }

    @Test
    fun `extracts date from Herren filename`() {
        assertEquals(LocalDate.of(2018, 4, 1), ImportService.extractPeriodFromFilename("Herren_20180401.csv"))
    }

    @Test
    fun `extracts date from Damen filename`() {
        assertEquals(LocalDate.of(2018, 4, 1), ImportService.extractPeriodFromFilename("Damen_20180401.csv"))
    }

    @Test
    fun `extracts date from December filename`() {
        assertEquals(LocalDate.of(2012, 12, 31), ImportService.extractPeriodFromFilename("Junioren_20121231.csv"))
    }

    @Test
    fun `throws for filename without underscore`() {
        assertThrows(IllegalArgumentException::class.java) {
            ImportService.extractPeriodFromFilename("Junioren-20180331.csv")
        }
    }

    // ── fileCategoryFromFilename ─────────────────────────────────────────────

    @Test
    fun `maps all four filename prefixes to categories`() {
        assertEquals("junioren", ImportService.fileCategoryFromFilename("Junioren_20180401.csv"))
        assertEquals("juniorinnen", ImportService.fileCategoryFromFilename("Juniorinnen_20180401.csv"))
        assertEquals("herren", ImportService.fileCategoryFromFilename("Herren_20180401.csv"))
        assertEquals("damen", ImportService.fileCategoryFromFilename("Damen_20180401.csv"))
    }

    @Test
    fun `throws for unknown filename prefix`() {
        assertThrows(IllegalArgumentException::class.java) {
            ImportService.fileCategoryFromFilename("Unknown_20180401.csv")
        }
    }

    // ── yobRangeToFetch (exact port of Ruby test cases) ──────────────────────
    //
    // Ruby test used date 2019-03-31.
    // Male   gender_factor = 100
    // Female gender_factor = 200

    private val period = LocalDate.of(2019, 3, 31)

    @Test
    fun `U11 male from yob 2008 returns 1 entry`() {
        val result = ImportService.yobRangeToFetch("2008", 11, period, 100)
        assertEquals(1, result.size)
        assertEquals(108, result[0]) // "08".toInt() + 100
    }

    @Test
    fun `U12 male from yob 2008 returns 2 entries`() {
        val result = ImportService.yobRangeToFetch("2008", 12, period, 100)
        assertEquals(2, result.size)
        assertEquals(108, result[0])
        assertEquals(107, result[1])
    }

    @Test
    fun `U14 female from yob 2008 returns 4 entries`() {
        val result = ImportService.yobRangeToFetch("2008", 14, period, 200)
        assertEquals(4, result.size)
        assertEquals(208, result[0])
        assertEquals(206, result[2])
    }

    @Test
    fun `U16 female from yob 2008 returns 6 entries`() {
        val result = ImportService.yobRangeToFetch("2008", 16, period, 200)
        assertEquals(6, result.size)
        assertEquals(208, result[0])
        assertEquals(204, result[4]) // "04" + 200 = 204
    }

    @Test
    fun `yobRangeToFetch is empty when player is too young for age group`() {
        // period.year - yob.toInt() = 2019 - 2012 = 7; ageGroup=6 → while(6 >= 7+0) is false immediately
        val result = ImportService.yobRangeToFetch("2012", 6, period, 100)
        assertTrue(result.isEmpty())
    }
}
