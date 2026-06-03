package de.hdawg.rankinginfo.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Ranking(
    long id,
    int dtbId,
    String lastname,
    String firstname,
    String nationality,
    String ageGroup,
    LocalDate date,
    int rankingPosition,
    String score,
    String club,
    String federation,
    boolean ageGroupRanking,
    boolean yobRanking,
    boolean yearEndRanking,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
