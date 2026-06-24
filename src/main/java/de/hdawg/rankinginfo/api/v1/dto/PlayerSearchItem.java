package de.hdawg.rankinginfo.api.v1.dto;

public record PlayerSearchItem(
    int dtb_id, String lastname, String firstname, String club, PlayerLink links) {}
