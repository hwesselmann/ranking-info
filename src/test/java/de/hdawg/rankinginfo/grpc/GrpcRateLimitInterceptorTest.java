package de.hdawg.rankinginfo.grpc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.grpc.Attributes;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import org.junit.jupiter.api.Test;

import de.hdawg.rankinginfo.api.security.RequestRateLimiter;
import de.hdawg.rankinginfo.config.ApiProperties;

class GrpcRateLimitInterceptorTest {

  private final RequestRateLimiter rateLimiter =
      new RequestRateLimiter(new ApiProperties(List.of()));
  private final GrpcRateLimitInterceptor interceptor = new GrpcRateLimitInterceptor(rateLimiter);

  private ServerCall<Object, Object> mockCall() {
    return mockCall(12345);
  }

  @SuppressWarnings("unchecked")
  private ServerCall<Object, Object> mockCall(int remotePort) {
    var call = (ServerCall<Object, Object>) mock(ServerCall.class);
    var method =
        MethodDescriptor.newBuilder(mock(MethodDescriptor.Marshaller.class), mock(MethodDescriptor.Marshaller.class))
            .setFullMethodName("rankinginfo.v1.RankingService/SomeMethod")
            .setType(MethodDescriptor.MethodType.UNARY)
            .build();
    when(call.getMethodDescriptor()).thenReturn((MethodDescriptor) method);
    var attributes =
        Attributes.newBuilder()
            .set(Grpc.TRANSPORT_ATTR_REMOTE_ADDR, new InetSocketAddress("127.0.0.1", remotePort))
            .build();
    when(call.getAttributes()).thenReturn(attributes);
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
  void allowsRequestsWithinLimit() {
    var call = mockCall();
    var headers = new Metadata();
    headers.put(GrpcAuthInterceptor.AUTHORIZATION_KEY, "Bearer some-token");
    var called = new AtomicBoolean(false);

    interceptor.interceptCall(call, headers, handlerReturning(called));

    assertTrue(called.get());
  }

  @Test
  void rejectsRequestsOnceBucketExhausted() {
    var headers = new Metadata();
    headers.put(GrpcAuthInterceptor.AUTHORIZATION_KEY, "Bearer exhausted-token");

    for (int i = 0; i < 1000; i++) {
      interceptor.interceptCall(mockCall(), headers, handlerReturning(new AtomicBoolean()));
    }

    var called = new AtomicBoolean(false);
    interceptor.interceptCall(mockCall(), headers, handlerReturning(called));

    assertFalse(called.get());
  }

  @Test
  void fallsBackToRemoteAddressWhenNoAuthorizationHeader() {
    var call = mockCall();
    var headers = new Metadata();
    var called = new AtomicBoolean(false);

    interceptor.interceptCall(call, headers, handlerReturning(called));

    assertTrue(called.get());
  }

  @Test
  void sameIpDifferentPortsShareRateLimitBucket() {
    var headers = new Metadata();

    for (int i = 0; i < 1000; i++) {
      interceptor.interceptCall(mockCall(20000 + i), headers, handlerReturning(new AtomicBoolean()));
    }

    var called = new AtomicBoolean(false);
    interceptor.interceptCall(mockCall(54321), headers, handlerReturning(called));

    assertFalse(called.get());
  }

  @Test
  void allowsHealthCheckWithoutRateLimiting() {
    var call = mock(ServerCall.class);
    var method =
        MethodDescriptor.newBuilder(mock(MethodDescriptor.Marshaller.class), mock(MethodDescriptor.Marshaller.class))
            .setFullMethodName("grpc.health.v1.Health/Check")
            .setType(MethodDescriptor.MethodType.UNARY)
            .build();
    when(call.getMethodDescriptor()).thenReturn((MethodDescriptor) method);
    var headers = new Metadata();
    var called = new AtomicBoolean(false);

    interceptor.interceptCall(call, headers, handlerReturning(called));

    assertTrue(called.get());
  }
}
