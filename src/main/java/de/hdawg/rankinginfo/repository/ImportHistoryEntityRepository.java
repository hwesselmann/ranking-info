package de.hdawg.rankinginfo.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import de.hdawg.rankinginfo.persistence.ImportHistoryEntity;

interface ImportHistoryEntityRepository extends JpaRepository<ImportHistoryEntity, Long> {

  boolean existsByCategoryAndPeriod(String category, LocalDate period);

  @Query("SELECT MAX(h.importedAt) FROM ImportHistoryEntity h")
  @Nullable
  LocalDateTime findMaxImportedAt();
}
