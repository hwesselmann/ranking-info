package de.hdawg.rankinginfo.service.model.player;

import java.time.LocalDate;

public record PointsHistory(LocalDate rankingPeriod, String points) {
}
