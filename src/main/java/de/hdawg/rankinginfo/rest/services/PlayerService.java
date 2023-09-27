package de.hdawg.rankinginfo.rest.services;

import de.hdawg.rankinginfo.model.Ranking;
import de.hdawg.rankinginfo.rest.exception.UnknownDtbIdException;
import de.hdawg.rankinginfo.rest.model.club.Club;
import de.hdawg.rankinginfo.rest.model.player.Player;
import de.hdawg.rankinginfo.rest.model.player.PlayerSearchItem;
import de.hdawg.rankinginfo.rest.model.player.PlayerSearchResult;
import de.hdawg.rankinginfo.rest.model.player.PointsHistory;
import de.hdawg.rankinginfo.rest.model.player.RankingItem;
import de.hdawg.rankinginfo.rest.model.player.Trend;
import de.hdawg.rankinginfo.rest.repository.RankingRepository;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

/**
 * service for fetching and mapping rankings from the database for application usage.
 */
@Service
public class PlayerService {

  private final RankingRepository rankingRepository;

  public PlayerService(RankingRepository rankingRepository) {
    this.rankingRepository = rankingRepository;
  }

  /**
   * find players matching the given parameters.
   *
   * @param dtbId unique player id (or part of it)
   * @param name  lastname (or part of it)
   * @param yob   year of birth
   * @return player search result container with results
   */
  public PlayerSearchResult findPlayers(String dtbId, String name, String yob) {
    var rankings = retrieveRankingsForMapping(dtbId, name, yob);
    var players = rankings.stream()
        .map(r -> new Player(r.getDtbId(), r.getFirstname(), r.getLastname(), r.getNationality(),
            r.getClub(),
            r.getFederation()))
        .toList().stream().distinct().toList();
    return new PlayerSearchResult(
        players.size(),
        ZonedDateTime.now(),
        players.stream()
            .map(p -> new PlayerSearchItem(p.getDtbId(), p.getFirstname(), p.getLastname(),
                p.getCurrentFederation(), p.getNationality(), p.getCurrentClub())).toList()
    );
  }

  /**
   * fetch rankings for mapping to players objects according to the passed in params.
   *
   * @param dtbId dtbid
   * @param name  name
   * @param yob   yob
   * @return list of rankings matching the params
   */
  List<Ranking> retrieveRankingsForMapping(String dtbId, String name, String yob) {
    if (name != null && !name.isEmpty()) {
      if (yob != null && !yob.isEmpty()) {
        if (dtbId != null && !dtbId.isEmpty()) {
          return rankingRepository.findPlayersByDtbIdAndNameAndYob(dtbId, name, yob);
        } else {
          return rankingRepository.findPlayersByNameAndYob(name, yob);
        }
      } else {
        if (dtbId != null && !dtbId.isEmpty()) {
          return rankingRepository.findPlayersByNameAndDtbId(dtbId, name);
        } else {
          return rankingRepository.findPlayersByName(name);
        }
      }
    } else {
      if (yob != null && !yob.isEmpty()) {
        if (dtbId != null && !dtbId.isEmpty()) {
          return rankingRepository.findPlayersByDtbIdAndYob(dtbId, yob);
        } else {
          return rankingRepository.findPlayersByYob(yob);
        }
      } else {
        if (dtbId != null && !dtbId.isEmpty()) {
          return rankingRepository.findPlayersByDtbId(dtbId);
        } else {
          return rankingRepository.findAllPlayers();
        }
      }
    }
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
    Player player = new Player(rankings.get(0).getDtbId(), rankings.get(0).getFirstname(),
        rankings.get(0).getLastname(),
        rankings.get(0).getNationality(), rankings.get(0).getClub(),
        rankings.get(0).getFederation());
    LocalDate currentRankingPeriod = rankings.get(0).getRankingPeriod();
    player.setClubs(
        rankings.stream().map(r -> new Club(r.getClub(), r.getFederation())).distinct().toList());
    player.setPoints(
        rankings.stream().map(r -> new PointsHistory(r.getRankingPeriod(), r.getPoints()))
            .distinct().toList());
    player.setOfficialRankingPositions(
        rankings.stream().sorted(Comparator.comparing(Ranking::getRankingPeriod).reversed())
            .map(r -> new RankingItem(r.getRankingPeriod(), r.getAgeGroup(),
                r.getYearOfBirthRanking(),
                r.getOverallYouthRanking(), r.getEndOfYearRanking(), r.getPoints(),
                r.getPosition()))
            .filter(r -> !r.endOfYear() && !r.overall() && !r.yobOnly()).toList());
    player.setYobRankingPositions(
        rankings.stream().sorted(Comparator.comparing(Ranking::getRankingPeriod).reversed())
            .map(r -> new RankingItem(r.getRankingPeriod(), r.getAgeGroup(),
                r.getYearOfBirthRanking(),
                r.getOverallYouthRanking(), r.getEndOfYearRanking(), r.getPoints(),
                r.getPosition()))
            .filter(r -> !r.endOfYear() && !r.overall() && r.yobOnly()).toList());
    player.setOverallRankingPositions(rankings.stream()
        .sorted(Comparator.comparing(Ranking::getRankingPeriod).reversed())
        .map(r -> new RankingItem(r.getRankingPeriod(), r.getAgeGroup(), r.getYearOfBirthRanking(),
            r.getOverallYouthRanking(), r.getEndOfYearRanking(), r.getPoints(), r.getPosition()))
        .filter(r -> !r.endOfYear() && r.overall() && !r.yobOnly()).toList());
    player.setEndOfYearRankingPositions(rankings.stream()
        .sorted(Comparator.comparing(Ranking::getRankingPeriod).reversed())
        .map(r -> new RankingItem(r.getRankingPeriod(), r.getAgeGroup(), r.getYearOfBirthRanking(),
            r.getOverallYouthRanking(), r.getEndOfYearRanking(), r.getPoints(), r.getPosition()))
        .filter(r -> r.endOfYear() && !r.overall() && !r.yobOnly()).toList());
    player.setTrends(calculateAgeGroupTrends(player.getOverallRankingPositions(),
        getEligibleAgeGroups(player.getDtbId(), currentRankingPeriod)));
    return player;
  }

  List<String> getEligibleAgeGroups(String dtbId, LocalDate rankingPeriod) {
    int yob = 2000 + Integer.parseInt(dtbId.substring(1, 3));
    return Stream.of(11, 12, 13, 14, 15, 16, 17, 18)
        .filter(ag -> rankingPeriod.getYear() - yob <= ag)
        .map(ageGroup -> "U" + ageGroup)
        .toList();
  }

  List<Trend> calculateAgeGroupTrends(List<RankingItem> rankingItems,
      List<String> eligibleAgeGroups) {
    List<Trend> result = new ArrayList<>();
    eligibleAgeGroups.forEach(a -> {
      List<RankingItem> filtered = rankingItems.stream()
          .sorted(Comparator.comparing(RankingItem::rankingPeriod).reversed())
          .filter(r -> r.ageGroup().equals(a))
          .limit(2).toList();
      if (filtered.size() > 1) {
        int counter = 0;
        for (RankingItem r : filtered) {
          if (counter % 2 == 0) {
            Trend t = new Trend(r.ageGroup(),
                r.rankingPosition() - filtered.get(counter + 1).rankingPosition(),
                Float.parseFloat(r.points().replace(",", "."))
                    - Float.parseFloat(filtered.get(counter + 1).points().replace(",", ".")));
            result.add(t);
          }
          counter++;
        }
      }
    });

    return result;
  }
}
