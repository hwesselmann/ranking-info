package de.hdawg.rankinginfo.grpc;

import java.util.Set;

final class GrpcPublicServices {

  static final Set<String> NAMES =
      Set.of("grpc.health.v1.Health", "grpc.reflection.v1alpha.ServerReflection", "grpc.reflection.v1.ServerReflection");

  private GrpcPublicServices() {}
}
