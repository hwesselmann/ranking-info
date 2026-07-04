package de.hdawg.rankinginfo.grpc;

import java.util.Set;

import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import de.hdawg.rankinginfo.api.security.RequestRateLimiter;

@Component
@GrpcGlobalServerInterceptor
@Order(1)
public class GrpcRateLimitInterceptor implements ServerInterceptor {

  private static final Set<String> PUBLIC_SERVICES =
      Set.of("grpc.health.v1.Health", "grpc.reflection.v1alpha.ServerReflection", "grpc.reflection.v1.ServerReflection");

  private final RequestRateLimiter rateLimiter;

  public GrpcRateLimitInterceptor(RequestRateLimiter rateLimiter) {
    this.rateLimiter = rateLimiter;
  }

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
    if (PUBLIC_SERVICES.contains(call.getMethodDescriptor().getServiceName())) {
      return next.startCall(call, headers);
    }

    var authHeader = headers.get(GrpcAuthInterceptor.AUTHORIZATION_KEY);
    var remoteAddr = call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
    var key = authHeader != null ? authHeader : String.valueOf(remoteAddr);

    if (rateLimiter.tryConsume(key)) {
      return next.startCall(call, headers);
    }
    call.close(Status.RESOURCE_EXHAUSTED.withDescription("Too Many Requests"), new Metadata());
    return new ServerCall.Listener<>() {};
  }
}
