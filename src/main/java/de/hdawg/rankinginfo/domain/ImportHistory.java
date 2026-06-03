package de.hdawg.rankinginfo.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ImportHistory(
    long id,
    String filename,
    String category,
    LocalDate period,
    LocalDateTime importedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
