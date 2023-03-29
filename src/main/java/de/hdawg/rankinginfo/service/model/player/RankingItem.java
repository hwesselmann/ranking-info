package de.hdawg.rankinginfo.service.model.player;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;

/**
 * intermediate dto for player details.
 *
 * @param rankingPeriod   ranking period
 * @param ageGroup        age group
 * @param yobOnly         includes only from this yob
 * @param overall         includes younger players
 * @param endOfYear       is a final ranking for the year
 * @param points          points
 * @param rankingPosition current position
 */
public record RankingItem(LocalDate rankingPeriod,
                          String ageGroup,
                          @JsonIgnore boolean yobOnly,
                          @JsonIgnore boolean overall,
                          @JsonIgnore boolean endOfYear,
                          @JsonIgnore String points,
                          int rankingPosition) {
}
