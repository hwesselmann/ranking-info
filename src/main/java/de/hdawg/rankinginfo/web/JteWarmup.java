package de.hdawg.rankinginfo.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/// Sends warmup HTTP requests to a handful of pages right after startup, so the first real user
/// request doesn't pay for jte's template compilation/class-loading cost.
///
/// Triggered by [ApplicationReadyEvent]; a no-op if the embedded server's port cannot be
/// determined (e.g. in a context without a web server).
@Component
public class JteWarmup implements ApplicationListener<ApplicationReadyEvent> {

  private static final Logger log = LoggerFactory.getLogger(JteWarmup.class);

  private static final String[] WARMUP_PATHS = {
    "/", "/listings", "/players", "/clubs", "/federations", "/status", "/help", "/about", "/privacy"
  };

  /// Issues one GET request per entry in [#WARMUP_PATHS] against the freshly started server,
  /// logging how many succeeded. Individual request failures are logged and otherwise ignored.
  ///
  /// @param event published once the application context is fully started and the embedded
  ///     server is listening
  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    var port = event.getApplicationContext().getEnvironment().getProperty("local.server.port");
    if (port == null) {
      return;
    }
    log.info("Pre-warming jte templates");
    var client = RestClient.create();
    var baseUrl = "http://localhost:" + port;
    int warmed = 0;
    for (var path : WARMUP_PATHS) {
      try {
        client.get().uri(baseUrl + path).retrieve().toBodilessEntity();
        warmed++;
      } catch (RestClientException e) {
        if (log.isWarnEnabled()) {
          log.warn("Template warmup request failed for {}: {}", path, e.getMessage());
        }
      }
    }
    log.info("jte template warmup complete ({}/{} templates warmed)", warmed, WARMUP_PATHS.length);
  }
}
