package de.hdawg.rankinginfo.importer.exception;

/**
 * exception indication an unknown nationality is encountered.
 */
public class UnknownNationalityException extends RuntimeException {
  private final String message;

  public UnknownNationalityException(String message) {
    this.message = message;
  }

  @Override
  public String getMessage() {
    return message;
  }
}