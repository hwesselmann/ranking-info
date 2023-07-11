package de.hdawg.rankinginfo.service.services;

import de.hdawg.rankinginfo.service.model.AgeGroup;
import de.hdawg.rankinginfo.service.model.Gender;
import de.hdawg.rankinginfo.service.model.Ranking;
import de.hdawg.rankinginfo.service.model.listing.Listing;
import de.hdawg.rankinginfo.service.model.listing.ListingItem;
import de.hdawg.rankinginfo.service.repository.RankingRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * service providing backend integration and mapping for ranking listing operations.
 */
@Service
public class ListingService {

  private final RankingRepository rankingRepository;

  public ListingService(RankingRepository rankingRepository) {
    this.rankingRepository = rankingRepository;
  }

  /**
   * retrieve the requested age groups. Filter results by given club String.
   *
   * @param rankingPeriod    ranking period
   * @param ageGroup         age group
   * @param gender           gender
   * @param isYobRanking     yob players only
   * @param overallRanking   overall ranking including younger players
   * @param endOfYearRanking is end of year ranking
   * @param club             club to filter for
   * @return listing container
   */
  public Listing getAgeGroupRankingsFilteredByClub(LocalDate rankingPeriod, AgeGroup ageGroup,
                                                   Gender gender,
                                                   boolean isYobRanking, boolean overallRanking,
                                                   boolean endOfYearRanking, String club) {
    Listing result =
        getAgeGroupRankings(rankingPeriod, ageGroup, gender, isYobRanking, overallRanking,
            endOfYearRanking);
    List<ListingItem> filteredItems =
        result.getListingItems().stream().filter(r -> r.club().contains(club)).toList();
    result.setListingItems(filteredItems);

    return result;
  }

  /**
   * retrieve the age group rankings from the datastore and map them to the json structure for the clients.
   *
   * @param rankingPeriod    ranking period
   * @param ageGroup         age group
   * @param gender           gender
   * @param isYobRanking     requested yob ranking only
   * @param overallRanking   requested overall ranking including younger players
   * @param endOfYearRanking requested end of year ranking
   * @return listing container
   */
  public Listing getAgeGroupRankings(LocalDate rankingPeriod, AgeGroup ageGroup, Gender gender,
                                     boolean isYobRanking,
                                     boolean overallRanking, boolean endOfYearRanking) {
    Listing result =
        new Listing(rankingPeriod, ageGroup, gender, isYobRanking, overallRanking,
            endOfYearRanking);
    List<Ranking> rankingsFromDb =
        rankingRepository.getRankingsForListing(rankingPeriod.plusDays(1), ageGroup, gender,
            isYobRanking, overallRanking, endOfYearRanking);

    List<ListingItem> listingItems = rankingsFromDb.stream()
        .map(r -> new ListingItem(r.getPosition(), r.getDtbId(), r.getFirstname(), r.getLastname(),
            r.getNationality(),
            r.getClub(), r.getFederation(), r.getPoints()))
        .toList();
    result.setListingItems(listingItems);
    return result;
  }
}
