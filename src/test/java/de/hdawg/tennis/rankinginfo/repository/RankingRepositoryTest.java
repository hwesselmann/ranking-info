package de.hdawg.tennis.rankinginfo.repository;

import de.hdawg.tennis.rankinginfo.model.Ranking;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class RankingRepositoryTest {

    @Autowired
    RankingRepository rankingRepository;

    @Test
    public void noRankingsInDatabase() {
        List<Ranking> rankings = rankingRepository.retrieveRankingListItems(LocalDate.now(), false, false, "U14", null, "");
        assertEquals(Collections.emptyList(), rankings);
    }
}
