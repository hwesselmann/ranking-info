package de.hdawg.rankinginfo.importer.exception;

/**
 * exception for errors in the import file naming.
 */
public class FilenameFormatException extends RuntimeException {

  private final String message;

  public FilenameFormatException(String message) {
    this.message = message;
  }

  @Override
  public String getMessage() {
    return message;
  }
}