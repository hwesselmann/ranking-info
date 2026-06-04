package de.hdawg.rankinginfo.api.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class BearerTokenFilterTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final FilterChain noOpChain = (req, res) -> {};

  @BeforeEach
  void setUp() {
    System.setProperty("API_BEARER_TOKEN", "");
  }

  @AfterEach
  void tearDown() {
    System.clearProperty("API_BEARER_TOKEN");
  }

  private BearerTokenFilter filter() {
    return filter(List.of("valid-token"));
  }

  private BearerTokenFilter filter(List<String> tokens) {
    return new BearerTokenFilter(tokens, objectMapper);
  }

  @Test
  @DisplayName("passes request with valid token")
  void passesRequestWithValidToken() throws Exception {
    var request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer valid-token");
    var response = new MockHttpServletResponse();
    var chainCalled = new AtomicBoolean(false);

    filter().doFilter(request, response, (req, res) -> chainCalled.set(true));

    assertTrue(chainCalled.get());
    assertEquals(200, response.getStatus());
  }

  @Test
  @DisplayName("returns 401 without authorization header")
  void returns401WithoutAuthorizationHeader() throws Exception {
    var request = new MockHttpServletRequest();
    var response = new MockHttpServletResponse();

    filter().doFilter(request, response, noOpChain);

    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
  }

  @Test
  @DisplayName("returns 401 with wrong token")
  void returns401WithWrongToken() throws Exception {
    var request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer wrong-token");
    var response = new MockHttpServletResponse();

    filter().doFilter(request, response, noOpChain);

    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
  }

  @Test
  @DisplayName("returns 401 when header is not Bearer scheme")
  void returns401WhenHeaderIsNotBearerScheme() throws Exception {
    var request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Token valid-token");
    var response = new MockHttpServletResponse();

    filter().doFilter(request, response, noOpChain);

    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
  }

  @Test
  @DisplayName("accepts token from configured tokens list")
  void acceptsTokenFromConfiguredTokensList() throws Exception {
    var filterWithToken = new BearerTokenFilter(List.of("env-token"), objectMapper);
    var request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer env-token");
    var response = new MockHttpServletResponse();
    var chainCalled = new AtomicBoolean(false);

    filterWithToken.doFilter(request, response, (req, res) -> chainCalled.set(true));

    assertTrue(chainCalled.get());
  }

  @Test
  @DisplayName("response body contains error json on 401")
  void responseBodyContainsErrorJsonOn401() throws Exception {
    var request = new MockHttpServletRequest();
    var response = new MockHttpServletResponse();

    filter().doFilter(request, response, noOpChain);

    var body = objectMapper.readTree(response.getContentAsString());
    assertEquals("Unauthorized", body.get("error").asText());
  }
}
