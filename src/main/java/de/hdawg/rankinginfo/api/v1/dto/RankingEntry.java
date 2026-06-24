package de.hdawg.rankinginfo.api.v1.dto;

import java.time.LocalDate;

public record RankingEntry(LocalDate quarter, String age_group, int ranking_position, String score) {}
