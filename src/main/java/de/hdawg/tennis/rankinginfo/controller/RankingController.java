package de.hdawg.tennis.rankinginfo.controller;

import de.hdawg.tennis.rankinginfo.model.Federation;
import de.hdawg.tennis.rankinginfo.model.Ranking;
import de.hdawg.tennis.rankinginfo.repository.RankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class RankingController {

    private final RankingRepository rankingRepository;

    /**
     * GET action preparing search view to show available datasets.
     *
     * @param model MVC data model
     * @return view to render
     */
    @GetMapping("/listings")
    public String getRankingListStartPage(Model model) {
        // TODO fetch available quarters for selection
        return "listing/start";
    }

    /**
     * POST action handling a search for a filtered rankings list.
     *
     * @param period          period to fetch
     * @param gender          Juniorinnen or Junioren?
     * @param ageGroup        age group to display, value can be between 11 and 18
     * @param ageGroupOptions extra options to show only players from the same year of birth or also include younger players.
     * @param federation      filter for players from a given federation (optional)
     * @param club            filter for players from a specific club (optional)
     * @param model           model object containing results
     * @return view name to render
     */
    @PostMapping("/listings")
    public String getRankingListing(@RequestParam(name = "quarter") String period, @RequestParam(name = "gender") String gender, @RequestParam(name = "age_group") String ageGroup, @RequestParam(name = "age_group_options", required = false) String ageGroupOptions, @RequestParam(name = "federation", required = false) String federation, @RequestParam(name = "club", required = false) String club, Model model) {
        log.debug("search triggered - processing search params");
        // TODO period and year-end-marker

        assert (ageGroup.length() == 3);

        Federation selectedFederation = Federation.NONE;
        if (!federation.isEmpty()) {
            selectedFederation = Federation.valueOf(federation);
        }
        // TODO club if present
        // is there an automatic mechanism to avoid sql injection?

        List<Ranking> rankings = rankingRepository.retrieveRankingListItems("", gender, ageGroup, selectedFederation, "BVH Tennis");
        model.addAttribute(rankings);

        return "listing/result";
    }
}
