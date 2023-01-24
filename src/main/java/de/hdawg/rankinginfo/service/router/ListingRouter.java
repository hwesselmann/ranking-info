package de.hdawg.rankinginfo.service.router;

import de.hdawg.rankinginfo.service.handler.ListingHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * Router for listing endpoints.
 */
@Component
public class ListingRouter {
  @Bean
  public RouterFunction<ServerResponse> route(ListingHandler listingHandler) {
    return RouterFunctions.route()
        .GET("/listing/{quarter}/{gender}/{ageGroup}/{modifier}", RequestPredicates.accept(MediaType.APPLICATION_JSON), listingHandler::retrieveAgeGroupListing)
        .GET("/listing/{quarter}/{gender}/{ageGroup}/{modifier}/{club}", RequestPredicates.accept(MediaType.APPLICATION_JSON), listingHandler::retrieveAgeGroupListingFilteredByClub)
        .build();
  }
}
