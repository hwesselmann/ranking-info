package de.hdawg.rankinginfo.app.exception;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * ControllerAdvice for handling errors.
 */
@RestControllerAdvice
@RequestMapping(produces = "application/json")
public class GlobalExceptionHandler {

  public static final String TIMESTAMP_FIELD_LABEL = "timestamp";
  public static final String ERROR_FIELD_LABEL = "error";

  /**
   * Handler for illegal ranking period requests.
   *
   * @param e exception
   * @return api error response
   */
  @ExceptionHandler(RankingPeriodException.class)
  public ResponseEntity<Object> handleIllegalRankingPeriod(final RankingPeriodException e) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put(TIMESTAMP_FIELD_LABEL, LocalDate.now());
    body.put(ERROR_FIELD_LABEL, e.getMessage());

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handler for datastore exceptions.
   *
   * @param e exception
   * @return api error response
   */
  @ExceptionHandler(DatastoreException.class)
  public ResponseEntity<Object> handleDatastoreException(final DatastoreException e) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put(TIMESTAMP_FIELD_LABEL, LocalDate.now());
    body.put(ERROR_FIELD_LABEL, e.getMessage());

    return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Handler for illegal dtbId requests.
   *
   * @param e exception
   * @return api error response
   */
  @ExceptionHandler(UnknownDtbIdException.class)
  ResponseEntity<Object> handleUnknownDtbId(UnknownDtbIdException e) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put(TIMESTAMP_FIELD_LABEL, LocalDate.now());
    body.put(ERROR_FIELD_LABEL, e.getMessage());

    return new ResponseEntity<>(body, HttpStatus.NO_CONTENT);
  }

  /**
   * Handler for requests asking for an unknown club.
   *
   * @param e exception
   * @return api error response
   */
  @ExceptionHandler(ClubNotFoundException.class)
  ResponseEntity<Object> handleUnknownClub(ClubNotFoundException e) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put(TIMESTAMP_FIELD_LABEL, LocalDate.now());
    body.put(ERROR_FIELD_LABEL, e.getMessage());

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }
}
