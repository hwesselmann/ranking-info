package de.hdawg.tennis.rankinginfo.importer;

import de.hdawg.tennis.rankinginfo.model.Federation;
import de.hdawg.tennis.rankinginfo.model.Ranking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RankingImporter {

    @Value("import.folder")
    private String importBaseFolder;

    /**
     * Import new Rankings from input files.
     *
     * @throws IOException IOException on filesystem errors
     */
    public void importRankings() throws IOException {
        File[] filesToImport = getFilesToImport(importBaseFolder);
        for (File importFile : filesToImport) {
            LocalDate periodToImport = getImportPeriodFromFileName(importFile.getName());
            List<Ranking> overallRankings = readRankingsFromFile(importFile.getAbsolutePath(), periodToImport);
            List<Ranking> calculatedRankings = calculateCompleteRankings(overallRankings);
            // Changeset schreiben oder in DB schreiben?
            // Nationalitäten Diff?
            // Liquibase-Update antriggern?
            // was passiert mit dem Importfile?
        }
    }

    /**
     * Get a list of files in a given folder for processing.
     *
     * @param importBaseFolder base folder
     * @return array of files in base folder
     */
    File[] getFilesToImport(String importBaseFolder) {
        File importFolder = new File(importBaseFolder);
        return importFolder.listFiles(File::isFile);
    }

    /**
     * Files to be imported must be named 'xxxxx_yyyymmdd.csv', e.g. 'Junioren_20190331.csv'.
     *
     * @param name name of file to import
     * @return date portion
     */
    LocalDate getImportPeriodFromFileName(String name) {
        String nameWithoutExtension = name.split("\\.")[0];
        String[] nameSplittedByUnderscore = nameWithoutExtension.split("_");
        if (nameSplittedByUnderscore.length < 2) {
            log.error("File {} seems to contain no or invalid date extension", nameWithoutExtension);
            throw new IllegalArgumentException("The file cannot be splitted into name and date parts");
        }
        int day = Integer.parseInt(nameSplittedByUnderscore[nameSplittedByUnderscore.length - 1].substring(6, 8));
        int month = Integer.parseInt(nameSplittedByUnderscore[nameSplittedByUnderscore.length - 1].substring(4, 6));
        int year = Integer.parseInt(nameSplittedByUnderscore[nameSplittedByUnderscore.length - 1].substring(0, 4));
        log.debug("date: {}.{}.{}", day, month, year);
        return LocalDate.of(year, month, day);
    }

    List<Ranking> readRankingsFromFile(String path, LocalDate periodToImport) throws IOException {
        List<String[]> rawRankingData = Files.lines(Paths.get(path)).map(line -> line.split(",")).collect(Collectors.toList());
        return rawRankingData.stream().map(item -> {
            Ranking ranking = new Ranking();
            ranking.setDtbId(item[4].split(" ")[0]);
            ranking.setAgeGroup("all");
            ranking.setPeriod(periodToImport);
            ranking.setRank(Integer.parseInt(item[0]));
            ranking.setPoints(item[6]);
            ranking.setFirstname(item[2]);
            ranking.setLastname(item[1]);
            ranking.setYob(identifyYobFromDTBid(ranking.getDtbId()));
            ranking.setNationality(item[3]);
            ranking.setGender(mapGender(ranking.getDtbId()));
            ranking.setFederation(Federation.valueOf(item[4].split(" ")[1]));
            ranking.setClub(item[5]);
            ranking.setIncludeAllPlayers(true);
            ranking.setOnlyYoBPlayers(false);
            ranking.setEndOfYearRanking(false);
            return ranking;
        }).collect(Collectors.toList());
    }

    String mapGender(String dtbId) {
        if (dtbId.startsWith("1")) {
            return "m";
        } else {
            return "w";
        }
    }

    int identifyYobFromDTBid(String dtbId) {
        // True, this is not very sophisticated. I'll deal with the implications in 2099
        return 2000 + Integer.parseInt(dtbId.substring(1, 3));
    }

    List<Ranking> calculateCompleteRankings(List<Ranking> overallRankings) {
        return Collections.emptyList();
    }
}
