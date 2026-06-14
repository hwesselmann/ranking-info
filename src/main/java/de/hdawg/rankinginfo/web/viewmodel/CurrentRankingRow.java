package de.hdawg.rankinginfo.web.viewmodel;

import org.jspecify.annotations.Nullable;

public record CurrentRankingRow(
    String ageGroup,
    int position,
    String score,
    @Nullable String positionChange,
    @Nullable String scoreChange,
    boolean isCurrentAgeGroup,
    boolean showScore,
    @Nullable Integer ageGroupRankingPosition) {}
