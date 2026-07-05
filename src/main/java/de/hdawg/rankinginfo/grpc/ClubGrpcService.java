package de.hdawg.rankinginfo.grpc;

import java.util.NoSuchElementException;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import de.hdawg.rankinginfo.grpc.v1.ClubGroup;
import de.hdawg.rankinginfo.grpc.v1.ClubPlayerItem;
import de.hdawg.rankinginfo.grpc.v1.ClubSearchItem;
import de.hdawg.rankinginfo.grpc.v1.ClubServiceGrpc;
import de.hdawg.rankinginfo.grpc.v1.GetClubRequest;
import de.hdawg.rankinginfo.grpc.v1.GetClubResponse;
import de.hdawg.rankinginfo.grpc.v1.SearchClubsRequest;
import de.hdawg.rankinginfo.grpc.v1.SearchClubsResponse;
import de.hdawg.rankinginfo.web.ClubService;

@GrpcService
public class ClubGrpcService extends ClubServiceGrpc.ClubServiceImplBase {

  private final ClubService clubService;

  public ClubGrpcService(ClubService clubService) {
    this.clubService = clubService;
  }

  @Override
  public void searchClubs(
      SearchClubsRequest request, StreamObserver<SearchClubsResponse> responseObserver) {
    if (request.getName().isBlank()) {
      throw new IllegalArgumentException("name parameter required");
    }

    var clubs = clubService.searchClubs(request.getName());
    if (clubs.isEmpty()) {
      throw new NoSuchElementException("Not Found");
    }

    var items =
        clubs.stream()
            .map(
                c ->
                    ClubSearchItem.newBuilder()
                        .setName(c.name())
                        .setYouthCount(c.youthCount())
                        .setAdultCount(c.adultCount())
                        .build())
            .toList();

    responseObserver.onNext(
        SearchClubsResponse.newBuilder()
            .setRequest(request)
            .addAllItems(items)
            .setTotalCount(clubs.size())
            .build());
    responseObserver.onCompleted();
  }

  @Override
  public void getClub(GetClubRequest request, StreamObserver<GetClubResponse> responseObserver) {
    var roster = clubService.getClubDetail(request.getId());
    if (roster.isEmpty()) {
      throw new NoSuchElementException("Not Found");
    }

    var groups =
        roster.entrySet().stream()
            .map(
                e ->
                    ClubGroup.newBuilder()
                        .setGroup(e.getKey())
                        .addAllPlayers(
                            e.getValue().stream()
                                .map(
                                    p ->
                                        ClubPlayerItem.newBuilder()
                                            .setDtbId(p.dtbId())
                                            .setLastname(p.lastname())
                                            .setFirstname(p.firstname())
                                            .setRank(p.rank())
                                            .setScore(p.score())
                                            .build())
                                .toList())
                        .build())
            .toList();

    responseObserver.onNext(
        GetClubResponse.newBuilder().setName(request.getId()).addAllGroups(groups).build());
    responseObserver.onCompleted();
  }
}
