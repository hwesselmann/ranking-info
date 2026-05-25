package de.hdawg.rankinginfo.service

import de.hdawg.rankinginfo.repository.ImportHistoryRepository
import de.hdawg.rankinginfo.repository.RankingRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.io.File
import java.nio.file.Path

@SpringBootTest
class ImportServiceScanTest {
    @Autowired lateinit var importService: ImportService

    @Autowired lateinit var rankingRepository: RankingRepository

    @Autowired lateinit var importHistoryRepository: ImportHistoryRepository

    @AfterEach
    fun cleanup() {
        rankingRepository.deleteAll()
        importHistoryRepository.deleteAll()
    }

    // ── scanAndImport ────────────────────────────────────────────────────────

    @Test
    fun `scanAndImport imports CSV files from folder`(
        @TempDir dir: Path,
    ) {
        copyFixture("Herren_20180401.csv", dir)
        importService.scanAndImport(dir.toString())
        assertEquals(10, rankingRepository.count())
    }

    @Test
    fun `scanAndImport skips already-imported files`(
        @TempDir dir: Path,
    ) {
        copyFixture("Herren_20180401.csv", dir)
        importService.scanAndImport(dir.toString())
        importService.scanAndImport(dir.toString()) // second run — should skip
        assertEquals(10, rankingRepository.count())
    }

    @Test
    fun `scanAndImport writes error log for invalid files`(
        @TempDir dir: Path,
    ) {
        File(dir.toFile(), "Herren_invalid.csv").writeText("bad,data")
        importService.scanAndImport(dir.toString())
        val errorLog = dir.resolve("error.log").toFile()
        assertTrue(errorLog.exists(), "error.log should be created for failed import")
    }

    @Test
    fun `scanAndImport handles non-existent folder gracefully`() {
        // Should not throw
        importService.scanAndImport("/tmp/does_not_exist_xyz_123")
    }

    @Test
    fun `scanAndImport appends separator between multiple errors`(
        @TempDir dir: Path,
    ) {
        File(dir.toFile(), "Herren_bad1.csv").writeText("bad")
        File(dir.toFile(), "Herren_bad2.csv").writeText("bad")
        importService.scanAndImport(dir.toString())
        val content = dir.resolve("error.log").toFile().readText()
        assertTrue(content.contains("\n\n"), "Multiple errors should be separated by blank line")
    }

    // ── year-end ranking path (period.monthValue == 1) ───────────────────────

    @Test
    fun `Junioren import with January date creates year-end rankings`(
        @TempDir dir: Path,
    ) {
        // Create a fixture named with a January date so year-end rankings are computed
        val src = javaClass.classLoader.getResourceAsStream("fixtures/Junioren_20180401.csv")!!.readBytes()
        File(dir.toFile(), "Junioren_20180101.csv").writeBytes(src)
        importService.scanAndImport(dir.toString())
        val yearEnd = rankingRepository.findAll().filter { it.yearEndRanking }
        assertTrue(yearEnd.isNotEmpty(), "January import should produce year-end rankings")
    }

    private fun copyFixture(
        name: String,
        dir: Path,
    ) {
        val bytes = javaClass.classLoader.getResourceAsStream("fixtures/$name")!!.readBytes()
        File(dir.toFile(), name).writeBytes(bytes)
    }
}
