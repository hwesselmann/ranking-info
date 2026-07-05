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

import de.hdawg.rankinginfo.domain.Ranking;
import de.hdawg.rankinginfo.grpc.v1.GetPlayerRequest;
import de.hdawg.rankinginfo.grpc.v1.PlayerServiceGrpc;
import de.hdawg.rankinginfo.grpc.v1.SearchPlayersRequest;
import de.hdawg.rankinginfo.repository.ImportHistoryRepository;
import de.hdawg.rankinginfo.repository.RankingRepository;

@SpringBootTest
class PlayerGrpcServiceTest {

  @Autowired RankingRepository rankingRepository;
  @Autowired ImportHistoryRepository importHistoryRepository;

  private ManagedChannel channel;
  private PlayerServiceGrpc.PlayerServiceBlockingStub authedStub;

  @BeforeEach
  void setUp() {
    rankingRepository.saveAll(
        List.of(
            new Ranking(0, 10_001_001, "Mueller", "Hans", "GER", "m00", LocalDate.parse("2026-04-01"), 1, "500", "TC Test", "WTV", false, false, false)));

    channel = ManagedChannelBuilder.forAddress("localhost", 9095).usePlaintext().build();
    var metadata = new Metadata();
    metadata.put(GrpcAuthInterceptor.AUTHORIZATION_KEY, "Bearer test-api-token");
    authedStub =
        PlayerServiceGrpc.newBlockingStub(channel)
            .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));
  }

  @AfterEach
  void tearDown() {
    channel.shutdownNow();
    rankingRepository.deleteAll();
    importHistoryRepository.deleteAll();
  }

  @Test
  @DisplayName("rejects blank lastname as INVALID_ARGUMENT")
  void rejectsBlankLastname() {
    var request = SearchPlayersRequest.newBuilder().setLastname("").build();

    var ex = assertThrows(StatusRuntimeException.class, () -> authedStub.searchPlayers(request));

    assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
  }

  @Test
  @DisplayName("returns NOT_FOUND when no players match")
  void returnsNotFoundWhenNoMatch() {
    var request = SearchPlayersRequest.newBuilder().setLastname("Nobody").build();

    var ex = assertThrows(StatusRuntimeException.class, () -> authedStub.searchPlayers(request));

    assertEquals(Status.Code.NOT_FOUND, ex.getStatus().getCode());
  }

  @Test
  @DisplayName("searches players by lastname")
  void searchesPlayersByLastname() {
    var request = SearchPlayersRequest.newBuilder().setLastname("Mueller").build();

    var response = authedStub.searchPlayers(request);

    assertEquals(1, response.getItemsCount());
    assertEquals(10_001_001, response.getItems(0).getDtbId());
  }

  @Test
  @DisplayName("returns player detail with ranking history")
  void returnsPlayerDetail() {
    var request = GetPlayerRequest.newBuilder().setDtbId(10_001_001).build();

    var response = authedStub.getPlayer(request);

    assertEquals("Mueller", response.getLastname());
    assertEquals(1, response.getRankingsCount());
  }

  @Test
  @DisplayName("returns NOT_FOUND for unknown player id")
  void returnsNotFoundForUnknownPlayer() {
    var request = GetPlayerRequest.newBuilder().setDtbId(99_999_999).build();

    var ex = assertThrows(StatusRuntimeException.class, () -> authedStub.getPlayer(request));

    assertEquals(Status.Code.NOT_FOUND, ex.getStatus().getCode());
  }
}
