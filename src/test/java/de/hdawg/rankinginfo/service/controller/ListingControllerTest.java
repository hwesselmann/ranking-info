package de.hdawg.rankinginfo.service.controller;

import de.hdawg.rankinginfo.service.actuator.ListingInfoContributor;
import de.hdawg.rankinginfo.service.exception.RankingPeriodException;
import de.hdawg.rankinginfo.service.services.ListingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ListingControllerTest {

  @Mock
  ListingInfoContributor listingInfoContributor;

  @Mock
  ListingService listingService;

  @InjectMocks
  ListingController sut = new ListingController(listingInfoContributor, listingService);

  @DisplayName("check if mapping of input to ranking period works")
  @Test
  void testMappingOfRankingPeriod() {
    assertEquals(LocalDate.of(2022, 3, 31), sut.checkAndMapRankingPeriod("2022-03-31"));
    assertEquals(LocalDate.of(2022, 6, 30), sut.checkAndMapRankingPeriod("2022-06-30"));
    assertEquals(LocalDate.of(2019, 9, 30), sut.checkAndMapRankingPeriod("2019-09-30"));
    assertEquals(LocalDate.of(2019, 12, 31), sut.checkAndMapRankingPeriod("2019-12-31"));
    assertNotEquals(LocalDate.of(2019, 6, 30), sut.checkAndMapRankingPeriod("2019-12-31"));
  }

  @DisplayName("check if invalid input results in an exception")
  @Test
  void testExceptionOnInvalidInput() {
    Throwable exception = assertThrows(RankingPeriodException.class, () -> sut.checkAndMapRankingPeriod("2018-03-30"));
    assertEquals("the year for the requested ranking period is not valid", exception.getMessage());

    exception = assertThrows(RankingPeriodException.class, () -> sut.checkAndMapRankingPeriod("10000-03-30"));
    assertEquals("the year for the requested ranking period is not valid", exception.getMessage());

    exception = assertThrows(RankingPeriodException.class, () -> sut.checkAndMapRankingPeriod("2019-01-30"));
    assertEquals("requested period is not a valid ranking period", exception.getMessage());

    exception = assertThrows(RankingPeriodException.class, () -> sut.checkAndMapRankingPeriod("2019-03-30"));
    assertEquals("the requested ranking period start with an invalid day", exception.getMessage());

    exception = assertThrows(RankingPeriodException.class, () -> sut.checkAndMapRankingPeriod("2019-12-30"));
    assertEquals("the requested ranking period start with an invalid day", exception.getMessage());

    exception = assertThrows(RankingPeriodException.class, () -> sut.checkAndMapRankingPeriod("2019-06-31"));
    assertEquals("the requested ranking period start with an invalid day", exception.getMessage());
  }

  @DisplayName("check correct mapping of modifiers")
  @Test
  void checkModifierMapping() {
    Map<String, Boolean> result = sut.mapModifier("yob");
    assertTrue(result.get("yob"));
    assertFalse(result.get("overall"));
    assertFalse(result.get("endofyear"));

    result = sut.mapModifier("overall");
    assertFalse(result.get("yob"));
    assertTrue(result.get("overall"));
    assertFalse(result.get("endofyear"));

    result = sut.mapModifier("endofyear");
    assertFalse(result.get("yob"));
    assertFalse(result.get("overall"));
    assertTrue(result.get("endofyear"));

    result = sut.mapModifier("");
    assertFalse(result.get("yob"));
    assertFalse(result.get("overall"));
    assertFalse(result.get("endofyear"));

    result = sut.mapModifier(" ");
    assertFalse(result.get("yob"));
    assertFalse(result.get("overall"));
    assertFalse(result.get("endofyear"));

    assertThrows(IllegalStateException.class, () -> sut.mapModifier("invalid"));
  }
}
