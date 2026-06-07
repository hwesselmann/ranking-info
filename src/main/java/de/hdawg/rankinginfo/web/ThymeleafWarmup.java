package de.hdawg.rankinginfo.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class ThymeleafWarmup implements ApplicationListener<ApplicationReadyEvent> {

  private static final Logger log = LoggerFactory.getLogger(ThymeleafWarmup.class);

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    var port = event.getApplicationContext().getEnvironment().getProperty("local.server.port");
    if (port == null) {
      return;
    }
    log.info("Pre-warming Thymeleaf templates");
    try {
      new RestTemplate().getForObject("http://localhost:" + port + "/listings", String.class);
      log.info("Thymeleaf template warmup complete");
    } catch (RestClientException e) {
      if (log.isWarnEnabled()) {
        log.warn("Template warmup request failed: {}", e.getMessage());
      }
    }
  }
}
