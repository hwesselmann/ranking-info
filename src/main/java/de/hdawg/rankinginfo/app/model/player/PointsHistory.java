package de.hdawg.rankinginfo.app.model.player;

import java.time.LocalDate;

public record PointsHistory(LocalDate rankingPeriod, String points) {

}
