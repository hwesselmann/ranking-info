package de.hdawg.rankinginfo.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import de.hdawg.rankinginfo.domain.ImportHistory;

@Table("import_histories")
public class ImportHistoryEntity {

  @Id
  @Nullable
  private final Long id;

  @Column("filename")
  private final String filename;

  @Column("category")
  private final String category;

  @Column("period")
  private final LocalDate period;

  @Column("imported_at")
  private final LocalDateTime importedAt;

  @Column("created_at")
  private final LocalDateTime createdAt;

  @Column("updated_at")
  private final LocalDateTime updatedAt;

  @PersistenceCreator
  ImportHistoryEntity(
      @Nullable Long id,
      String filename,
      String category,
      LocalDate period,
      LocalDateTime importedAt,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.filename = filename;
    this.category = category;
    this.period = period;
    this.importedAt = importedAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static ImportHistoryEntity fromDomain(ImportHistory h) {
    return new ImportHistoryEntity(
        h.id() != 0 ? h.id() : null,
        h.filename(),
        h.category(),
        h.period(),
        h.importedAt(),
        h.createdAt(),
        h.updatedAt());
  }

  public ImportHistory toDomain() {
    return new ImportHistory(
        id == null ? 0L : id, filename, category, period, importedAt, createdAt, updatedAt);
  }
}
