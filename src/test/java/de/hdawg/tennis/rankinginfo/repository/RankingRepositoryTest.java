package de.hdawg.tennis.rankinginfo.repository;

import de.hdawg.tennis.rankinginfo.model.Ranking;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class RankingRepositoryTest {

    @Autowired
    RankingRepository rankingRepository;

    @Test
    public void fetchRankingsFromDatabase() {
        List<Ranking> rankings = rankingRepository.retrieveRankingListItems("2020-03-31", "m", "all", null, "");
        assertEquals(21, rankings.size());
        assertEquals("213", rankings.get(0).getPoints());
    }
}
