package de.hdawg.rankinginfo.api.v1;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "de.hdawg.rankinginfo.api")
public class GlobalApiExceptionHandler {

  @ExceptionHandler({NoSuchElementException.class, IndexOutOfBoundsException.class})
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse handleNotFound(Exception e) {
    return new ErrorResponse("Not Found");
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleIllegalArgument(IllegalArgumentException e) {
    return new ErrorResponse(e.getMessage());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse handleException(Exception e) {
    return new ErrorResponse("Internal Server Error");
  }
}
