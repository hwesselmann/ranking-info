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
import de.hdawg.rankinginfo.grpc.v1.ListListingsRequest;
import de.hdawg.rankinginfo.grpc.v1.RankingServiceGrpc;
import de.hdawg.rankinginfo.repository.ImportHistoryRepository;
import de.hdawg.rankinginfo.repository.RankingRepository;

@SpringBootTest
class RankingGrpcServiceTest {

  @Autowired RankingRepository rankingRepository;
  @Autowired ImportHistoryRepository importHistoryRepository;
  @Autowired CacheManager cacheManager;

  private final String quarter = "2026-04-01";
  private final String prevQuarter = "2026-01-01";

  private ManagedChannel channel;
  private RankingServiceGrpc.RankingServiceBlockingStub authedStub;
  private RankingServiceGrpc.RankingServiceBlockingStub unauthedStub;

  @BeforeEach
  void setUp() {
    var q = LocalDate.parse(quarter);
    var pq = LocalDate.parse(prevQuarter);
    rankingRepository.saveAll(
        List.of(
            ranking(10_001_001, "Mueller", "Hans", "m00", q, 1, "500"),
            ranking(10_002_002, "Schmidt", "Peter", "m00", q, 2, "490"),
            ranking(10_001_001, "Mueller", "Hans", "m00", pq, 3, "460")));
    var cache = cacheManager.getCache("available_dates");
    if (cache != null) {
      cache.clear();
    }

    channel = ManagedChannelBuilder.forAddress("localhost", 9095).usePlaintext().build();
    unauthedStub = RankingServiceGrpc.newBlockingStub(channel);
    var metadata = new Metadata();
    metadata.put(GrpcAuthInterceptor.AUTHORIZATION_KEY, "Bearer test-api-token");
    authedStub = unauthedStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));
  }

  @AfterEach
  void tearDown() {
    channel.shutdownNow();
    rankingRepository.deleteAll();
    importHistoryRepository.deleteAll();
  }

  private static Ranking ranking(
      int dtbId, String lastname, String firstname, String ageGroup, LocalDate date, int position, String score) {
    return new Ranking(0, dtbId, lastname, firstname, "GER", ageGroup, date, position, score, "TC Test", "WTV", false, false, false);
  }

  @Test
  @DisplayName("rejects request without an API token")
  void rejectsRequestWithoutToken() {
    var request = ListListingsRequest.newBuilder().setQuarter(quarter).setAgeGroupSlug("m00").setPage(1).setPerPage(25).build();

    var ex = assertThrows(StatusRuntimeException.class, () -> unauthedStub.listListings(request));

    assertEquals(Status.Code.UNAUTHENTICATED, ex.getStatus().getCode());
  }

  @Test
  @DisplayName("returns paginated listing with position-change deltas")
  void returnsPaginatedListingWithPositionChange() {
    var request = ListListingsRequest.newBuilder().setQuarter(quarter).setAgeGroupSlug("m00").setPage(1).setPerPage(25).build();

    var response = authedStub.listListings(request);

    assertEquals(2, response.getItemsCount());
    assertEquals(2, response.getPageInfo().getTotalCount());
    var mueller = response.getItemsList().stream().filter(i -> i.getDtbId() == 10_001_001).findFirst().orElseThrow();
    assertEquals(2, mueller.getPositionChange());
    var schmidt = response.getItemsList().stream().filter(i -> i.getDtbId() == 10_002_002).findFirst().orElseThrow();
    assertEquals(false, schmidt.hasPositionChange());
  }
}
