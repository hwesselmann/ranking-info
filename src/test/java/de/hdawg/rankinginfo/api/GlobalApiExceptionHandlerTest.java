package de.hdawg.rankinginfo.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class GlobalApiExceptionHandlerTest {

  @Autowired MockMvc mockMvc;

  private static final String AUTH = "Bearer test-api-token";

  @Test
  @DisplayName("invalid date in listings path triggers 500 with error JSON body")
  void invalidDateReturns500WithErrorBody() throws Exception {
    mockMvc
        .perform(get("/api/v1/listings/not-a-date/m00").header("Authorization", AUTH))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("Internal Server Error"));
  }

  @Test
  @DisplayName("valid request to listings with no data returns 200 (handler is not invoked)")
  void validRequestDoesNotTriggerHandler() throws Exception {
    mockMvc
        .perform(get("/api/v1/listings/2099-01-01/m00").header("Authorization", AUTH))
        .andExpect(status().isOk());
  }
}
