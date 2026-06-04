package de.hdawg.rankinginfo.api.v1;

public record ListingItem(
    int dtb_id,
    int ranking_position,
    String lastname,
    String firstname,
    String nationality,
    String club,
    String federation,
    String score,
    Integer position_change,
    PlayerLink links) {}
