package de.hdawg.rankinginfo.web;

import org.jspecify.annotations.Nullable;

public record RankingRow(
    int rowNumber,
    int rankingPosition,
    @Nullable String positionChange,
    String displayName,
    String dtbId,
    String playerUrl,
    String flagSrc,
    String nationality,
    String club,
    String clubUrl,
    String federation,
    String score) {}
