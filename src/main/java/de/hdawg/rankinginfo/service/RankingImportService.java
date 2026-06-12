package de.hdawg.rankinginfo.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.hdawg.rankinginfo.domain.ImportHistory;
import de.hdawg.rankinginfo.domain.Ranking;
import de.hdawg.rankinginfo.domain.RankingCoding;
import de.hdawg.rankinginfo.repository.ImportHistoryRepository;
import de.hdawg.rankinginfo.repository.RankingRepository;

@SuppressWarnings("PMD.GuardLogStatement")
@Service
public class RankingImportService {

  private static final Logger log = LoggerFactory.getLogger(RankingImportService.class);

  private static final String CATEGORY_JUNIOREN = "junioren";
  private static final String CATEGORY_JUNIORINNEN = "juniorinnen";

  private static final Set<String> SPECIAL_SCORES = Set.of("0,0", "PR", "Einst.");
  private static final Map<String, Integer> GENDER_FACTORS =
      Map.of(
          CATEGORY_JUNIOREN, RankingCoding.GENDER_FACTOR_JUNIOREN,
          CATEGORY_JUNIORINNEN, RankingCoding.GENDER_FACTOR_JUNIORINNEN);
  private static final Map<String, String> AGE_GROUP_MAP =
      Map.of(
          "herren", "m00",
          "damen", "w00",
          CATEGORY_JUNIOREN, "overall",
          CATEGORY_JUNIORINNEN, "overall");

  private static final int CSV_COL_POSITION = 0;
  private static final int CSV_COL_LASTNAME = 1;
  private static final int CSV_COL_FIRSTNAME = 2;
  private static final int CSV_COL_NATIONALITY = 3;
  private static final int CSV_COL_DTB_INFO = 4;
  private static final int CSV_COL_CLUB = 5;
  private static final int CSV_COL_SCORE = 6;
  private static final int CSV_MIN_COLS = 7;
  private static final int DTB_INFO_PARTS_MIN = 2;
  private static final int JANUARY_MONTH = 1;
  private static final int YOUNGEST_AGE_GROUP = 11;

  private final RankingRepository rankingRepository;
  private final ImportHistoryRepository importHistoryRepository;

  public RankingImportService(
      RankingRepository rankingRepository, ImportHistoryRepository importHistoryRepository) {
    this.rankingRepository = rankingRepository;
    this.importHistoryRepository = importHistoryRepository;
  }

  @Transactional
  @EvictImportCaches
  public void importRankings(Path file) throws DuplicateImportError, IOException {
    var fileNamePart = file.getFileName();
    if (fileNamePart == null) throw new IllegalArgumentException("Path has no filename: " + file);
    var filename = fileNamePart.toString();
    var period = ImportService.extractPeriodFromFilename(filename);
    var category = ImportService.fileCategoryFromFilename(filename);
    var capitalizedCategory = Character.toUpperCase(category.charAt(0)) + category.substring(1);

    if (importHistoryRepository.existsByCategoryAndPeriod(capitalizedCategory, period)) {
      throw new DuplicateImportError(
          "Rankings for '" + category + "' / " + period + " have already been imported");
    }

    var ageGroup = AGE_GROUP_MAP.get(category);
    if (ageGroup == null) {
      throw new IllegalStateException("No age group mapping for category '" + category + "'");
    }
    storeFromCsv(file, period, ageGroup);

    var genderFactor = GENDER_FACTORS.get(category);
    if (genderFactor != null) {
      calculateRankings(period, genderFactor);
    }

    var now = LocalDateTime.now(ZoneOffset.UTC);
    importHistoryRepository.save(
        new ImportHistory(0L, filename, capitalizedCategory, period, now, now, now));
    log.info("Import of '{}' completed", filename);
  }

