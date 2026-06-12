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

@SuppressWarnings("PMD.GuardLogStatement")
@Service
public class ImportService {

  private static final Logger log = LoggerFactory.getLogger(ImportService.class);

  private static final int FILENAME_PARTS_MIN = 2;

  private final RankingImportService rankingImportService;

  public ImportService(RankingImportService rankingImportService) {
    this.rankingImportService = rankingImportService;
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
      case "Junioren" -> "junioren";
      case "Juniorinnen" -> "juniorinnen";
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

  public void scanAndImport(String folderPath) {
    doScanAndImport(folderPath, null);
  }

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
        rankingImportService.importRankings(csvFile.toPath());
        log.info("Imported '{}'", csvFile.getName());
      } catch (DuplicateImportError _) {
        log.info("Skipping '{}' (already imported)", csvFile.getName());
      } catch (Exception e) {
        var logFile = errorLogPath != null ? errorLogPath : folderPath + "/error.log";
        writeErrorLog(logFile, csvFile.getName() + ": " + e.getMessage());
        log.error("Error importing '{}': {}", csvFile.getName(), e.getMessage());
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
