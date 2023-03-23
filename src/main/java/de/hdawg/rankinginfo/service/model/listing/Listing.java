package de.hdawg.rankinginfo.service.model.listing;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;


/**
 * container for the listings, formatted for the api response.
 */

public class Listing {

  private LocalDate rankingPeriod;
  private String ageGroup;

  @JsonProperty("show only players from the year of birth from this age group")
  private Boolean yobRanking;

  @JsonProperty("also include younger players in calculation")
  private Boolean overallRanking;

  @JsonProperty("is final ranking for the year")
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
   * @param yobRanking       only players from this yob?
   * @param overallRanking   all players available?
   * @param endOfYearRanking is this the final ranking for this year?
   */
  public Listing(LocalDate rankingPeriod, String ageGroup, Boolean yobRanking, Boolean overallRanking, Boolean endOfYearRanking) {
    this.rankingPeriod = rankingPeriod;
    this.ageGroup = ageGroup;
    this.yobRanking = yobRanking;
    this.overallRanking = overallRanking;
    this.endOfYearRanking = endOfYearRanking;
  }

  /**
   * All args constructor.
   *
   * @param rankingPeriod    ranking period
   * @param ageGroup         age group
   * @param yobRanking       only players from this yob?
   * @param overallRanking   all players available?
   * @param endOfYearRanking is this the final ranking for this year?
   * @param listingItems     listing items containing the items of the listing
   */
  public Listing(LocalDate rankingPeriod, String ageGroup, Boolean yobRanking, Boolean overallRanking, Boolean endOfYearRanking, List<ListingItem> listingItems) {
    this.rankingPeriod = rankingPeriod;
    this.ageGroup = ageGroup;
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

  public String getAgeGroup() {
    return ageGroup;
  }

  public void setAgeGroup(String ageGroup) {
    this.ageGroup = ageGroup;
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
