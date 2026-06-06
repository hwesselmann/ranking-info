package de.hdawg.rankinginfo.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClubControllerTest extends WebControllerTestBase {

  @Test
  @DisplayName("GET clubs returns 200 without params")
  void getClubsReturns200WithoutParams() throws Exception {
    mockMvc.perform(get("/clubs")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET clubs with commit and name returns club list")
  void getClubsWithCommitAndNameReturnsClubList() throws Exception {
    mockMvc
        .perform(get("/clubs").param("club", "Baden").param("commit", "1"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET clubs search with no match returns empty clubs")
  void getClubsSearchWithNoMatchReturnsEmptyClubs() throws Exception {
    mockMvc
        .perform(get("/clubs").param("club", "XYZNOTEXIST").param("commit", "1"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET club show returns 200")
  void getClubShowReturns200() throws Exception {
    mockMvc.perform(get("/clubs/TC Baden")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET club show for unknown club returns empty players")
  void getClubShowForUnknownClubReturnsEmptyPlayers() throws Exception {
    mockMvc.perform(get("/clubs/UNKNOWN CLUB")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET clubs with HX-Request header returns results fragment")
  void getClubsWithHtmxHeaderReturnsFragment() throws Exception {
    mockMvc
        .perform(
            get("/clubs")
                .param("club", "Baden")
                .param("commit", "1")
                .header("HX-Request", "true"))
        .andExpect(status().isOk())
        .andExpect(view().name("clubs/index :: results"));
  }
}
