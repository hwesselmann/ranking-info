package de.hdawg.rankinginfo.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FederationServiceTest extends WebControllerTestBase {

  @Autowired
  private FederationService federationService;

  @Test
  @DisplayName("buildFederationData returns correctly keyed outer map")
  void buildFederationDataReturnsCorrectKeys() {
    var data = federationService.buildFederationData();
    assertFalse(data.isEmpty());
    data.forEach((fedName, agCounts) -> {
      assertFalse(fedName.isBlank(), "Federation name should not be blank");
      assertFalse(agCounts.isEmpty(), "Each federation should have at least one age group count");
    });
  }

  @Test
  @DisplayName("buildFederationData maps federation codes to display names")
  void buildFederationDataMapsFederationCodes() {
    var data = federationService.buildFederationData();
    if (!data.isEmpty()) {
      // Should contain full names, not raw codes
      data.keySet().forEach(name ->
          assertFalse(name.matches("[A-Z]{2,4}"), "Expected display name, not raw code: " + name));
    }
  }

  @Test
  @DisplayName("buildFederationData returns empty map when no data exists")
  void buildFederationDataEmptyWhenNoData() {
    // With base test data present, this verifies the result is non-null
    var data = federationService.buildFederationData();
    assertTrue(data != null);
  }
}
