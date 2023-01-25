package de.hdawg.rankinginfo.service.model.listing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.util.List;



/**
 * container for the listings, formatted for the api response.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
