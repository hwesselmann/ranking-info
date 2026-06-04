package de.hdawg.rankinginfo.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import de.hdawg.rankinginfo.domain.ImportHistory;

@Table("import_histories")
public class ImportHistoryEntity {

  @Id
  private Long id;

  @Column("filename")
  private String filename;

  @Column("category")
  private String category;

  @Column("period")
  private LocalDate period;

  @Column("imported_at")
  private LocalDateTime importedAt;

  @Column("created_at")
  private LocalDateTime createdAt;

  @Column("updated_at")
  private LocalDateTime updatedAt;

  ImportHistoryEntity() {}

  public static ImportHistoryEntity fromDomain(ImportHistory h) {
    var e = new ImportHistoryEntity();
    if (h.id() != 0) {
      e.id = h.id();
    }
    e.filename = h.filename();
    e.category = h.category();
    e.period = h.period();
    e.importedAt = h.importedAt();
    e.createdAt = h.createdAt();
    e.updatedAt = h.updatedAt();
    return e;
  }

  public ImportHistory toDomain() {
    return new ImportHistory(
        id == null ? 0L : id, filename, category, period, importedAt, createdAt, updatedAt);
  }
}
