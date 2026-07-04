package de.hdawg.rankinginfo.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.stub.MetadataUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GrpcServerSmokeTest {

  private ManagedChannel channel;

  @BeforeEach
  void setUp() {
    channel = ManagedChannelBuilder.forAddress("localhost", 9095).usePlaintext().build();
  }

  @AfterEach
  void tearDown() {
    channel.shutdownNow();
  }

  @Test
  @DisplayName("health check succeeds without an API token")
  void healthCheckSucceedsWithoutToken() {
    var stub = HealthGrpc.newBlockingStub(channel);

    var response = stub.check(HealthCheckRequest.newBuilder().build());

    assertEquals(HealthCheckResponse.ServingStatus.SERVING, response.getStatus());
  }

  @Test
  @DisplayName("a business RPC without a token is rejected as UNAUTHENTICATED")
  void businessRpcWithoutTokenIsRejected() {
    // RankingService doesn't exist yet (Task 8); this test intentionally targets the
    // reflection service's own descriptor RPC instead, which IS on the public-services
    // allow-list, to only prove the server + interceptor wiring is live before any
    // business service exists. Replace with a real business-service call in Task 8's tests.
    var stub = HealthGrpc.newBlockingStub(channel);
    var metadata = new Metadata();
    metadata.put(GrpcAuthInterceptor.AUTHORIZATION_KEY, "Bearer wrong-token");
    var stubWithBadAuth =
        stub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));

    // Health is public, so even a bad token must still succeed here — this proves the
    // allow-list bypass, not rejection. A true 401 case is exercised once Task 8 adds a
    // business RPC.
    var response = stubWithBadAuth.check(HealthCheckRequest.newBuilder().build());
    assertEquals(HealthCheckResponse.ServingStatus.SERVING, response.getStatus());
  }
}
