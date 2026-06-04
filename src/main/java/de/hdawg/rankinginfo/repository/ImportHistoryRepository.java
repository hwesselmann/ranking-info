package de.hdawg.rankinginfo.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;

import de.hdawg.rankinginfo.domain.ImportHistory;
import de.hdawg.rankinginfo.persistence.ImportHistoryEntity;

@Repository
public class ImportHistoryRepository {

  private final ImportHistoryEntityRepository jdbc;

  public ImportHistoryRepository(ImportHistoryEntityRepository jdbc) {
    this.jdbc = jdbc;
  }

  public List<ImportHistory> findAll() {
    return jdbc.findAll().stream().map(ImportHistoryEntity::toDomain).toList();
  }

  public void deleteAll() {
    jdbc.deleteAll();
  }

  public ImportHistory save(ImportHistory importHistory) {
    return jdbc.save(ImportHistoryEntity.fromDomain(importHistory)).toDomain();
  }

  public boolean existsByCategoryAndPeriod(String category, LocalDate period) {
    return jdbc.existsByCategoryAndPeriod(category, period);
  }

  @Nullable
  public LocalDateTime findMaxImportedAt() {
    return jdbc.findMaxImportedAt();
  }
}
