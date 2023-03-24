package de.hdawg.rankinginfo.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ControllerAdvice for handling errors.
 */
@RestControllerAdvice
@RequestMapping(produces = "application/json")
public class GlobalExceptionHandler {

  /**
   * Handler for illegal ranking period requests.
   *
   * @param e exception
   * @return api error response
   */
  @ExceptionHandler(RankingPeriodException.class)
  public ResponseEntity<Object> illegalRankingPeriod(final RankingPeriodException e) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDate.now());
    body.put("error", e.getMessage());

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handler for illegal dtbId requests.
   *
   * @param e exception
   * @return api error response
   */
  @ExceptionHandler(UnknownDtbIdException.class)
  ResponseEntity<Object> unknownDtbId(UnknownDtbIdException e) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDate.now());
    body.put("error", e.getMessage());

    return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
  }
}
