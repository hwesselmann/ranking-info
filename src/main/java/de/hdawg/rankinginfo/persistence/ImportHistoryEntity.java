package de.hdawg.rankinginfo.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import de.hdawg.rankinginfo.domain.ImportHistory;

@Entity
@Table(name = "import_histories")
public class ImportHistoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String filename;
  private String category;
  private LocalDate period;

  @Column(name = "imported_at")
  private LocalDateTime importedAt;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  protected ImportHistoryEntity() {}

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
