package de.hdawg.rankinginfo.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import de.hdawg.rankinginfo.domain.Ranking;

@Entity
@Table(name = "rankings")
public class RankingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "dtb_id")
  private Integer dtbId;

  private String lastname;
  private String firstname;
  private String nationality;

  @Column(name = "age_group")
  private String ageGroup;

  private LocalDate date;

  @Column(name = "ranking_position")
  private Integer rankingPosition;

  private String score;
  private String club;
  private String federation;

  @Column(name = "age_group_ranking")
  private Boolean ageGroupRanking;

  @Column(name = "yob_ranking")
  private Boolean yobRanking;

  @Column(name = "year_end_ranking")
  private Boolean yearEndRanking;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  protected RankingEntity() {}

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
