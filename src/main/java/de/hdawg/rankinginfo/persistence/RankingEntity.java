package de.hdawg.rankinginfo.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import de.hdawg.rankinginfo.domain.Ranking;

@Table("rankings")
public class RankingEntity {

  @Id
  private Long id;

  @Column("dtb_id")
  private Integer dtbId;

  @Column("lastname")
  private String lastname;

  @Column("firstname")
  private String firstname;

  @Column("nationality")
  private String nationality;

  @Column("age_group")
  private String ageGroup;

  @Column("date")
  private LocalDate date;

  @Column("ranking_position")
  private Integer rankingPosition;

  @Column("score")
  private String score;

  @Column("club")
  private String club;

  @Column("federation")
  private String federation;

  @Column("age_group_ranking")
  private Boolean ageGroupRanking;

  @Column("yob_ranking")
  private Boolean yobRanking;

  @Column("year_end_ranking")
  private Boolean yearEndRanking;

  @Column("created_at")
  private LocalDateTime createdAt;

  @Column("updated_at")
  private LocalDateTime updatedAt;

  RankingEntity() {}

  public static RankingEntity fromDomain(Ranking r) {
    var e = new RankingEntity();
    if (r.id() != 0) {
      e.id = r.id();
    }
    e.dtbId = r.dtbId();
    e.lastname = r.lastname();
    e.firstname = r.firstname();
    e.nationality = r.nationality();
    e.ageGroup = r.ageGroup();
    e.date = r.date();
    e.rankingPosition = r.rankingPosition();
    e.score = r.score();
    e.club = r.club();
    e.federation = r.federation();
    e.ageGroupRanking = r.ageGroupRanking();
    e.yobRanking = r.yobRanking();
    e.yearEndRanking = r.yearEndRanking();
    e.createdAt = r.createdAt();
    e.updatedAt = r.updatedAt();
    return e;
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
        yearEndRanking,
        createdAt,
        updatedAt);
  }
}
