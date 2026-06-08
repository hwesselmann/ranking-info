package de.hdawg.rankinginfo.service;

public class DuplicateImportError extends Exception {

  private static final long serialVersionUID = 1L;

  public DuplicateImportError(String message) {
    super(message);
  }
}
