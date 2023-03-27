package de.hdawg.rankinginfo.service.services;

import de.hdawg.rankinginfo.service.exception.UnknownDtbIdException;
import de.hdawg.rankinginfo.service.model.Ranking;
import de.hdawg.rankinginfo.service.model.club.Club;
import de.hdawg.rankinginfo.service.model.player.*;
import de.hdawg.rankinginfo.service.repository.RankingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * service for fetching and mapping rankings from the database for application usage.
 */
@Service
public class PlayerService {

  private final RankingRepository rankingRepository;

  public PlayerService(RankingRepository rankingRepository) {
    this.rankingRepository = rankingRepository;
  }

  public PlayerSearchResult findPlayers(String dtbId, String name, String yob) {
    List<Ranking> rankings = rankingRepository.findPlayers(dtbId, name, yob);
    List<Player> players = rankings.stream()
        .map(r -> new Player(r.getDtbId(), r.getFirstname(), r.getLastname(), r.getNationality(), r.getClub(),
            r.getFederation()))
        .toList().stream().distinct().toList();
    PlayerSearchResult result = new PlayerSearchResult();
    result.setCount(players.size());
    result.setRequested(LocalDateTime.now());
    List<PlayerSearchItem> items = players.stream().map(p -> new PlayerSearchItem(p.getDtbId(), p.getFirstname(), p.getLastname(),
        p.getCurrentFederation(), p.getNationality(), p.getCurrentClub())).toList();
    result.setItems(items);
    return result;
  }

  /**
   * get a player for a given id.
   *
   * @param dtbId unique id
   * @return player domain object
   */
  public Player getPlayerById(String dtbId) {
    List<Ranking> rankings = rankingRepository.getRankingsForPlayer(dtbId);
    if (rankings.isEmpty()) {
      throw new UnknownDtbIdException("DTB-ID " + dtbId + " was not found");
    }
    Player player = new Player(rankings.get(0).getDtbId(), rankings.get(0).getFirstname(), rankings.get(0).getLastname(),
        rankings.get(0).getNationality(), rankings.get(0).getClub(), rankings.get(0).getFederation());
    player.setClubs(rankings.stream().map(r -> new Club(r.getClub(), r.getFederation())).distinct().toList());
    player.setPoints(rankings.stream().map(r -> new PointsHistory(r.getRankingPeriod(), r.getPoints())).distinct().toList());
    player.setOfficialRankingPositions(rankings.stream().sorted(Comparator.comparing(Ranking::getRankingPeriod).reversed())
        .map(r -> new RankingItem(r.getRankingPeriod(), r.getAgeGroup(), r.getYearOfBirthRanking(),
            r.getOverallYouthRanking(), r.getEndOfYearRanking(), r.getPosition()))
        .filter(r -> !r.endOfYear() && !r.overall() && !r.yobOnly()).toList());
    player.setYobRankingPositions(rankings.stream().sorted(Comparator.comparing(Ranking::getRankingPeriod).reversed())
        .map(r -> new RankingItem(r.getRankingPeriod(), r.getAgeGroup(), r.getYearOfBirthRanking(),
            r.getOverallYouthRanking(), r.getEndOfYearRanking(), r.getPosition()))
        .filter(r -> !r.endOfYear() && !r.overall() && r.yobOnly()).toList());
    player.setOverallRankingPositions(rankings.stream().sorted(Comparator.comparing(Ranking::getRankingPeriod).reversed())
        .map(r -> new RankingItem(r.getRankingPeriod(), r.getAgeGroup(), r.getYearOfBirthRanking(),
            r.getOverallYouthRanking(), r.getEndOfYearRanking(), r.getPosition()))
        .filter(r -> !r.endOfYear() && r.overall() && !r.yobOnly()).toList());
    player.setEndOfYearRankingPositions(rankings.stream().sorted(Comparator.comparing(Ranking::getRankingPeriod).reversed())
        .map(r -> new RankingItem(r.getRankingPeriod(), r.getAgeGroup(), r.getYearOfBirthRanking(),
            r.getOverallYouthRanking(), r.getEndOfYearRanking(), r.getPosition()))
        .filter(r -> r.endOfYear() && !r.overall() && !r.yobOnly()).toList());
    player.setTrends(new ArrayList<>());
    return player;
  }

  List<Trend> calculateAgeGroupTrends(List<Ranking> previousRankings, List<Ranking> currentRankings) {
    List<Trend> result = new ArrayList<>();

    return result;
  }
}
