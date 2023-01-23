package de.hdawg.rankinginfo.service.exception;

import lombok.Getter;

/**
 * Exception thrown when there is an issue with the requested ranking period.
 */
@Getter
public class RankingPeriodException extends RuntimeException {
  private final String message;

  public RankingPeriodException(String msg) {
    message = msg;
  }
}
