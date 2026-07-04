package de.hdawg.rankinginfo.grpc;

import java.util.Set;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import de.hdawg.rankinginfo.api.security.ApiTokenValidator;

@Component
@GrpcGlobalServerInterceptor
@Order(2)
public class GrpcAuthInterceptor implements ServerInterceptor {

  static final Metadata.Key<String> AUTHORIZATION_KEY =
      Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

  private static final Set<String> PUBLIC_SERVICES =
      Set.of("grpc.health.v1.Health", "grpc.reflection.v1alpha.ServerReflection", "grpc.reflection.v1.ServerReflection");

  private final ApiTokenValidator tokenValidator;

  public GrpcAuthInterceptor(ApiTokenValidator tokenValidator) {
    this.tokenValidator = tokenValidator;
  }

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
    if (PUBLIC_SERVICES.contains(call.getMethodDescriptor().getServiceName())) {
      return next.startCall(call, headers);
    }

    var authHeader = headers.get(AUTHORIZATION_KEY);
    var token = authHeader != null ? authHeader.replaceFirst("^Bearer ", "") : null;
    if (token == null || token.isBlank() || !tokenValidator.isValid(token)) {
      call.close(Status.UNAUTHENTICATED.withDescription("Unauthorized"), new Metadata());
      return new ServerCall.Listener<>() {};
    }
    return next.startCall(call, headers);
  }
}
