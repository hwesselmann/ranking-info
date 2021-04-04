package de.hdawg.tennis.rankinginfo.repository;

import de.hdawg.tennis.rankinginfo.model.Federation;
import de.hdawg.tennis.rankinginfo.model.Ranking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class RankingRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Fetch rankings by applying search criteria if applicable.
     *
     * @param period the period a ranking list is requested for
     * @param gender 'Juniorinnen' or 'Junioren'
     * @param ageGroup age group to fetch rankings for including modifiers
     * @param federation filter for a specific federation
     * @param club filter for clubs
     * @return a list of requested rankings (in the right order)
     */
    public List<Ranking> retrieveRankingListItems(LocalDate period, boolean gender, String ageGroup, Federation federation, String club) {
        return Collections.emptyList();
    }

}
