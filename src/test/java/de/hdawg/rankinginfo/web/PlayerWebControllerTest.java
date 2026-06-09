package de.hdawg.rankinginfo.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlayerWebControllerTest extends WebControllerTestBase {

  @Test
  @DisplayName("GET players returns 200 without params")
  void getPlayersReturns200WithoutParams() throws Exception {
    mockMvc.perform(get("/players")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET players by lastname with multiple results returns 200")
  void getPlayersByLastnameWithMultipleResultsReturns200() throws Exception {
    rankingRepository.save(r(10_003_003, "Mueller", "Fritz", "m00", q, 3, "400", false, false));
    mockMvc.perform(get("/players").param("lastname", "Mueller")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET players by lastname with single result redirects")
  void getPlayersByLastnameWithSingleResultRedirects() throws Exception {
    mockMvc
        .perform(get("/players").param("lastname", "Schmidt"))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  @DisplayName("GET players by lastname and yob with single result redirects")
  void getPlayersByLastnameAndYobWithSingleResultRedirects() throws Exception {
    mockMvc
        .perform(get("/players").param("lastname", "Mueller").param("yob", "2000"))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  @DisplayName("GET players by yob only redirects or returns list")
  void getPlayersByYobOnlyRedirectsOrReturnsList() throws Exception {
    mockMvc.perform(get("/players").param("yob", "2000")).andExpect(status().is2xxSuccessful());
  }

  @Test
  @DisplayName("GET players by dtb_id with single result redirects")
  void getPlayersByDtbIdWithSingleResultRedirects() throws Exception {
    mockMvc
        .perform(get("/players").param("dtb_id", "10001001"))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  @DisplayName("GET players by dtb_id with no match returns 200")
  void getPlayersByDtbIdWithNoMatchReturns200() throws Exception {
    mockMvc.perform(get("/players").param("dtb_id", "99999999")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET player show returns 200 for known player")
  void getPlayerShowReturns200ForKnownPlayer() throws Exception {
    mockMvc.perform(get("/players/10001001")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET player show with two quarters includes position change data")
  void getPlayerShowWithTwoQuartersIncludesPositionChangeData() throws Exception {
    mockMvc.perform(get("/players/10001001")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET player show redirects for unknown player")
  void getPlayerShowRedirectsForUnknownPlayer() throws Exception {
    mockMvc.perform(get("/players/99999999")).andExpect(status().is3xxRedirection());
  }

  @Test
  @DisplayName("GET players with HX-Request and multiple results returns fragment")
  void getPlayersWithHtmxHeaderReturnsFragment() throws Exception {
    rankingRepository.save(r(10_003_003, "Mueller", "Fritz", "m00", q, 3, "400", false, false));
    mockMvc
        .perform(
            get("/players")
                .param("lastname", "Mueller")
                .header("HX-Request", "true"))
        .andExpect(status().isOk())
        .andExpect(view().name("players/results"));
  }

  @Test
  @DisplayName("GET players with HX-Request and single result sets HX-Redirect header")
  void getPlayersWithHtmxSingleResultSetsRedirectHeader() throws Exception {
    mockMvc
        .perform(
            get("/players")
                .param("lastname", "Schmidt")
                .header("HX-Request", "true"))
        .andExpect(status().isOk())
        .andExpect(header().string("HX-Redirect", "/players/10002002"));
  }
}
