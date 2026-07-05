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

  // Catch-all: intercepts EVERYTHING, including an already-built StatusRuntimeException from a
  // direct Status.xxx.asRuntimeException() throw, and downgrades it to INTERNAL. gRPC service
  // implementations must throw plain domain exceptions (IllegalArgumentException,
  // NoSuchElementException, etc.) instead, or this handler will swallow their real status code.
  @GrpcExceptionHandler(Exception.class)
  public Status handleException(Exception e) {
    return Status.INTERNAL.withDescription("Internal Server Error");
  }
}
