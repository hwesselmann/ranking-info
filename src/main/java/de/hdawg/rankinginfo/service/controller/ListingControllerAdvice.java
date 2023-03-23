package de.hdawg.rankinginfo.service.controller;

import de.hdawg.rankinginfo.service.exception.RankingPeriodException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * ControllerAdvice for handling errors.
 */
@RestControllerAdvice
public class ListingControllerAdvice {

  private static final Logger log = LoggerFactory.getLogger(ListingControllerAdvice.class);

  @ExceptionHandler(RankingPeriodException.class)
  ResponseEntity<String> illegalRankingPeriod(RankingPeriodException ex) {
    log.debug("handling RankingPeriodException: {}", ex.toString());
    return ResponseEntity.badRequest().body(ex.getMessage());
  }
}
