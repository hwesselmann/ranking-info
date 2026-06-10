package de.hdawg.rankinginfo.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import de.hdawg.rankinginfo.domain.Ranking;

@Table("rankings")
public class RankingEntity {

  @Id
  @Nullable
  private final Long id;

  @Column("dtb_id")
  private final Integer dtbId;

  @Column("lastname")
  private final String lastname;

  @Column("firstname")
  private final String firstname;

  @Column("nationality")
  private final String nationality;

  @Column("age_group")
  private final String ageGroup;

  @Column("date")
  private final LocalDate date;

  @Column("ranking_position")
  private final Integer rankingPosition;

  @Column("score")
  private final String score;

  @Column("club")
  private final String club;

  @Column("federation")
  private final String federation;

  @Column("age_group_ranking")
  private final Boolean ageGroupRanking;

  @Column("yob_ranking")
  private final Boolean yobRanking;

  @Column("year_end_ranking")
  private final Boolean yearEndRanking;

  @Column("created_at")
  private final LocalDateTime createdAt;

  @Column("updated_at")
  private final LocalDateTime updatedAt;

  @PersistenceCreator
  RankingEntity(
      @Nullable Long id,
      Integer dtbId,
      String lastname,
      String firstname,
      String nationality,
      String ageGroup,
      LocalDate date,
      Integer rankingPosition,
      String score,
      String club,
      String federation,
      Boolean ageGroupRanking,
      Boolean yobRanking,
      Boolean yearEndRanking,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.dtbId = dtbId;
    this.lastname = lastname;
    this.firstname = firstname;
    this.nationality = nationality;
    this.ageGroup = ageGroup;
    this.date = date;
    this.rankingPosition = rankingPosition;
    this.score = score;
    this.club = club;
    this.federation = federation;
    this.ageGroupRanking = ageGroupRanking;
    this.yobRanking = yobRanking;
    this.yearEndRanking = yearEndRanking;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static RankingEntity fromDomain(Ranking r) {
    var now = LocalDateTime.now(ZoneOffset.UTC);
    return new RankingEntity(
        r.id() != 0 ? r.id() : null,
        r.dtbId(),
        r.lastname(),
        r.firstname(),
        r.nationality(),
        r.ageGroup(),
        r.date(),
        r.rankingPosition(),
        r.score(),
        r.club(),
        r.federation(),
        r.ageGroupRanking(),
        r.yobRanking(),
        r.yearEndRanking(),
        now,
        now);
  }

  public Ranking toDomain() {
    return new Ranking(
        id == null ? 0L : id,
        dtbId,
        lastname,
        firstname,
        nationality,
        ageGroup,
        date,
        rankingPosition,
        score,
        club,
        federation,
        ageGroupRanking,
        yobRanking,
        yearEndRanking);
  }
}
