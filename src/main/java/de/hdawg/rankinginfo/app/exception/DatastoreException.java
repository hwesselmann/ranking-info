package de.hdawg.rankinginfo.app.exception;

/**
 * Exception thrown when there is an issue with the datastore.
 */
public class DatastoreException extends RuntimeException {

  private final String message;

  public DatastoreException(String msg) {
    message = msg;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
