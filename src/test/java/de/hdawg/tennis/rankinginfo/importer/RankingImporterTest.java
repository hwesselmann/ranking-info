package de.hdawg.tennis.rankinginfo.importer;

import de.hdawg.tennis.rankinginfo.model.Ranking;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RankingImporterTest {

    // TODO refactor and add some initialization code instead of starting all over in every test

    private static final String IMPORT_BASE_FOLDER = "src/test/resources";
    private static final String FILENAME = "Junioren_20180331.csv";
    private static final String INVALID_FILENAME = "Junioren-31032018.csv";

    @Test
    public void testImportFileRetrieval() {
        RankingImporter rankingImporter = new RankingImporter();
        File[] filesToImport = rankingImporter.getFilesToImport(IMPORT_BASE_FOLDER);
        assertEquals(1, filesToImport.length);

        filesToImport = rankingImporter.getFilesToImport(IMPORT_BASE_FOLDER + "/testFolder");
        assertEquals(2, filesToImport.length);
    }

    @Test
    public void testPeriodRetrievalFromFilename() {
        RankingImporter rankingImporter = new RankingImporter();
        LocalDate periodEnding = rankingImporter.getImportPeriodFromFileName(FILENAME);

        assertEquals(31, periodEnding.getDayOfMonth());
        assertEquals(3, periodEnding.getMonthValue());
        assertEquals(2018, periodEnding.getYear());
    }

    @Test
    public void testPeriodRetrievalFromInvalidFilename() {
        RankingImporter rankingImporter = new RankingImporter();
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            rankingImporter.getImportPeriodFromFileName(INVALID_FILENAME);
        });
    }

    @Test
    public void testMappingOfGenderFromDTBId() {
        RankingImporter rankingImporter = new RankingImporter();

        assertEquals("m", rankingImporter.mapGender("12345678"));
        assertEquals("w", rankingImporter.mapGender("52345678"));
        assertEquals("w", rankingImporter.mapGender("22345678"));
    }

    @Test
    public void testIdentifyingYobFromDTBId() {
        RankingImporter rankingImporter = new RankingImporter();

        assertEquals(2008, rankingImporter.identifyYobFromDTBid("10823456"));
        assertEquals(2011, rankingImporter.identifyYobFromDTBid("21123456"));
        assertEquals(2001, rankingImporter.identifyYobFromDTBid("20123456"));
        assertEquals(2099, rankingImporter.identifyYobFromDTBid("19923456"));
    }

    @Test
    public void testReadingAndMappingRawDataToRankings() throws IOException {
        RankingImporter rankingImporter = new RankingImporter();
        File rankingImportTestFile = new File(getClass().getClassLoader().getResource(FILENAME).getFile());
        List<Ranking> rankings = rankingImporter.readRankingsFromFile(rankingImportTestFile.getAbsolutePath(), LocalDate.of(2019, 03,31));

        assertEquals(17, rankings.size());
        assertEquals("Eintracht Frankfurt", rankings.get(0).getClub());
        assertEquals("J****n", rankings.get(0).getFirstname());
        assertEquals("P****y", rankings.get(0).getLastname());
        assertEquals(173, rankings.get(16).getRank());
        assertEquals("N***h", rankings.get(16).getFirstname());
        assertEquals("W****n", rankings.get(16).getLastname());
    }
}
