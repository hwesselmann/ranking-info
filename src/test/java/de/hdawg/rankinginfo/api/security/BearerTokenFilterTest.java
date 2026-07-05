package de.hdawg.rankinginfo.api.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import de.hdawg.rankinginfo.config.ApiProperties;

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
    return new BearerTokenFilter(new ApiTokenValidator(new ApiProperties(tokens)), objectMapper);
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

  static Stream<Arguments> unauthorizedRequests() {
    return Stream.of(
        Arguments.of("no authorization header", null),
        Arguments.of("wrong token", "Bearer wrong-token"),
        Arguments.of("non-bearer scheme", "Token valid-token"),
        Arguments.of("token off by one character", "Bearer valid-toke"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("unauthorizedRequests")
  void returns401ForUnauthorizedRequest(String description, String headerValue) throws Exception {
    var request = new MockHttpServletRequest();
    if (headerValue != null) {
      request.addHeader("Authorization", headerValue);
    }
    var response = new MockHttpServletResponse();

    filter().doFilter(request, response, noOpChain);

    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
  }

  @Test
  @DisplayName("accepts token from configured tokens list")
  void acceptsTokenFromConfiguredTokensList() throws Exception {
    var filterWithToken =
        new BearerTokenFilter(new ApiTokenValidator(new ApiProperties(List.of("env-token"))), objectMapper);
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
    assertEquals("Unauthorized", body.get("error").textValue());
  }

  @Test
  @DisplayName("accepts any token from a multi-token configuration")
  void acceptsAnyTokenFromMultiTokenConfig() throws Exception {
    var multiFilter = filter(List.of("token-a", "token-b", "token-c"));

    for (var t : List.of("token-a", "token-b", "token-c")) {
      var request = new MockHttpServletRequest();
      request.addHeader("Authorization", "Bearer " + t);
      var response = new MockHttpServletResponse();
      var chainCalled = new AtomicBoolean(false);

      multiFilter.doFilter(request, response, (req, res) -> chainCalled.set(true));

      assertTrue(chainCalled.get(), "Expected chain to be called for token: " + t);
    }
  }

  @Test
  @DisplayName("blank tokens in configuration are ignored")
  void blankTokensInConfigAreIgnored() throws Exception {
    var filterWithBlanks = filter(List.of("  ", "", "valid-token"));
    var request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer   ");
    var response = new MockHttpServletResponse();

    filterWithBlanks.doFilter(request, response, noOpChain);

    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
  }
}
