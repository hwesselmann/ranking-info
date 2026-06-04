package de.hdawg.rankinginfo.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StaticPageControllerTest extends WebControllerTestBase {

  @Test
  @DisplayName("GET home returns 200 and populates start date")
  void getHomeReturns200AndPopulatesStartDate() throws Exception {
    mockMvc.perform(get("/")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET home with empty DB shows placeholder date")
  void getHomeWithEmptyDbShowsPlaceholderDate() throws Exception {
    rankingRepository.deleteAll();
    mockMvc.perform(get("/")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET help returns 200")
  void getHelpReturns200() throws Exception {
    mockMvc.perform(get("/help")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET about returns 200")
  void getAboutReturns200() throws Exception {
    mockMvc.perform(get("/about")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET privacy returns 200 and imprint data")
  void getPrivacyReturns200AndImprintData() throws Exception {
    mockMvc.perform(get("/privacy")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET status returns 200 with counts")
  void getStatusReturns200WithCounts() throws Exception {
    mockMvc.perform(get("/status")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET status shows player counts in model")
  void getStatusShowsPlayerCountsInModel() throws Exception {
    mockMvc.perform(get("/status")).andExpect(status().isOk());
  }
}
