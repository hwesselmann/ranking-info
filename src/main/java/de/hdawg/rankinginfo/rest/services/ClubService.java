package de.hdawg.rankinginfo.rest.services;

import de.hdawg.rankinginfo.rest.model.club.ClubPlayer;
import de.hdawg.rankinginfo.rest.model.club.ClubPlayerResult;
import de.hdawg.rankinginfo.rest.model.club.ClubSearchResult;
import de.hdawg.rankinginfo.rest.repository.RankingRepository;
import java.time.ZonedDateTime;
import org.springframework.stereotype.Service;

/**
 * Service class providing access to club data and players by club.
 */
@Service
public class ClubService {

  private final RankingRepository repository;

  public ClubService(RankingRepository repository) {
    this.repository = repository;
  }

  /**
   * Query the repository and return a search result object.
   *
   * @param searchTerm search term, may be empty
   * @return search result container for clubs
   */
  public ClubSearchResult findClub(String searchTerm) {
    var result = repository.findClubsBySearchTerm(searchTerm);
    return new ClubSearchResult(result.size(), ZonedDateTime.now(), result);
  }

  /**
   * retrieve all players with a ranking in the current period for a given club.
   *
   * @param name name of the club
   * @return club player result container
   */
  public ClubPlayerResult getPlayersForClub(String name) {
    var rankings = repository.findRankingsForPlayersOfGivenClub(name,
        repository.getMostRecentRankingPeriod());
    var result = rankings.stream().map(r ->
            new ClubPlayer(r.getDtbId(), r.getLastname(), r.getFirstname(), r.getPoints(),
                r.getPosition())
        )
        .toList();
    return new ClubPlayerResult(name, result.size(), ZonedDateTime.now(), result);
  }
}
