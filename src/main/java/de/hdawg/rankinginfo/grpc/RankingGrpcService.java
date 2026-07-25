package de.hdawg.rankinginfo.grpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import de.hdawg.rankinginfo.domain.Ranking;
import de.hdawg.rankinginfo.grpc.v1.ListListingsRequest;
import de.hdawg.rankinginfo.grpc.v1.ListListingsResponse;
import de.hdawg.rankinginfo.grpc.v1.ListingItem;
import de.hdawg.rankinginfo.grpc.v1.PageInfo;
import de.hdawg.rankinginfo.grpc.v1.RankingServiceGrpc;
import de.hdawg.rankinginfo.service.Pagination;
import de.hdawg.rankinginfo.service.RankingFilter;
import de.hdawg.rankinginfo.service.RankingService;

@GrpcService
public class RankingGrpcService extends RankingServiceGrpc.RankingServiceImplBase {

  private final RankingService rankingService;

  public RankingGrpcService(RankingService rankingService) {
    this.rankingService = rankingService;
  }

  @Override
  public void listListings(
      ListListingsRequest request, StreamObserver<ListListingsResponse> responseObserver) {
    var pagination = Pagination.of(request.getPage(), request.getPerPage());
    var filter =
        new RankingFilter(
            request.getQuarter(),
            request.getAgeGroupSlug(),
            request.getAgeGroupOptions().isBlank() ? null : request.getAgeGroupOptions(),
            request.getFederation().isBlank() ? null : request.getFederation(),
            request.getClub().isBlank() ? null : request.getClub(),
            request.getYearEnd());

    var rankings =
        rankingService.findFilteredRankings(filter, pagination.page(), pagination.perPage());
    var dtbIds = rankings.getContent().stream().map(Ranking::dtbId).toList();
    var prevPositions = rankingService.findPreviousPositions(filter, dtbIds);

    var items = rankings.getContent().stream().map(r -> toListingItem(r, prevPositions.get(r.dtbId()))).toList();

    var echo =
        ListListingsRequest.newBuilder()
            .setQuarter(request.getQuarter())
            .setAgeGroupSlug(request.getAgeGroupSlug())
            .setAgeGroupOptions(request.getAgeGroupOptions())
            .setFederation(request.getFederation())
            .setClub(request.getClub())
            .setYearEnd(request.getYearEnd())
            .setPage(pagination.page())
            .setPerPage(pagination.perPage())
            .build();

    var response =
        ListListingsResponse.newBuilder()
            .setRequest(echo)
            .addAllItems(items)
            .setPageInfo(
                PageInfo.newBuilder()
                    .setPage(pagination.page())
                    .setPerPage(pagination.perPage())
                    .setTotalCount(rankings.getTotalElements())
                    .build())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  private static ListingItem toListingItem(Ranking r, Integer prevPosition) {
    var builder =
        ListingItem.newBuilder()
            .setDtbId(r.dtbId())
            .setRankingPosition(r.rankingPosition())
            .setLastname(r.lastname())
            .setFirstname(r.firstname())
            .setNationality(r.nationality())
            .setClub(r.club())
            .setFederation(r.federation())
            .setScore(r.score());
    if (prevPosition != null) {
      builder.setPositionChange(prevPosition - r.rankingPosition());
    }
    return builder.build();
  }
}
