package de.hdawg.rankinginfo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CsvParsingTest {

  @Test
  @DisplayName("simple row without quotes is split on commas")
  void simpleRowWithoutQuotes() throws IOException {
    var line = "1,Mustermann,Max,GER,10234567 HH,TC Hamburg,850";
    var parts = ImportService.parseCsvLine(line);
    assertEquals(7, parts.length);
    assertEquals("1", parts[0]);
    assertEquals("Mustermann", parts[1]);
    assertEquals("Max", parts[2]);
    assertEquals("GER", parts[3]);
    assertEquals("10234567 HH", parts[4]);
    assertEquals("TC Hamburg", parts[5]);
    assertEquals("850", parts[6]);
  }

  @Test
  @DisplayName("quoted decimal score is delivered without surrounding quotes")
  void quotedDecimalScore() throws IOException {
    var line = "3,Schmidt,Anna,GER,20345678 BY,SC München,\"66,9\"";
    var parts = ImportService.parseCsvLine(line);
    assertEquals(7, parts.length);
    assertEquals("66,9", parts[6]);
  }

  @Test
  @DisplayName("club name with internal comma inside quotes is not split")
  void clubNameWithCommaInsideQuotes() throws IOException {
    var line = "5,Weber,Klaus,GER,10567890 NRW,\"TC Rot-Weiss, Köln\",720";
    var parts = ImportService.parseCsvLine(line);
    assertEquals(7, parts.length);
    assertEquals("TC Rot-Weiss, Köln", parts[5]);
    assertEquals("720", parts[6]);
  }
}
