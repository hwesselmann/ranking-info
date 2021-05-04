package de.hdawg.tennis.rankinginfo.repository;

import de.hdawg.tennis.rankinginfo.model.Federation;
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
    public void fetchPlainRankings() {
        List<Ranking> rankings = rankingRepository.retrieveRankingListItems("2020-03-31", "m", "all", Federation.NONE, "");
        assertEquals(21, rankings.size());
        assertEquals("213", rankings.get(0).getPoints());

        rankings = rankingRepository.retrieveRankingListItems("2020-03-31", "m", "U12", Federation.NONE, "");
        assertEquals(2, rankings.size());
    }

    @Test
    public void fetchRankingsIncludingFederation() {
        List<Ranking> rankings = rankingRepository.retrieveRankingListItems("2020-03-31", "m", "all", Federation.WTV, "");
        assertEquals(4, rankings.size());
        assertEquals("213", rankings.get(0).getPoints());

        rankings = rankingRepository.retrieveRankingListItems("2020-03-31", "m", "all", Federation.BAD, "");
        assertEquals(1, rankings.size());
    }

    @Test
    public void fetchRankingsIncludingClub() {
        List<Ranking> rankings = rankingRepository.retrieveRankingListItems("2020-03-31", "m", "all", Federation.NONE, "TC Muster");
        assertEquals(2, rankings.size());

        rankings = rankingRepository.retrieveRankingListItems("2020-03-31", "m", "all", Federation.NONE, " Musterhausen ");
        assertEquals(8, rankings.size());

        rankings = rankingRepository.retrieveRankingListItems("2020-03-31", "m", "all", Federation.NONE, "VfL");
        assertEquals(2, rankings.size());
    }

    @Test
    public void fetchRankingsIncludingFederationAndClub() {
        List<Ranking> rankings = rankingRepository.retrieveRankingListItems("2020-03-31", "m", "all", Federation.WTV, "TC Muster");
        assertEquals(1, rankings.size());

        rankings = rankingRepository.retrieveRankingListItems("2020-03-31", "m", "all", Federation.BB, " Musterhausen ");
        assertEquals(0, rankings.size());

        rankings = rankingRepository.retrieveRankingListItems("2020-03-31", "m", "all", Federation.WTV, "VfL");
        assertEquals(1, rankings.size());
    }
}
