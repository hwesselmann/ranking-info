package de.hdawg.rankinginfo.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
public class ImportService {

  private static final Logger log = LoggerFactory.getLogger(ImportService.class);

  private static final String CATEGORY_JUNIOREN = "junioren";
  private static final String CATEGORY_JUNIORINNEN = "juniorinnen";

  private static final Set<String> SPECIAL_SCORES = Set.of("0,0", "PR", "Einst.");
  private static final Map<String, Integer> GENDER_FACTORS =
      Map.of(
          CATEGORY_JUNIOREN, RankingCoding.GENDER_FACTOR_JUNIOREN,
          CATEGORY_JUNIORINNEN, RankingCoding.GENDER_FACTOR_JUNIORINNEN);
  private static final Map<String, String> AGE_GROUP_MAP =
      Map.of("herren", "m00", "damen", "w00", CATEGORY_JUNIOREN, "overall", CATEGORY_JUNIORINNEN, "overall");

  private static final int CSV_COL_POSITION = 0;
  private static final int CSV_COL_LASTNAME = 1;
  private static final int CSV_COL_FIRSTNAME = 2;
  private static final int CSV_COL_NATIONALITY = 3;
  private static final int CSV_COL_DTB_INFO = 4;
  private static final int CSV_COL_CLUB = 5;
  private static final int CSV_COL_SCORE = 6;
  private static final int CSV_MIN_COLS = 7;
  private static final int FILENAME_PARTS_MIN = 2;
  private static final int DTB_INFO_PARTS_MIN = 2;
  private static final int JANUARY_MONTH = 1;
  private static final int YOUNGEST_AGE_GROUP = 11;

  private final RankingRepository rankingRepository;
  private final ImportHistoryRepository importHistoryRepository;

  public ImportService(
      RankingRepository rankingRepository, ImportHistoryRepository importHistoryRepository) {
    this.rankingRepository = rankingRepository;
    this.importHistoryRepository = importHistoryRepository;
  }

  public static LocalDate extractPeriodFromFilename(String filename) {
    var name = new File(filename).getName();
    int dotIdx = name.lastIndexOf('.');
    if (dotIdx > 0) name = name.substring(0, dotIdx);
    var parts = name.split("_");
    if (parts.length < FILENAME_PARTS_MIN) {
      throw new IllegalArgumentException(
          "Could not retrieve period part from filename '" + filename + "'");
    }
    return LocalDate.parse(parts[parts.length - 1], DateTimeFormatter.ofPattern("yyyyMMdd"));
  }

  public static String fileCategoryFromFilename(String filename) {
    var name = new File(filename).getName();
    int dotIdx = name.lastIndexOf('.');
    if (dotIdx > 0) name = name.substring(0, dotIdx);
    var prefix = name.split("_")[0];
    return switch (prefix) {
      case "Junioren" -> CATEGORY_JUNIOREN;
      case "Juniorinnen" -> CATEGORY_JUNIORINNEN;
      case "Herren" -> "herren";
      case "Damen" -> "damen";
      default ->
          throw new IllegalArgumentException(
              "Unknown file category in filename '" + filename + "'");
    };
  }

  public static List<Integer> yobRangeToFetch(
      String yob, int ageGroup, LocalDate period, int genderFactor) {
    var classes = new ArrayList<Integer>();
    int yobGenderMarker = Integer.parseInt(yob.substring(2, 4)) + genderFactor;
    int i = 0;
    while (ageGroup >= period.getYear() - Integer.parseInt(yob) + i) {
      classes.add(yobGenderMarker - i);
      i++;
    }
    return classes;
  }

