package de.hdawg.rankinginfo.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.NoSuchElementException;

import io.grpc.Status;
import org.junit.jupiter.api.Test;

class GrpcExceptionAdviceTest {

  private final GrpcExceptionAdvice advice = new GrpcExceptionAdvice();

  @Test
  void mapsNoSuchElementExceptionToNotFound() {
    var status = advice.handleNotFound(new NoSuchElementException("missing"));

    assertEquals(Status.Code.NOT_FOUND, status.getCode());
  }

  @Test
  void mapsIndexOutOfBoundsExceptionToNotFound() {
    var status = advice.handleNotFound(new IndexOutOfBoundsException("out of bounds"));

    assertEquals(Status.Code.NOT_FOUND, status.getCode());
  }

  @Test
  void mapsIllegalArgumentExceptionToInvalidArgument() {
    var status = advice.handleIllegalArgument(new IllegalArgumentException("bad input"));

    assertEquals(Status.Code.INVALID_ARGUMENT, status.getCode());
    assertEquals("bad input", status.getDescription());
  }

  @Test
  void mapsGenericExceptionToInternal() {
    var status = advice.handleException(new RuntimeException("boom"));

    assertEquals(Status.Code.INTERNAL, status.getCode());
  }
}
