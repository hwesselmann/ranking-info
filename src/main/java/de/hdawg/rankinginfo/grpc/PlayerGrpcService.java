package de.hdawg.rankinginfo.grpc;

import java.util.NoSuchElementException;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import de.hdawg.rankinginfo.domain.RankingCoding;
import de.hdawg.rankinginfo.grpc.v1.GetPlayerRequest;
import de.hdawg.rankinginfo.grpc.v1.GetPlayerResponse;
import de.hdawg.rankinginfo.grpc.v1.PlayerRankingEntry;
import de.hdawg.rankinginfo.grpc.v1.PlayerSearchItem;
import de.hdawg.rankinginfo.grpc.v1.PlayerServiceGrpc;
import de.hdawg.rankinginfo.grpc.v1.SearchPlayersRequest;
import de.hdawg.rankinginfo.grpc.v1.SearchPlayersResponse;
import de.hdawg.rankinginfo.service.PlayerService;

@GrpcService
public class PlayerGrpcService extends PlayerServiceGrpc.PlayerServiceImplBase {

  private final PlayerService playerService;

  public PlayerGrpcService(PlayerService playerService) {
    this.playerService = playerService;
  }

  @Override
  public void searchPlayers(
      SearchPlayersRequest request, StreamObserver<SearchPlayersResponse> responseObserver) {
    if (request.getLastname().isBlank()) {
      throw new IllegalArgumentException("lastname parameter required");
    }
    var yob = request.getYob();
    if (!yob.isBlank() && yob.length() != 4) {
      throw new IllegalArgumentException("yob must be a 4-digit year");
    }

    int yy = yob.isBlank() ? 0 : Integer.parseInt(yob.substring(2, 4));
    var players =
        !yob.isBlank()
            ? playerService.findPlayersByLastnameAndYob(
                request.getLastname(),
                yy + RankingCoding.GENDER_FACTOR_JUNIOREN,
                yy + RankingCoding.GENDER_FACTOR_JUNIORINNEN)
            : playerService.findPlayersByLastname(request.getLastname());

    if (players.isEmpty()) {
      throw new NoSuchElementException("Not Found");
    }

    var items =
        players.stream()
            .map(
                p ->
                    PlayerSearchItem.newBuilder()
                        .setDtbId(p.dtbId())
                        .setLastname(p.lastname())
                        .setFirstname(p.firstname())
                        .setClub(p.club())
                        .build())
            .toList();

    responseObserver.onNext(
        SearchPlayersResponse.newBuilder()
            .setRequest(request)
            .addAllItems(items)
            .setTotalCount(players.size())
            .build());
    responseObserver.onCompleted();
  }

  @Override
  public void getPlayer(GetPlayerRequest request, StreamObserver<GetPlayerResponse> responseObserver) {
    var rankings = playerService.findNonAggregateRankings(request.getDtbId());
    if (rankings.isEmpty()) {
      throw new NoSuchElementException("Not Found");
    }
    var current = rankings.getFirst();
    var entries =
        rankings.stream()
            .map(
                r ->
                    PlayerRankingEntry.newBuilder()
                        .setQuarter(r.date().toString())
                        .setAgeGroup(r.ageGroup())
                        .setRankingPosition(r.rankingPosition())
                        .setScore(r.score())
                        .build())
            .toList();

    responseObserver.onNext(
        GetPlayerResponse.newBuilder()
            .setDtbId(current.dtbId())
            .setLastname(current.lastname())
            .setFirstname(current.firstname())
            .setNationality(current.nationality())
            .setClub(current.club())
            .setFederation(current.federation())
            .addAllRankings(entries)
            .build());
    responseObserver.onCompleted();
  }
}
