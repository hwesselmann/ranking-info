package de.hdawg.tennis.rankinginfo.importer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.File;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RankingImporterTest {

    private static final String IMPORT_BASE_FOLDER = "src/test/resources";
    private static final String FILENAME = "Junioren_20180331.csv";
    private static final String INVALID_FILENAME = "Junioren-31032018.csv";
    private static final String OVERALL = "Overall";

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
}
