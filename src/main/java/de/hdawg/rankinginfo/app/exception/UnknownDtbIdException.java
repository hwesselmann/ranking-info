package de.hdawg.rankinginfo.app.exception;

/**
 * Exception for indicating an unknown dtbId was requested.
 */
public class UnknownDtbIdException extends RuntimeException {

  private final String message;

  public UnknownDtbIdException(String msg) {
    message = msg;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
