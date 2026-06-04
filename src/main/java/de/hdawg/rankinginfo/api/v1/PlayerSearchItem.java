package de.hdawg.rankinginfo.api.v1;

public record PlayerSearchItem(
    int dtb_id, String lastname, String firstname, String club, PlayerLink links) {}
