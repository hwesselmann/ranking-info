package de.hdawg.rankinginfo.web;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ListingControllerTest extends WebControllerTestBase {

  @BeforeEach
  @Override
  protected void seed() {
    super.seed();
    rankingRepository.save(r(10_711_113, "Juniormann3", "Max", "U14", q, 2, "90", true, false));
  }

  @Test
  @DisplayName("GET listings returns 200 without commit")
  void getListingsReturns200WithoutCommit() throws Exception {
    mockMvc.perform(get("/listings")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET listings with Herren commit returns rankings")
  void getListingsWithHerrenCommitReturnsRankings() throws Exception {
    mockMvc
        .perform(
            get("/listings")
                .param("quarter", q.toString())
                .param("gender", "Herren")
                .param("commit", "1"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET listings with Damen commit returns female rankings")
  void getListingsWithDamenCommitReturnsFemaleRankings() throws Exception {
    mockMvc
        .perform(
            get("/listings")
                .param("quarter", q.toString())
                .param("gender", "Damen")
                .param("commit", "1"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET listings with Junioren and age group")
  void getListingsWithJuniorenAndAgeGroup() throws Exception {
    mockMvc
        .perform(
            get("/listings")
                .param("quarter", q.toString())
                .param("gender", "Junioren")
                .param("age_group", "U14")
                .param("commit", "1"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET listings with Juniorinnen no age group uses overall")
  void getListingsWithJuniorinnenNoAgeGroupUsesOverall() throws Exception {
    mockMvc
        .perform(
            get("/listings")
                .param("quarter", q.toString())
                .param("gender", "Juniorinnen")
                .param("commit", "1"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET listings with federation filter")
  void getListingsWithFederationFilter() throws Exception {
    mockMvc
        .perform(
            get("/listings")
                .param("quarter", q.toString())
                .param("gender", "Herren")
                .param("federation", "BAD")
                .param("commit", "1"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET listings with club filter")
  void getListingsWithClubFilter() throws Exception {
    mockMvc
        .perform(
            get("/listings")
                .param("quarter", q.toString())
                .param("gender", "Herren")
                .param("club", "Baden")
                .param("commit", "1"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET listings populates quarters and federations model")
  void getListingsPopulatesQuartersAndFederationsModel() throws Exception {
    mockMvc.perform(get("/listings")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("empty age_group_options uses default even-odd logic, not cumulative")
  void emptyAgeGroupOptionsUsesDefaultEvenOddLogicNotCumulative() throws Exception {
    mockMvc
        .perform(
            get("/listings")
                .param("quarter", q.toString())
                .param("gender", "Junioren")
                .param("age_group", "U14")
                .param("age_group_options", "")
                .param("commit", "1"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("rankings", hasSize(1)));
  }
}
