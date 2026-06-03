package de.hdawg.rankinginfo.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import de.hdawg.rankinginfo.domain.ImportHistory;
import de.hdawg.rankinginfo.persistence.ImportHistoryEntity;

@Repository
public class ImportHistoryRepository {

  private final ImportHistoryEntityRepository jpa;

  public ImportHistoryRepository(ImportHistoryEntityRepository jpa) {
    this.jpa = jpa;
  }

  public List<ImportHistory> findAll() {
    return jpa.findAll().stream().map(ImportHistoryEntity::toDomain).toList();
  }

  public void deleteAll() {
    jpa.deleteAll();
  }

  public ImportHistory save(ImportHistory importHistory) {
    return jpa.save(ImportHistoryEntity.fromDomain(importHistory)).toDomain();
  }

  public boolean existsByCategoryAndPeriod(String category, LocalDate period) {
    return jpa.existsByCategoryAndPeriod(category, period);
  }

  @Nullable
  public LocalDateTime findMaxImportedAt() {
    return jpa.findMaxImportedAt();
  }
}
