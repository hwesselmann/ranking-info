package de.hdawg.rankinginfo.rest.model.player;

import java.time.LocalDate;

public record PointsHistory(LocalDate rankingPeriod, String points) {
}
