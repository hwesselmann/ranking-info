package de.hdawg.rankinginfo.app.model.listing;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.hdawg.rankinginfo.model.AgeGroup;
import de.hdawg.rankinginfo.model.Gender;
import java.time.LocalDate;
import java.util.List;


/**
 * container for the listings, formatted for the api response.
 */

public class Listing {

  private LocalDate rankingPeriod;
  private AgeGroup ageGroup;
  private Gender gender;

  @JsonProperty("onlyPlayersFromYearOfBirthFromAgeGroup")
  private Boolean yobRanking;

  @JsonProperty("includeYoungerPlayers")
  private Boolean overallRanking;

  @JsonProperty("finalRankingForYear")
  private Boolean endOfYearRanking;

  private List<ListingItem> listingItems;

  /**
   * No args constructor.
   */
  public Listing() {
  }

  /**
   * Constructor for response initialization,leaving out lististem to be added after computing.
   *
   * @param rankingPeriod    ranking period
   * @param ageGroup         age group
   * @param gender           gender
   * @param yobRanking       only players from this yob?
   * @param overallRanking   all players available?
   * @param endOfYearRanking is this the final ranking for this year?
   */
  public Listing(LocalDate rankingPeriod, AgeGroup ageGroup, Gender gender, Boolean yobRanking,
      Boolean overallRanking, Boolean endOfYearRanking) {
    this.rankingPeriod = rankingPeriod;
    this.ageGroup = ageGroup;
    this.gender = gender;
    this.yobRanking = yobRanking;
    this.overallRanking = overallRanking;
    this.endOfYearRanking = endOfYearRanking;
  }

  /**
   * All args constructor.
   *
   * @param rankingPeriod    ranking period
   * @param ageGroup         age group
   * @param gender           gender
   * @param yobRanking       only players from this yob?
   * @param overallRanking   all players available?
   * @param endOfYearRanking is this the final ranking for this year?
   * @param listingItems     listing items containing the items of the listing
   */
  public Listing(LocalDate rankingPeriod, AgeGroup ageGroup, Gender gender, Boolean yobRanking,
      Boolean overallRanking, Boolean endOfYearRanking, List<ListingItem> listingItems) {
    this.rankingPeriod = rankingPeriod;
    this.ageGroup = ageGroup;
    this.gender = gender;
    this.yobRanking = yobRanking;
    this.overallRanking = overallRanking;
    this.endOfYearRanking = endOfYearRanking;
    this.listingItems = listingItems;
  }

  public LocalDate getRankingPeriod() {
    return rankingPeriod;
  }

  public void setRankingPeriod(LocalDate rankingPeriod) {
    this.rankingPeriod = rankingPeriod;
  }

  public AgeGroup getAgeGroup() {
    return ageGroup;
  }

  public void setAgeGroup(AgeGroup ageGroup) {
    this.ageGroup = ageGroup;
  }

  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  public Boolean getYobRanking() {
    return yobRanking;
  }

  public void setYobRanking(Boolean yobRanking) {
    this.yobRanking = yobRanking;
  }

  public Boolean getOverallRanking() {
    return overallRanking;
  }

  public void setOverallRanking(Boolean overallRanking) {
    this.overallRanking = overallRanking;
  }

  public Boolean getEndOfYearRanking() {
    return endOfYearRanking;
  }

  public void setEndOfYearRanking(Boolean endOfYearRanking) {
    this.endOfYearRanking = endOfYearRanking;
  }

  public List<ListingItem> getListingItems() {
    return listingItems;
  }

  public void setListingItems(List<ListingItem> listingItems) {
    this.listingItems = listingItems;
  }
}
