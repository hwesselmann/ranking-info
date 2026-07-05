package de.hdawg.rankinginfo.grpc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.Status;
import org.junit.jupiter.api.Test;

import de.hdawg.rankinginfo.api.security.ApiTokenValidator;
import de.hdawg.rankinginfo.config.ApiProperties;

class GrpcAuthInterceptorTest {

  private final ApiTokenValidator tokenValidator =
      new ApiTokenValidator(new ApiProperties(List.of("valid-token")));
  private final GrpcAuthInterceptor interceptor = new GrpcAuthInterceptor(tokenValidator);

  @SuppressWarnings("unchecked")
  private ServerCall<Object, Object> mockCall(String serviceName) {
    var call = (ServerCall<Object, Object>) mock(ServerCall.class);
    var method =
        MethodDescriptor.newBuilder(mock(MethodDescriptor.Marshaller.class), mock(MethodDescriptor.Marshaller.class))
            .setFullMethodName(serviceName + "/SomeMethod")
            .setType(MethodDescriptor.MethodType.UNARY)
            .build();
    when(call.getMethodDescriptor()).thenReturn((MethodDescriptor) method);
    return call;
  }

  @SuppressWarnings("unchecked")
  private ServerCallHandler<Object, Object> handlerReturning(AtomicBoolean called) {
    ServerCallHandler<Object, Object> handler = mock(ServerCallHandler.class);
    when(handler.startCall(any(), any()))
        .thenAnswer(
            invocation -> {
              called.set(true);
              return mock(ServerCall.Listener.class);
            });
    return handler;
  }

  @Test
  void passesCallWithValidToken() {
    var call = mockCall("rankinginfo.v1.RankingService");
    var headers = new Metadata();
    headers.put(GrpcAuthInterceptor.AUTHORIZATION_KEY, "Bearer valid-token");
    var called = new AtomicBoolean(false);

    interceptor.interceptCall(call, headers, handlerReturning(called));

    assertTrue(called.get());
    verify(call, never()).close(any(), any());
  }

  @Test
  void rejectsCallWithMissingToken() {
    var call = mockCall("rankinginfo.v1.RankingService");
    var headers = new Metadata();
    var called = new AtomicBoolean(false);

    interceptor.interceptCall(call, headers, handlerReturning(called));

    assertFalse(called.get());
    verify(call, times(1)).close(argThatUnauthenticated(), any());
  }

  @Test
  void rejectsCallWithWrongToken() {
    var call = mockCall("rankinginfo.v1.RankingService");
    var headers = new Metadata();
    headers.put(GrpcAuthInterceptor.AUTHORIZATION_KEY, "Bearer wrong-token");
    var called = new AtomicBoolean(false);

    interceptor.interceptCall(call, headers, handlerReturning(called));

    assertFalse(called.get());
  }

  @Test
  void allowsHealthCheckWithoutToken() {
    var call = mockCall("grpc.health.v1.Health");
    var headers = new Metadata();
    var called = new AtomicBoolean(false);

    interceptor.interceptCall(call, headers, handlerReturning(called));

    assertTrue(called.get());
  }

  @Test
  void allowsReflectionWithoutToken() {
    var call = mockCall("grpc.reflection.v1alpha.ServerReflection");
    var headers = new Metadata();
    var called = new AtomicBoolean(false);

    interceptor.interceptCall(call, headers, handlerReturning(called));

    assertTrue(called.get());
  }

  private static Status argThatUnauthenticated() {
    return org.mockito.ArgumentMatchers.argThat(
        status -> status != null && status.getCode() == Status.Code.UNAUTHENTICATED);
  }
}
