package de.hdawg.rankinginfo.api.v1;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import jakarta.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.hdawg.rankinginfo.api.v1.dto.ListingItem;
import de.hdawg.rankinginfo.api.v1.dto.ListingRequest;
import de.hdawg.rankinginfo.api.v1.dto.ListingResponse;
import de.hdawg.rankinginfo.api.v1.dto.PlayerLink;
import de.hdawg.rankinginfo.domain.Ranking;
import de.hdawg.rankinginfo.service.RankingFilter;
import de.hdawg.rankinginfo.service.RankingService;

@RestController
@RequestMapping("/api/v1/listings")
@SecurityRequirement(name = "bearerAuth")
public class ListingsApiController {

  private static final int MAX_PER_PAGE = 100;

  private final RankingService rankingService;

  public ListingsApiController(RankingService rankingService) {
    this.rankingService = rankingService;
  }

  @Operation(summary = "Get paginated ranking list for a quarter and age group")
  @GetMapping("/{quarter}/{ageGroupSlug}")
  public ResponseEntity<ListingResponse> index(
      @PathVariable String quarter,
      @PathVariable String ageGroupSlug,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(name = "per_page", defaultValue = "25") int perPage,
      @RequestParam(required = false) String federation,
      @RequestParam(required = false) String club,
      @RequestParam(name = "age_group_options", required = false) String ageGroupOptions,
      @RequestParam(name = "year_end", defaultValue = "false") boolean yearEnd,
      HttpServletRequest request) {

    var filter = new RankingFilter(quarter, ageGroupSlug, ageGroupOptions, federation, club, yearEnd);
    int cappedPerPage = Math.min(perPage, MAX_PER_PAGE);

    var maxImportedAt = rankingService.maxImportedAt();
    var etagSource =
        "%s|%s|%s|%s|%s|%s|%s|%d|%d"
            .formatted(
                maxImportedAt,
                quarter,
                ageGroupSlug,
                ageGroupOptions,
                federation,
                club,
                yearEnd,
                page,
                cappedPerPage);
    var etag = "\"" + sha256(etagSource) + "\"";
    var ifNoneMatch = request.getHeader("If-None-Match");
    if (etag.equals(ifNoneMatch)) {
      return ResponseEntity.status(304).build();
    }

    var rankings = rankingService.findFilteredRankings(filter, page, cappedPerPage);
    var dtbIds = rankings.getContent().stream().map(Ranking::dtbId).toList();
    var prevPositions = rankingService.findPreviousPositions(filter, dtbIds);

    var data =
        rankings.getContent().stream()
            .map(
                r -> {
                  var prev = prevPositions.get(r.dtbId());
                  Integer positionChange = prev != null ? prev - r.rankingPosition() : null;
                  return new ListingItem(
                      r.dtbId(),
                      r.rankingPosition(),
                      r.lastname(),
                      r.firstname(),
                      r.nationality(),
                      r.club(),
                      r.federation(),
                      r.score(),
                      positionChange,
                      new PlayerLink("/api/v1/players/" + r.dtbId()));
                })
            .toList();

    var response =
        new ListingResponse(
            new ListingRequest(
                quarter,
                ageGroupSlug,
                ageGroupOptions,
                federation,
                club,
                yearEnd,
                page,
                cappedPerPage,
                rankings.getTotalElements()),
            data);

    return ResponseEntity.ok().header("ETag", etag).body(response);
  }

  private static String sha256(String input) {
    try {
      var bytes = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
      var sb = new StringBuilder();
      for (byte b : bytes) {
        sb.append("%02x".formatted(b));
      }
      return sb.toString().substring(0, 16);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
