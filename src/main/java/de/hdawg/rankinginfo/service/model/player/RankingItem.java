package de.hdawg.rankinginfo.service.model.player;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;

public record RankingItem(LocalDate rankingPeriod,
                          String ageGroup,
                          @JsonIgnore boolean yobOnly,
                          @JsonIgnore boolean overall,
                          @JsonIgnore boolean endOfYear,
                          int rankingPosition) {
}
