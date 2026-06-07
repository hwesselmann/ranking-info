package de.hdawg.rankinginfo.web.viewmodel;

import java.util.List;

public record DiagramDataView(List<AgeGroupTimeSeries> positions, List<ScoreTimeSeries> scores) {}
