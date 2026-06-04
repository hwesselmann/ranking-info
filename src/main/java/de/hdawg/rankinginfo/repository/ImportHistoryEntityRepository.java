package de.hdawg.rankinginfo.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import de.hdawg.rankinginfo.persistence.ImportHistoryEntity;

interface ImportHistoryEntityRepository extends ListCrudRepository<ImportHistoryEntity, Long> {

  boolean existsByCategoryAndPeriod(String category, LocalDate period);

  @Query("SELECT MAX(imported_at) FROM import_histories")
  @Nullable
  LocalDateTime findMaxImportedAt();
}
