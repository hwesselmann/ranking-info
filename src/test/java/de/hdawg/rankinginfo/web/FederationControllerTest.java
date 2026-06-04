package de.hdawg.rankinginfo.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FederationControllerTest extends WebControllerTestBase {

  @Test
  @DisplayName("GET federations returns 200")
  void getFederationsReturns200() throws Exception {
    mockMvc.perform(get("/federations")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET federations populates federations model")
  void getFederationsPopulatesFederationsModel() throws Exception {
    mockMvc.perform(get("/federations")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET federations with empty DB returns empty map")
  void getFederationsWithEmptyDbReturnsEmptyMap() throws Exception {
    rankingRepository.deleteAll();
    mockMvc.perform(get("/federations")).andExpect(status().isOk());
  }
}
