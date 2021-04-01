package de.hdawg.tennis.rankinginfo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class RankingController {

    @GetMapping("/listings")
    public String getRankingList(Model model) {
        return "listing";
    }
}
