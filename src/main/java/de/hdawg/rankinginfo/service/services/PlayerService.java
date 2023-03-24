package de.hdawg.rankinginfo.service.services;

import de.hdawg.rankinginfo.service.model.Federation;
import de.hdawg.rankinginfo.service.model.Nationality;
import de.hdawg.rankinginfo.service.model.player.Player;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
public class PlayerService {

  public List<Player> findPlayers(String dtbId, String name, String yob) {
    return Collections.emptyList();
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
