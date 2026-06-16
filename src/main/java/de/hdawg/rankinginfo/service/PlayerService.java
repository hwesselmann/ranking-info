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

/// Player lookup and search support, querying [RankingRepository] directly for lightweight
/// listings (search by name, by birth year, or by partial DTB ID) and a minimal player profile.
///
/// For a player's full profile page (rankings history, diagrams), the web tier's
/// `PlayerProfileService` is used instead.
@Service
@Transactional(readOnly = true)
public class PlayerService {

  private static final int DTB_ID_LENGTH = 8;

  private final RankingRepository rankingRepository;

  public PlayerService(RankingRepository rankingRepository) {
    this.rankingRepository = rankingRepository;
  }

  /// Expands a partial DTB ID into the full ID range it could complete to, by padding the
  /// digits typed so far out to 8 digits.
  ///
  /// @param dtbId digits typed by the user, e.g. `"12"`; unparseable input is treated as `0`
  /// @return a two-element array `{start, end}` spanning every full DTB ID with `dtbId` as a
  ///     prefix
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

  /// Converts a 4-digit birth year into the male YOB+gender marker (matching the leading
  /// digits of male [RankingCoding#YOB_MULTIPLIER]-encoded DTB IDs).
  ///
  /// @param yob a 4-digit birth year, e.g. `"1995"`
  /// @return the marker for the male cohort born in `yob`; add `100` to get the corresponding
  ///     female marker
  public int yobToMaleMarker(String yob) {
    return Integer.parseInt(yob.trim().substring(2, 4)) + 100;
  }

  /// Loads a minimal player profile: identity fields plus every club the player has appeared
  /// under, derived from their adult/youth-overall ranking rows.
  ///
  /// @param dtbId the player's DTB ID
  /// @return the player's profile
  /// @throws NoSuchElementException if no ranking rows exist for `dtbId`
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

  /// Searches players whose last name matches `lastname`.
  ///
  /// @param lastname the last name to search for
  /// @return matching ranking rows, one per player
  public List<Ranking> findPlayersByLastname(String lastname) {
    return rankingRepository.findByLastnameLike(lastname);
  }

  /// Searches players whose last name matches `lastname` and whose DTB ID falls in the male or
  /// female cohort range for the given YOB+gender markers.
  ///
  /// @param lastname the last name to search for
  /// @param yobMale the male YOB+gender marker (see [#yobToMaleMarker(String)])
  /// @param yobFemale the female YOB+gender marker
  /// @return matching ranking rows, one per player
  public List<Ranking> findPlayersByLastnameAndYob(String lastname, int yobMale, int yobFemale) {
    return rankingRepository.findByLastnameAndYob(
        lastname,
        yobMale * RankingCoding.YOB_MULTIPLIER,
        yobMale * RankingCoding.YOB_MULTIPLIER + RankingCoding.YOB_MULTIPLIER - 1,
        yobFemale * RankingCoding.YOB_MULTIPLIER,
        yobFemale * RankingCoding.YOB_MULTIPLIER + RankingCoding.YOB_MULTIPLIER - 1);
  }

  /// Searches players whose DTB ID falls in the male or female cohort range for the given
  /// YOB+gender markers.
  ///
  /// @param yobMale the male YOB+gender marker (see [#yobToMaleMarker(String)])
  /// @param yobFemale the female YOB+gender marker
  /// @return matching ranking rows, one per player
  public List<Ranking> findPlayersByYob(int yobMale, int yobFemale) {
    return rankingRepository.findByYob(
        yobMale * RankingCoding.YOB_MULTIPLIER,
        yobMale * RankingCoding.YOB_MULTIPLIER + RankingCoding.YOB_MULTIPLIER - 1,
        yobFemale * RankingCoding.YOB_MULTIPLIER,
        yobFemale * RankingCoding.YOB_MULTIPLIER + RankingCoding.YOB_MULTIPLIER - 1);
  }

  /// Fetches a player's raw (non age-group-bracket, non year-of-birth, non year-end) ranking
  /// rows.
  ///
  /// @param dtbId the player's DTB ID
  /// @return matching ranking rows, most recent quarter first
  public List<Ranking> findNonAggregateRankings(int dtbId) {
    return rankingRepository
        .findByDtbIdAndYobRankingFalseAndAgeGroupRankingFalseAndYearEndRankingFalseOrderByDateDescAgeGroupAsc(
            dtbId);
  }

  /// Searches players whose DTB ID falls within `[dtbIdStart, dtbIdEnd]`, typically built via
  /// [#dtbIdRange(String)].
  ///
  /// @param dtbIdStart inclusive lower bound
  /// @param dtbIdEnd inclusive upper bound
  /// @return matching ranking rows, one per player
  public List<Ranking> findPlayersByDtbIdRange(int dtbIdStart, int dtbIdEnd) {
    return rankingRepository.findByDtbIdRange(dtbIdStart, dtbIdEnd);
  }
}
