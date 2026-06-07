package de.hdawg.rankinginfo.web.viewmodel;

import org.jspecify.annotations.Nullable;

public record PlayerSummaryRow(
    int dtbId,
    String lastname,
    String firstname,
    int rank,
    String score,
    @Nullable String ageGroup) {}
