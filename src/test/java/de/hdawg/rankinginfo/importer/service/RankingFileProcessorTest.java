package de.hdawg.rankinginfo.importer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.hdawg.rankinginfo.importer.exception.FilenameFormatException;
import de.hdawg.rankinginfo.importer.exception.UnknownNationalityException;
import de.hdawg.rankinginfo.model.Federation;
import de.hdawg.rankinginfo.model.Nationality;
import de.hdawg.rankinginfo.model.Ranking;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RankingFileProcessorTest {

  @Test
  @DisplayName("make sure player data was completely loaded from import file")
  void makeSureAllPlayerDataWasLoaded() {
    RankingFileProcessor sut = new RankingFileProcessor();
    int expected = 1691;
    List<Ranking> actual = sut.readRankingsFromImportFile(
        "src/test/resources/AlphaGesamtranglisteJuniorenfuerJugendturnierveranstalter_20221001.csv");

    assertEquals(expected, actual.size());
    assertEquals(Nationality.NIL, actual.get(8).getNationality());
    assertEquals(Nationality.NIL, actual.get(10).getNationality());
    assertEquals(Federation.WTB, actual.get(0).getFederation());
    assertEquals(Nationality.GER, actual.get(640).getNationality());
    assertEquals("1184,0", actual.get(876).getPoints());
  }

  @Test
  @DisplayName("ensure the right period is extracted from the filename")
  void verifyCorrectSplittingOfFileName() {
    RankingFileProcessor sut = new RankingFileProcessor();
    LocalDate expected = LocalDate.of(2022, 10, 1);
    LocalDate actual = sut.getPeriodFromFilename(
        "AlphaGesamtranglisteJuniorenfuerJugendturnierveranstalter_20221001.csv");

    assertEquals(expected, actual);
  }

  @Test
  @DisplayName("ensure an error is thrown when the filename does not contain a date part")
  void ensureExceptionIsThrownWhenThereIsNoDatePart() {
    RankingFileProcessor sut = new RankingFileProcessor();

    Throwable exception = assertThrows(FilenameFormatException.class,
        () -> sut.getPeriodFromFilename(
            "AlphaGesamtranglisteJuniorenfuerJugendturnierveranstalter.csv"));
    assertEquals("No date could be found when inspecting the import file", exception.getMessage());
  }

  @Test
  @DisplayName("ensure an error is thrown when the date part is an invalid date")
  void ensureExceptionIsThrownWhenTheDateIsInvalid() {
    RankingFileProcessor sut = new RankingFileProcessor();

    Throwable exception = assertThrows(FilenameFormatException.class,
        () -> sut.getPeriodFromFilename(
            "AlphaGesamtranglisteJuniorenfuerJugendturnierveranstalter_19990101.csv"));
    assertEquals("No valid date could be extracted from the file", exception.getMessage());

    exception = assertThrows(FilenameFormatException.class, () -> sut.getPeriodFromFilename(
        "AlphaGesamtranglisteJuniorenfuerJugendturnierveranstalter_20330301.csv"));
    assertEquals("No valid date could be extracted from the file", exception.getMessage());

    exception = assertThrows(FilenameFormatException.class, () -> sut.getPeriodFromFilename(
        "AlphaGesamtranglisteJuniorenfuerJugendturnierveranstalter_20330431.csv"));
    assertEquals("No valid date could be extracted from the file", exception.getMessage());
  }

  @Test
  @DisplayName("ensure an error is thrown when an unknown nationality is imported")
  void ensureExceptionIsThrownWhenAnUnknownNationalityIsEncountered() {
    RankingFileProcessor sut = new RankingFileProcessor();

    Throwable exception = assertThrows(UnknownNationalityException.class,
        () -> sut.readRankingsFromImportFile(
            "src/test/resources/unknown-nationality-AlphaGesamtranglisteJuniorenfuerJugendturnierveranstalter_20221001.csv"));
    assertEquals("the given nationality XYZ is not mappable in the current version.",
        exception.getMessage());
  }
}
