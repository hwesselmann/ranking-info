package de.hdawg.rankinginfo.service.router;

import de.hdawg.rankinginfo.service.handler.ListingHandler;
import de.hdawg.rankinginfo.service.model.listing.Listing;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * Router for listing endpoints.
 */
@Configuration
public class ListingRouter {
  /**
   * routing definitions for listing endpoint.
   *
   * @param listingHandler handler for the endpoint
   * @return server response
   */

  @Bean
  @RouterOperations({
      @RouterOperation(
          method = RequestMethod.GET,
          path = "/listing/{quarter}/{gender}/{ageGroup}/{modifier}",
          operation =
          @Operation(
              description = "get listings for specified quarter, gender, age group and modifiers",
              operationId = "retrieveAgeGroupListing",
              tags = "listing",
              parameters = {
                  @Parameter(in = ParameterIn.PATH, name = "quarter", description = "ranking period in format yyyy-mm-dd"),
                  @Parameter(in = ParameterIn.PATH, name = "gender", description = "gender to request listing for: 'boys' or 'girls'"),
                  @Parameter(in = ParameterIn.PATH, name = "ageGroup", description = "requested age group: 'u11','u12','u13','u14','u15','u16','u17','u18'"),
                  @Parameter(in = ParameterIn.PATH, name = "modifier", description = "get different data views. valid: 'official', 'yob', 'overall', 'endofyear'"),
              },
              responses = {
                  @ApiResponse(
                      responseCode = "200",
                      description = "get listing endpoint",
                      content = {
                          @Content(
                              mediaType = MediaType.APPLICATION_JSON_VALUE,
                              array = @ArraySchema(schema = @Schema(implementation = Listing.class)))
                      }),
                  @ApiResponse(
                      responseCode = "400",
                      description = "bad request - most commonly using wrong parameters",
                      content = {
                          @Content(
                              mediaType = MediaType.APPLICATION_JSON_VALUE,
                              schema = @Schema(implementation = ErrorResponse.class))
                      })
              })),
      @RouterOperation(
          method = RequestMethod.GET,
          path = "/listing/{quarter}/{gender}/{ageGroup}/{modifier}/{club}",
          operation =
          @Operation(
              description = "get listings for specified quarter, gender, age group and modifiers filtered by club string",
              operationId = "retrieveAgeGroupListingFilteredByClub",
              tags = "listing",
              parameters = {
                  @Parameter(in = ParameterIn.PATH, name = "quarter", description = "ranking period in format yyyy-mm-dd"),
                  @Parameter(in = ParameterIn.PATH, name = "gender", description = "gender to request listing for: 'boys' or 'girls'"),
                  @Parameter(in = ParameterIn.PATH, name = "ageGroup", description = "requested age group: 'u11','u12','u13','u14','u15','u16','u17','u18'"),
                  @Parameter(in = ParameterIn.PATH, name = "modifier", description = "get different data views. valid: 'official', 'yob', 'overall', 'endofyear'"),
                  @Parameter(in = ParameterIn.PATH, name = "club", description = "club name or name part to filter for")
              },
              responses = {
                  @ApiResponse(
                      responseCode = "200",
                      description = "get listings filtered by given club",
                      content = {
                          @Content(
                              mediaType = MediaType.APPLICATION_JSON_VALUE,
                              schema = @Schema(implementation = Listing.class))
                      }),
                  @ApiResponse(
                      responseCode = "400",
                      description = "Bad Request response",
                      content = {
                          @Content(
                              mediaType = MediaType.APPLICATION_JSON_VALUE,
                              schema = @Schema(implementation = ErrorResponse.class))
                      }),
                  @ApiResponse(
                      responseCode = "400",
                      description = "bad request - most commonly using wrong parameters",
                      content = {
                          @Content(
                              mediaType = MediaType.APPLICATION_JSON_VALUE,
                              schema = @Schema(implementation = ErrorResponse.class))
                      })
              }))
  })
  public RouterFunction<ServerResponse> route(ListingHandler listingHandler) {
    return RouterFunctions.route()
        .path("/listing/{quarter}/{gender}/{ageGroup}/{modifier}", b1 -> b1
            .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), b2 -> b2
                .GET("/{club}", listingHandler::retrieveAgeGroupListingFilteredByClub)
                .GET(listingHandler::retrieveAgeGroupListing)))
        .build();
  }
}
