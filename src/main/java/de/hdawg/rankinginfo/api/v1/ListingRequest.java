package de.hdawg.rankinginfo.api.v1;

public record ListingRequest(
    String quarter,
    String age_group,
    String age_group_options,
    String federation,
    String club,
    boolean year_end,
    int page,
    int per_page,
    long total_count) {}
