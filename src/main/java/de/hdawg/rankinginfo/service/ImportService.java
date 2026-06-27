package de.hdawg.rankinginfo.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.opencsv.CSVParserBuilder;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/// Scans a folder for ranking CSV files and hands each one to [RankingImportService] for
/// parsing and persistence, logging successes and failures along the way.
///
/// Filenames are expected to follow the `{Category}_{yyyyMMdd}.csv` convention (e.g.
/// `Herren_20240101.csv`); [#extractPeriodFromFilename(String)] and
/// [#fileCategoryFromFilename(String)] decode that convention, and are also used by
/// [RankingImportService] itself.
@SuppressWarnings("PMD.GuardLogStatement")
@Service
public class ImportService {

  private static final Logger log = LoggerFactory.getLogger(ImportService.class);

  private static final int FILENAME_PARTS_MIN = 2;

  private final RankingImportService rankingImportService;

  public ImportService(RankingImportService rankingImportService) {
    this.rankingImportService = rankingImportService;
  }

  /// Extracts the ranking period (quarter date) from a `{Category}_{yyyyMMdd}.csv` filename.
  ///
  /// @param filename the CSV filename (path is ignored, only the base name is used)
  /// @return the date encoded in the filename's trailing `yyyyMMdd` segment
  /// @throws IllegalArgumentException if the filename has no `_`-separated date segment
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

  /// Extracts the lowercase file category (`herren`/`damen`/`junioren`/`juniorinnen`) from a
  /// `{Category}_{yyyyMMdd}.csv` filename.
  ///
  /// @param filename the CSV filename (path is ignored, only the base name is used)
  /// @return the lowercased category
  /// @throws IllegalArgumentException if the filename's category prefix is not one of the four
  ///     known categories
  public static String fileCategoryFromFilename(String filename) {
    var name = new File(filename).getName();
    int dotIdx = name.lastIndexOf('.');
    if (dotIdx > 0) name = name.substring(0, dotIdx);
    var prefix = name.split("_")[0];
    return switch (prefix) {
      case "Junioren" -> "junioren";
      case "Juniorinnen" -> "juniorinnen";
      case "Herren" -> "herren";
      case "Damen" -> "damen";
      default ->
          throw new IllegalArgumentException(
              "Unknown file category in filename '" + filename + "'");
    };
  }

  /// Computes the YOB+gender marker values (matching the leading digits of
  /// [RankingCoding#YOB_MULTIPLIER]-encoded DTB IDs) for every birth year whose age in `period`
  /// is at most `ageGroup`, starting from the birth year encoded by `yob`.
  ///
  /// Used to gather the cumulative (`age <= ageGroup`) set of birth-year cohorts for a junior
  /// age bracket, as opposed to a single exact-age cohort.
  ///
  /// @param yob the birth year of the youngest cohort to include, as a 4-digit string (e.g.
  ///     `"2013"`)
  /// @param ageGroup the upper age bound (inclusive) of the bracket being resolved
  /// @param period the ranking quarter date ages are computed against
  /// @param genderFactor [RankingCoding#GENDER_FACTOR_JUNIOREN] or
  ///     [RankingCoding#GENDER_FACTOR_JUNIORINNEN]
  /// @return YOB+gender markers for each included birth year, unsorted
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

  /// Scans `folderPath` for CSV and PDF ranking files and imports each one, logging failures to
  /// `folderPath + "/error.log"`.
  ///
  /// @param folderPath directory to scan for `.csv` and `.pdf` files; a no-op if it doesn't
  ///     exist or isn't a directory
  public void scanAndImport(String folderPath) {
    doScanAndImport(folderPath, null);
  }

  /// Scans `folderPath` for CSV and PDF ranking files and imports each one, logging failures to
  /// `errorLogPath` (or `folderPath + "/error.log"` if `null`).
  ///
  /// @param folderPath directory to scan for `.csv` and `.pdf` files; a no-op if it doesn't
  ///     exist or isn't a directory
  /// @param errorLogPath file to append import failures to, or `null` to use the default
  ///     location inside `folderPath`
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
    var rankingFiles =
        folder.listFiles(
            (dir, name) -> {
              var lower = name.toLowerCase(Locale.ROOT);
              return lower.endsWith(".csv") || lower.endsWith(".pdf");
            });
    if (rankingFiles == null) return;
    for (var rankingFile : rankingFiles) {
      try {
        rankingImportService.importRankings(rankingFile.toPath());
        log.info("Imported '{}'", rankingFile.getName());
      } catch (DuplicateImportError _) {
        log.info("Skipping '{}' (already imported)", rankingFile.getName());
      } catch (Exception e) {
        var logFile = errorLogPath != null ? errorLogPath : folderPath + "/error.log";
        writeErrorLog(logFile, rankingFile.getName() + ": " + e.getMessage());
        log.error("Error importing '{}': {}", rankingFile.getName(), e.getMessage());
      }
    }
  }

  static String[] parseCsvLine(String line) throws IOException {
    return new CSVParserBuilder().withSeparator(',').withQuoteChar('"').build().parseLine(line);
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
}
