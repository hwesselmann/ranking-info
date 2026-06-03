package de.hdawg.rankinginfo.domain;

import java.util.List;

public record Player(
    int dtbId,
    String lastname,
    String firstname,
    String nationality,
    String club,
    String federation,
    List<Club> clubs) {}
