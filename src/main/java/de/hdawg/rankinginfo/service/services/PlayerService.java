package de.hdawg.rankinginfo.service.services;

import de.hdawg.rankinginfo.service.model.Federation;
import de.hdawg.rankinginfo.service.model.Nationality;
import de.hdawg.rankinginfo.service.model.Ranking;
import de.hdawg.rankinginfo.service.model.player.Player;
import de.hdawg.rankinginfo.service.model.player.PlayerSearchResult;
import de.hdawg.rankinginfo.service.model.player.PlayerSearchResultItem;
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
    List<PlayerSearchResultItem> items = players.stream().map(p -> new PlayerSearchResultItem(p.getDtbId(),p.getFirstname(), p.getLastname(),
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
    Player player = new Player("12345678", "Harry", "Hirsch", Nationality.GER, "TC Berliner Gänse", Federation.BB);
    player.setClubs(new HashMap<>());
    player.setPoints(new HashMap<>());
    player.setRankingPositions(new HashMap<>());
    player.setTrends(new HashMap<>());
    return player;
  }
}
