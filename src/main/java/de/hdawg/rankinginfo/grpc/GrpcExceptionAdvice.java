package de.hdawg.rankinginfo.grpc;

import java.util.NoSuchElementException;

import io.grpc.Status;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class GrpcExceptionAdvice {

  @GrpcExceptionHandler({NoSuchElementException.class, IndexOutOfBoundsException.class})
  public Status handleNotFound(Exception e) {
    return Status.NOT_FOUND.withDescription("Not Found");
  }

  @GrpcExceptionHandler(IllegalArgumentException.class)
  public Status handleIllegalArgument(IllegalArgumentException e) {
    return Status.INVALID_ARGUMENT.withDescription(e.getMessage());
  }

  @GrpcExceptionHandler(Exception.class)
  public Status handleException(Exception e) {
    return Status.INTERNAL.withDescription("Internal Server Error");
  }
}
