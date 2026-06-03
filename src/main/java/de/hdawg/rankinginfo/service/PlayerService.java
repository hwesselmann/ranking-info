package de.hdawg.rankinginfo.service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.hdawg.rankinginfo.domain.Club;
import de.hdawg.rankinginfo.domain.Player;
import de.hdawg.rankinginfo.domain.Ranking;
import de.hdawg.rankinginfo.repository.RankingRepository;

@Service
@Transactional(readOnly = true)
public class PlayerService {

  private final RankingRepository rankingRepository;

  public PlayerService(RankingRepository rankingRepository) {
    this.rankingRepository = rankingRepository;
  }

  public Player loadPlayerProfile(int dtbId) {
    var allRankings =
        rankingRepository.findByDtbIdAndAgeGroupInOrderByDateDesc(
            dtbId, List.of("overall", "m00", "w00"));
    var current = allRankings.get(0);
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
    return deduplicateByDtbId(rankingRepository.findByLastnameLike(lastname));
  }

  public List<Ranking> findPlayersByLastnameAndYob(String lastname, int yobMale, int yobFemale) {
    return deduplicateByDtbId(
        rankingRepository.findByLastnameAndYob(
            lastname,
            yobMale * 100_000,
            yobMale * 100_000 + 99_999,
            yobFemale * 100_000,
            yobFemale * 100_000 + 99_999));
  }

  public List<Ranking> findPlayersByYob(int yobMale, int yobFemale) {
    return deduplicateByDtbId(
        rankingRepository.findByYob(
            yobMale * 100_000,
            yobMale * 100_000 + 99_999,
            yobFemale * 100_000,
            yobFemale * 100_000 + 99_999));
  }

  public List<Ranking> findPlayersByDtbIdRange(int dtbIdStart, int dtbIdEnd) {
    return deduplicateByDtbId(rankingRepository.findByDtbIdRange(dtbIdStart, dtbIdEnd));
  }

  private List<Ranking> deduplicateByDtbId(List<Ranking> rankings) {
    var seen = new HashSet<Integer>();
    return rankings.stream().filter(r -> seen.add(r.dtbId())).toList();
  }
}
