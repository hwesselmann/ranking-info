package de.hdawg.rankinginfo.api.v1.dto;

public record ClubPlayerItem(
    int dtb_id, String lastname, String firstname, int rank, String score, PlayerLink links) {}
