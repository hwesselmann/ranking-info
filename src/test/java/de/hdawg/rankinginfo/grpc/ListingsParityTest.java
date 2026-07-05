package de.hdawg.rankinginfo.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.time.LocalDate;
import java.util.List;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import de.hdawg.rankinginfo.domain.Ranking;
import de.hdawg.rankinginfo.grpc.v1.ListListingsRequest;
import de.hdawg.rankinginfo.grpc.v1.RankingServiceGrpc;
import de.hdawg.rankinginfo.repository.ImportHistoryRepository;
import de.hdawg.rankinginfo.repository.RankingRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ListingsParityTest {

  @Autowired MockMvc mockMvc;
  @Autowired RankingRepository rankingRepository;
  @Autowired ImportHistoryRepository importHistoryRepository;
  @Autowired CacheManager cacheManager;

  private final String quarter = "2026-04-01";
  private ManagedChannel channel;
  private RankingServiceGrpc.RankingServiceBlockingStub grpcStub;

  @BeforeEach
  void setUp() {
    var q = LocalDate.parse(quarter);
    rankingRepository.saveAll(
        List.of(
            new Ranking(0, 10_001_001, "Mueller", "Hans", "GER", "m00", q, 1, "500", "TC Test", "WTV", false, false, false),
            new Ranking(0, 10_002_002, "Schmidt", "Peter", "GER", "m00", q, 2, "490", "TC Test", "WTV", false, false, false)));
    var cache = cacheManager.getCache("available_dates");
    if (cache != null) {
      cache.clear();
    }

    channel = ManagedChannelBuilder.forAddress("localhost", 9095).usePlaintext().build();
    var metadata = new Metadata();
    metadata.put(GrpcAuthInterceptor.AUTHORIZATION_KEY, "Bearer test-api-token");
    grpcStub =
        RankingServiceGrpc.newBlockingStub(channel)
            .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));
  }

  @AfterEach
  void tearDown() {
    channel.shutdownNow();
    rankingRepository.deleteAll();
    importHistoryRepository.deleteAll();
  }

  @Test
  @DisplayName("REST and gRPC return the same items for identical parameters")
  void restAndGrpcReturnSameItems() throws Exception {
    var restResult =
        mockMvc
            .perform(
                get("/api/v1/listings/" + quarter + "/m00")
                    .header("Authorization", "Bearer test-api-token"))
            .andReturn();
    var restBody = new ObjectMapper().readTree(restResult.getResponse().getContentAsString());
    var restItems = restBody.get("data");

    var grpcResponse =
        grpcStub.listListings(
            ListListingsRequest.newBuilder().setQuarter(quarter).setAgeGroupSlug("m00").setPage(1).setPerPage(25).build());

    assertEquals(restItems.size(), grpcResponse.getItemsCount());
    for (int i = 0; i < grpcResponse.getItemsCount(); i++) {
      var restItem = restItems.get(i);
      var grpcItem = grpcResponse.getItems(i);
      assertEquals(restItem.get("dtb_id").asInt(), grpcItem.getDtbId());
      assertEquals(restItem.get("ranking_position").asInt(), grpcItem.getRankingPosition());
      assertEquals(restItem.get("lastname").asText(), grpcItem.getLastname());
      assertEquals(restItem.get("score").asText(), grpcItem.getScore());
    }
  }
}
