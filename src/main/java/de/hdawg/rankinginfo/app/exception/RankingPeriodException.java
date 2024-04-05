package de.hdawg.rankinginfo.app.exception;

/**
 * Exception thrown when there is an issue with the requested ranking period.
 */
public class RankingPeriodException extends RuntimeException {

  private final String message;

  public RankingPeriodException(String msg) {
    message = msg;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
