package de.hdawg.rankinginfo.api.v1.dto;

import java.util.List;

public record PlayerDetailData(
    int dtb_id,
    String lastname,
    String firstname,
    String nationality,
    String club,
    String federation,
    List<RankingEntry> rankings) {

  public PlayerDetailData {
    rankings = List.copyOf(rankings);
  }
}
