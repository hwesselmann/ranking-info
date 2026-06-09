package de.hdawg.rankinginfo.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.hdawg.rankinginfo.domain.Club;
import de.hdawg.rankinginfo.domain.Player;
import de.hdawg.rankinginfo.domain.Ranking;
import de.hdawg.rankinginfo.domain.RankingCoding;
import de.hdawg.rankinginfo.repository.RankingRepository;

@Service
@Transactional(readOnly = true)
public class PlayerService {

  private static final int DTB_ID_LENGTH = 8;

  private final RankingRepository rankingRepository;

  public PlayerService(RankingRepository rankingRepository) {
    this.rankingRepository = rankingRepository;
  }

  public int[] dtbIdRange(String dtbId) {
    long idNum;
    try {
      idNum = Long.parseLong(dtbId.trim());
    } catch (NumberFormatException e) {
      idNum = 0L;
    }
    int missing = DTB_ID_LENGTH - Long.toString(idNum).length();
    int factor = 1;
    for (int i = 0; i < missing; i++) factor *= 10;
    int start = (int) (idNum * factor);
    int end = start + factor - 1;
    return new int[] {start, end};
  }

  public int yobToMaleMarker(String yob) {
    return Integer.parseInt(yob.trim().substring(2, 4)) + 100;
  }

  public Player loadPlayerProfile(int dtbId) {
    var allRankings =
        rankingRepository.findByDtbIdAndAgeGroupInOrderByDateDesc(
            dtbId, List.of("overall", "m00", "w00"));
    if (allRankings.isEmpty()) {
      throw new NoSuchElementException("No rankings found for player " + dtbId);
    }
    var current = allRankings.getFirst();
    var clubs =
        allRankings.stream()
            .map(r -> new Club(r.club(), r.federation()))
            .collect(Collectors.toMap(Club::name, c -> c, (a, b) -> a))
            .values()
            .stream()
            .sorted(java.util.Comparator.comparing(Club::name))
            .toList();
    return new Player(
        dtbId,
        current.lastname(),
        current.firstname(),
        current.nationality(),
        current.club(),
        current.federation(),
        clubs);
  }

  public List<Ranking> findPlayersByLastname(String lastname) {
    return rankingRepository.findByLastnameLike(lastname);
  }

  public List<Ranking> findPlayersByLastnameAndYob(String lastname, int yobMale, int yobFemale) {
    return rankingRepository.findByLastnameAndYob(
        lastname,
        yobMale * RankingCoding.YOB_MULTIPLIER,
        yobMale * RankingCoding.YOB_MULTIPLIER + RankingCoding.YOB_MULTIPLIER - 1,
        yobFemale * RankingCoding.YOB_MULTIPLIER,
        yobFemale * RankingCoding.YOB_MULTIPLIER + RankingCoding.YOB_MULTIPLIER - 1);
  }

  public List<Ranking> findPlayersByYob(int yobMale, int yobFemale) {
    return rankingRepository.findByYob(
        yobMale * RankingCoding.YOB_MULTIPLIER,
        yobMale * RankingCoding.YOB_MULTIPLIER + RankingCoding.YOB_MULTIPLIER - 1,
        yobFemale * RankingCoding.YOB_MULTIPLIER,
        yobFemale * RankingCoding.YOB_MULTIPLIER + RankingCoding.YOB_MULTIPLIER - 1);
  }

  public List<Ranking> findNonAggregateRankings(int dtbId) {
    return rankingRepository
        .findByDtbIdAndYobRankingFalseAndAgeGroupRankingFalseAndYearEndRankingFalseOrderByDateDescAgeGroupAsc(
            dtbId);
  }

  public List<Ranking> findPlayersByDtbIdRange(int dtbIdStart, int dtbIdEnd) {
    return rankingRepository.findByDtbIdRange(dtbIdStart, dtbIdEnd);
  }
}