  @Transactional
  @EvictImportCaches
  public void importRankings(Path file) throws DuplicateImportError, IOException {
    var fileNamePart = file.getFileName();
    if (fileNamePart == null) throw new IllegalArgumentException("Path has no filename: " + file);
    var filename = fileNamePart.toString();
    var period = extractPeriodFromFilename(filename);
    var category = fileCategoryFromFilename(filename);
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

  @EvictImportCaches
  public void scanAndImport(String folderPath) {
    doScanAndImport(folderPath, null);
  }

  @EvictImportCaches
  public void scanAndImport(String folderPath, @Nullable String errorLogPath) {
    doScanAndImport(folderPath, errorLogPath);
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  private void doScanAndImport(String folderPath, @Nullable String errorLogPath) {
    var folder = new File(folderPath);
    if (!folder.isDirectory()) {
      log.warn("Import folder '{}' does not exist or is not a directory", folderPath);
      return;
    }
    var csvFiles = folder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".csv"));
    if (csvFiles == null) return;
    for (var csvFile : csvFiles) {
      try {
        importRankings(csvFile.toPath());
        log.info("Imported '{}'", csvFile.getName());
      } catch (DuplicateImportError e) {
        log.info("Skipping '{}' (already imported)", csvFile.getName());
      } catch (Exception e) {
        var logFile = errorLogPath != null ? errorLogPath : folderPath + "/error.log";
        writeErrorLog(logFile, csvFile.getName() + ": " + e.getMessage());
        log.error("Error importing '{}': {}", csvFile.getName(), e.getMessage());
      }
    }
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
    var yobYoungest = String.valueOf(period.getYear() - YOUNGEST_AGE_GROUP);

    for (int ageGroup = YOUNGEST_AGE_GROUP; ageGroup <= 18; ageGroup++) {
      var yobsGeneral =
          yobRangeToFetch(yobYoungest, ageGroup, period, genderFactor).stream()
              .sorted()
              .toList();
      rankingsForAgeRange(yobsGeneral, period, ageGroup, false, false, false);

      int yobSingle =
          (period.getYear() - ageGroup) % 100 + genderFactor;
      rankingsForAgeRange(List.of(yobSingle), period, ageGroup, true, false, false);
    }

    for (int ageGroup : List.of(12, 14, 16, 18)) {
      int base = (period.getYear() - ageGroup) % 100 + genderFactor;
      var yobs = List.of(base, base + 1).stream().sorted().toList();
      rankingsForAgeRange(yobs, period, ageGroup, false, true, false);
    }

    if (period.getMonthValue() == JANUARY_MONTH) {
      var yeDate = period.minusDays(1);
      for (int ageGroup : List.of(12, 14, 16, 18)) {
        int base = (yeDate.getYear() - ageGroup) % 100 + genderFactor;
        var yobs = List.of(base, base + 1).stream().sorted().toList();
        rankingsForAgeRange(yobs, period, ageGroup, false, true, true);
      }
    }
  }

  private void rankingsForAgeRange(
      List<Integer> classesToRetrieve,
      LocalDate period,
      int ageGroup,
      boolean yobRanking,
      boolean ageGroupRanking,
      boolean yearEndRanking) {

    int idStart = classesToRetrieve.get(0) * RankingCoding.YOB_MULTIPLIER;
    int idEnd =
        classesToRetrieve.get(classesToRetrieve.size() - 1) * RankingCoding.YOB_MULTIPLIER
            + RankingCoding.YOB_MULTIPLIER - 1;
    var rankingsFromDb = rankingRepository.findForAgeRangeInPeriod(period, idStart, idEnd);

    int lastRank = 0;
    int startRanking = 0;
    int countUp = 1;
    var records = new ArrayList<Ranking>();

    for (var curr : rankingsFromDb) {
      if (curr.rankingPosition() > lastRank) {
        lastRank = curr.rankingPosition();
        startRanking = countUp;
      }
      records.add(
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

    if (!records.isEmpty()) {
      rankingRepository.saveAll(records);
    }
  }

  private static void writeErrorLog(String logFilePath, String message) {
    var logFile = new File(logFilePath);
    var separator = logFile.exists() ? "\n\n" : "";
    try (var writer = new FileWriter(logFile, StandardCharsets.UTF_8, true)) {
      writer.write(separator + message);
    } catch (IOException e) {
      log.warn("Could not write to error log '{}': {}", logFilePath, e.getMessage());
    }
  }

  static String[] parseCsvLine(String line) throws IOException {
    return new CSVParserBuilder().withSeparator(',').withQuoteChar('"').build().parseLine(line);
  }

  @Nullable
  private static Integer parseIntOrNull(String s) {
    try {
      return Integer.parseInt(s.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static int parseIntOrZero(String s) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return 0;
    }
  }
}
