package de.hdawg.rankinginfo.service.services;

import de.hdawg.rankinginfo.service.exception.UnknownDtbIdException;
import de.hdawg.rankinginfo.service.model.Federation;
import de.hdawg.rankinginfo.service.model.Nationality;
import de.hdawg.rankinginfo.service.model.Ranking;
import de.hdawg.rankinginfo.service.model.player.Player;
import de.hdawg.rankinginfo.service.model.player.PlayerSearchItem;
import de.hdawg.rankinginfo.service.model.player.PlayerSearchResult;
import de.hdawg.rankinginfo.service.repository.RankingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

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
        .map(r -> new Player(r.dtbId(), r.firstname(), r.lastname(), r.nationality(), r.club(), r.federation()))
        .toList().stream().distinct().toList();
    PlayerSearchResult result = new PlayerSearchResult();
    result.setCount(players.size());
    result.setRequested(LocalDateTime.now());
    List<PlayerSearchItem> items = players.stream().map(p -> new PlayerSearchItem(p.getDtbId(),p.getFirstname(), p.getLastname(),
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
    Player player = new Player(rankings.get(0).dtbId(), rankings.get(0).firstname(), rankings.get(0).lastname(),
        rankings.get(0).nationality(), rankings.get(0).club(), rankings.get(0).federation());
    player.setClubs(new HashMap<>());
    player.setPoints(new HashMap<>());
    player.setRankingPositions(new HashMap<>());
    player.setTrends(new HashMap<>());
    return player;
  }
}
