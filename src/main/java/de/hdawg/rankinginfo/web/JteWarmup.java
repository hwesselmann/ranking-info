package de.hdawg.rankinginfo.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class JteWarmup implements ApplicationListener<ApplicationReadyEvent> {

  private static final Logger log = LoggerFactory.getLogger(JteWarmup.class);

  private static final String[] WARMUP_PATHS = {
    "/", "/listings", "/players", "/clubs", "/federations", "/status", "/help", "/about", "/privacy"
  };

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    var port = event.getApplicationContext().getEnvironment().getProperty("local.server.port");
    if (port == null) {
      return;
    }
    log.info("Pre-warming jte templates");
    var restTemplate = new RestTemplate();
    var baseUrl = "http://localhost:" + port;
    int warmed = 0;
    for (var path : WARMUP_PATHS) {
      try {
        restTemplate.getForObject(baseUrl + path, String.class);
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
