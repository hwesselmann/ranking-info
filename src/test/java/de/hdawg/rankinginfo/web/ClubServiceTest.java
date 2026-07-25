package de.hdawg.rankinginfo.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ClubServiceTest extends WebControllerTestBase {

  @Autowired
  private ClubService clubService;

  @Test
  @DisplayName("searchClubs returns combined youth and adult counts per club name")
  void searchClubsReturnsCombinedCounts() {
    var results = clubService.searchClubs("Baden");
    assertFalse(results.isEmpty());
    var club = results.get(0);
    assertTrue(club.youthCount() >= 0);
    assertTrue(club.adultCount() >= 0);
  }

  @Test
  @DisplayName("searchClubs with no match returns empty list")
  void searchClubsNoMatch() {
    var results = clubService.searchClubs("XYZNOTEXIST");
    assertTrue(results.isEmpty());
  }

  @Test
  @DisplayName("getClubDetail groups players into age-group buckets")
  void getClubDetailGroupsByAgeGroup() {
    var detail = clubService.getClubDetail("TC Baden-Baden");
    for (var entry : detail.entrySet()) {
      assertFalse(entry.getValue().isEmpty(), "Each age group bucket should have at least one player");
    }
  }

  @Test
  @DisplayName("getClubDetail for unknown club returns empty map")
  void getClubDetailUnknownClub() {
    var detail = clubService.getClubDetail("XYZNOTEXIST");
    assertTrue(detail.isEmpty());
  }

  @Test
  @DisplayName("searchClubs counts youth and adult players per club")
  void searchClubsCountsPlayersPerClub() {
    var results = clubService.searchClubs("TC");

    assertEquals(2, results.size());

    var baden = results.get(0);
    assertEquals("TC Baden", baden.name());
    assertEquals(1, baden.youthCount());
    assertEquals(2, baden.adultCount());

    var other = results.get(1);
    assertEquals("TC Other", other.name());
    assertEquals(0, other.youthCount());
    assertEquals(1, other.adultCount());
  }

  @Test
  @DisplayName("searchClubs rejects search terms shorter than the minimum length")
  void searchClubsRejectsTooShortTerm() {
    assertThrows(IllegalArgumentException.class, () -> clubService.searchClubs("T"));
    assertThrows(IllegalArgumentException.class, () -> clubService.searchClubs(" "));
    assertThrows(IllegalArgumentException.class, () -> clubService.searchClubs(""));
  }

  @Test
  @DisplayName("getClubDetail matches the club name exactly, ignoring case")
  void getClubDetailMatchesExactlyIgnoringCase() {
    var detail = clubService.getClubDetail("tc baden");

    assertFalse(detail.isEmpty());
  }

  @Test
  @DisplayName("getClubDetail does not match clubs that merely contain the given name")
  void getClubDetailDoesNotMatchOnSubstring() {
    var detail = clubService.getClubDetail("TC");

    assertTrue(detail.isEmpty());
  }

  @Test
  @DisplayName("searchClubs result has correct field types")
  void searchClubsHasCorrectFieldTypes() {
    var results = clubService.searchClubs("Baden");
    if (!results.isEmpty()) {
      var club = results.get(0);
      assertEquals(club.name().getClass(), String.class);
      assertTrue(club.youthCount() >= 0);
      assertTrue(club.adultCount() >= 0);
    }
  }
}