  private void storeFromCsv(Path file, LocalDate period, String ageGroup) throws IOException {
    var records = new ArrayList<Ranking>();
    var csvParser = new CSVParserBuilder().withSeparator(',').withQuoteChar('"').build();
    try (var reader =
        new CSVReaderBuilder(
                new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8))
            .withCSVParser(csvParser)
            .build()) {
      try {
        var parts = reader.readNext();
        while (parts != null) {
          if (parts.length >= CSV_MIN_COLS) {
            var dtbParts = parts[CSV_COL_DTB_INFO].trim().split(" ");
            if (dtbParts.length >= DTB_INFO_PARTS_MIN) {
              var dtbId = parseIntOrNull(dtbParts[0]);
              if (dtbId != null) {
                records.add(
                    new Ranking(
                        0L,
                        dtbId,
                        parts[CSV_COL_LASTNAME].trim(),
                        parts[CSV_COL_FIRSTNAME].trim(),
                        parts[CSV_COL_NATIONALITY].trim().isEmpty()
                            ? "nil"
                            : parts[CSV_COL_NATIONALITY].trim(),
                        ageGroup,
                        period,
                        parseIntOrZero(parts[CSV_COL_POSITION].trim()),
                        parts[CSV_COL_SCORE].trim(),
                        parts[CSV_COL_CLUB].trim(),
                        dtbParts[1],
                        false,
                        false,
                        false));
              }
            }
          }
          parts = reader.readNext();
        }
      } catch (CsvValidationException e) {
        throw new IOException("Failed to parse CSV: " + e.getMessage(), e);
      }
    }
    rankingRepository.saveAll(records);
    log.debug("Stored {} records from CSV", records.size());
  }

  private void calculateRankings(LocalDate period, int genderFactor) {
    // Pre-fetch all 'overall' rankings for this gender in one query instead of once per age group
    int dtbIdStart =
        genderFactor == RankingCoding.GENDER_FACTOR_JUNIOREN
            ? RankingCoding.MALE_DTB_ID_START
            : RankingCoding.FEMALE_DTB_ID_START;
    int dtbIdEnd =
        genderFactor == RankingCoding.GENDER_FACTOR_JUNIOREN
            ? RankingCoding.MALE_DTB_ID_END
            : RankingCoding.FEMALE_DTB_ID_END;
    var allOverall = rankingRepository.findForAgeRangeInPeriod(period, dtbIdStart, dtbIdEnd);

    var yobYoungest = String.valueOf(period.getYear() - YOUNGEST_AGE_GROUP);
    var allDerived = new ArrayList<Ranking>();

    for (int ageGroup = YOUNGEST_AGE_GROUP; ageGroup <= 18; ageGroup++) {
      var yobsGeneral =
          ImportService.yobRangeToFetch(yobYoungest, ageGroup, period, genderFactor).stream()
              .sorted()
              .toList();
      computeRankingsForAgeRange(yobsGeneral, ageGroup, false, false, false, allOverall, allDerived);

      int yobSingle = (period.getYear() - ageGroup) % 100 + genderFactor;
      computeRankingsForAgeRange(
          List.of(yobSingle), ageGroup, true, false, false, allOverall, allDerived);
    }

    for (int ageGroup : List.of(12, 14, 16, 18)) {
      int base = (period.getYear() - ageGroup) % 100 + genderFactor;
      var yobs = List.of(base, base + 1).stream().sorted().toList();
      computeRankingsForAgeRange(yobs, ageGroup, false, true, false, allOverall, allDerived);
    }

    if (period.getMonthValue() == JANUARY_MONTH) {
      var yeDate = period.minusDays(1);
      for (int ageGroup : List.of(12, 14, 16, 18)) {
        int base = (yeDate.getYear() - ageGroup) % 100 + genderFactor;
        var yobs = List.of(base, base + 1).stream().sorted().toList();
        computeRankingsForAgeRange(yobs, ageGroup, false, true, true, allOverall, allDerived);
      }
    }

    if (!allDerived.isEmpty()) {
      rankingRepository.saveAll(allDerived);
    }
  }

  private static void computeRankingsForAgeRange(
      List<Integer> classesToRetrieve,
      int ageGroup,
      boolean yobRanking,
      boolean ageGroupRanking,
      boolean yearEndRanking,
      List<Ranking> allOverall,
      List<Ranking> output) {

    if (classesToRetrieve.isEmpty()) return;
    int idStart = classesToRetrieve.getFirst() * RankingCoding.YOB_MULTIPLIER;
    int idEnd =
        classesToRetrieve.getLast() * RankingCoding.YOB_MULTIPLIER + RankingCoding.YOB_MULTIPLIER
            - 1;

    // Filter from pre-fetched list — preserves ranking_position ASC ordering from the DB query
    var rankingsInRange =
        allOverall.stream().filter(r -> r.dtbId() >= idStart && r.dtbId() <= idEnd).toList();

    int lastRank = 0;
    int startRanking = 0;
    int countUp = 1;

    for (var curr : rankingsInRange) {
      if (curr.rankingPosition() > lastRank) {
        lastRank = curr.rankingPosition();
        startRanking = countUp;
      }
      output.add(
          new Ranking(
              0L,
              curr.dtbId(),
              curr.lastname(),
              curr.firstname(),
              curr.nationality(),
              "U" + ageGroup,
              curr.date(),
              startRanking,
              curr.score(),
              curr.club(),
              curr.federation(),
              ageGroupRanking,
              yobRanking,
              yearEndRanking));
      if ("GER".equals(curr.nationality()) && !SPECIAL_SCORES.contains(curr.score())) {
        countUp++;
      }
    }
  }

  @Nullable
  private static Integer parseIntOrNull(String s) {
    try {
      return Integer.parseInt(s.trim());
    } catch (NumberFormatException _) {
      return null;
    }
  }

  private static int parseIntOrZero(String s) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException _) {
      return 0;
    }
  }
}
