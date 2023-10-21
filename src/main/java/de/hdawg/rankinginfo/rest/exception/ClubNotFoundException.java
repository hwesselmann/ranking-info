package de.hdawg.rankinginfo.rest.exception;

/**
 * Exception thrown when a club is requested which is not available in the datastore.
 */
public class ClubNotFoundException extends RuntimeException {

  private final String message;

  public ClubNotFoundException(String message) {
    this.message = message;
  }

  @Override
  public String getMessage() {
    return message;
  }
}