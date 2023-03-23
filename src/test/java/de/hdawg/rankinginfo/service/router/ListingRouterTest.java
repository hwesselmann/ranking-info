package de.hdawg.rankinginfo.service.router;

import de.hdawg.rankinginfo.service.handler.ListingHandler;
import de.hdawg.rankinginfo.service.model.Federation;
import de.hdawg.rankinginfo.service.model.Nationality;
import de.hdawg.rankinginfo.service.model.listing.Listing;
import de.hdawg.rankinginfo.service.model.listing.ListingItem;
import de.hdawg.rankinginfo.service.repository.RankingRepository;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
class ListingRouterTest {

  @Autowired
  private ListingRouter sut;

  @MockBean
  private ListingHandler listingHandler;

  @MockBean
  private RankingRepository rankingRepository;



  @DisplayName("verify listing route is handled correctly")
  @Test
  void testFetchingListing() {
    WebTestClient client = WebTestClient.bindToRouterFunction(sut.route(listingHandler)).build();

    Listing result = prepareListing(false);
    given(listingHandler.retrieveAgeGroupListing(any())).willReturn(ServerResponse.ok().body(BodyInserters.fromValue(result)));

    client.get()
        .uri("/listing/2022-03-31/boys/u14/official")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Listing.class);
  }

  @DisplayName("verify listing route with club filter is handled correctly")
  @Test
  void testFetchingListingWithClubFilter() {
    WebTestClient client = WebTestClient.bindToRouterFunction(sut.route(listingHandler)).build();

    Listing result = prepareListing(true);
    given(listingHandler.retrieveAgeGroupListingFilteredByClub(any())).willReturn(ServerResponse.ok().body(BodyInserters.fromValue(result)));

    client.get()
        .uri("/listing/2022-03-31/boys/u14/official/TC Tennis")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Listing.class);
  }

  private Listing prepareListing(boolean filtered) {
    Listing result = new Listing(LocalDate.of(2022, 3, 31), "U14",
        false, false, false);
    List<ListingItem> items = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      ListingItem item = new ListingItem(1, "12345678", "Hans", "Spieler",
          Nationality.GER, "TC Tennis", Federation.WTV, "21,3");
      if (!filtered) {
        items.add(item);
      } else {
        if ((i & 3) == 0) {
          items.add(item);
        }
      }
    }
    result.setListingItems(items);
    return result;
  }
}
