package de.hdawg.rankinginfo.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

import de.hdawg.rankinginfo.domain.Ranking;
import de.hdawg.rankinginfo.grpc.v1.ClubServiceGrpc;
import de.hdawg.rankinginfo.grpc.v1.GetClubRequest;
import de.hdawg.rankinginfo.grpc.v1.SearchClubsRequest;
import de.hdawg.rankinginfo.repository.ImportHistoryRepository;
import de.hdawg.rankinginfo.repository.RankingRepository;

@SpringBootTest
class ClubGrpcServiceTest {

  private static final int GRPC_TEST_PORT = 9095;

  @Autowired RankingRepository rankingRepository;
  @Autowired ImportHistoryRepository importHistoryRepository;
  @Autowired CacheManager cacheManager;

  private ManagedChannel channel;
  private ClubServiceGrpc.ClubServiceBlockingStub authedStub;

  @BeforeEach
  void setUp() {
    var q = LocalDate.parse("2026-04-01");
    rankingRepository.saveAll(
        List.of(
            new Ranking(0, 10_001_001, "Mueller", "Hans", "GER", "m00", q, 1, "500", "TC Test", "WTV", false, false, false)));
    var cache = cacheManager.getCache("available_dates");
    if (cache != null) {
      cache.clear();
    }

    channel = ManagedChannelBuilder.forAddress("localhost", GRPC_TEST_PORT).usePlaintext().build();
    var metadata = new Metadata();
    metadata.put(GrpcAuthInterceptor.AUTHORIZATION_KEY, "Bearer test-api-token");
    authedStub =
        ClubServiceGrpc.newBlockingStub(channel)
            .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));
  }

  @AfterEach
  void tearDown() {
    channel.shutdownNow();
    rankingRepository.deleteAll();
    importHistoryRepository.deleteAll();
  }

  @Test
  @DisplayName("rejects blank name as INVALID_ARGUMENT")
  void rejectsBlankName() {
    var request = SearchClubsRequest.newBuilder().setName("").build();

    var ex = assertThrows(StatusRuntimeException.class, () -> authedStub.searchClubs(request));

    assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
  }

  @Test
  @DisplayName("searches clubs by name")
  void searchesClubsByName() {
    var request = SearchClubsRequest.newBuilder().setName("TC Test").build();

    var response = authedStub.searchClubs(request);

    assertEquals(1, response.getItemsCount());
    assertEquals("TC Test", response.getItems(0).getName());
    assertEquals(1, response.getItems(0).getAdultCount());
  }

  @Test
  @DisplayName("returns NOT_FOUND for unknown club")
  void returnsNotFoundForUnknownClub() {
    var request = GetClubRequest.newBuilder().setId("Unknown Club").build();

    var ex = assertThrows(StatusRuntimeException.class, () -> authedStub.getClub(request));

    assertEquals(Status.Code.NOT_FOUND, ex.getStatus().getCode());
  }

  @Test
  @DisplayName("returns club roster grouped by category")
  void returnsClubRoster() {
    var request = GetClubRequest.newBuilder().setId("TC Test").build();

    var response = authedStub.getClub(request);

    assertEquals(1, response.getGroupsCount());
    assertEquals("Herren", response.getGroups(0).getGroup());
    assertEquals(1, response.getGroups(0).getPlayersCount());
  }
}
