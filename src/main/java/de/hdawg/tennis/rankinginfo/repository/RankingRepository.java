package de.hdawg.tennis.rankinginfo.repository;

import de.hdawg.tennis.rankinginfo.model.Federation;
import de.hdawg.tennis.rankinginfo.model.Ranking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class RankingRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<Ranking> retrieveRankingListItems(LocalDate period, boolean yearEndRanking, boolean gender, String ageGroup, Federation federation, String club) {
        return Collections.emptyList();
    }

}
