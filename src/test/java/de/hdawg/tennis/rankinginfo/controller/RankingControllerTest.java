package de.hdawg.tennis.rankinginfo.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RankingControllerTest {

    @Test
    public void testAgeGroupOptionsProcessing() {
        RankingController rankingController = new RankingController(null);

        assertEquals("y", rankingController.applyAgeGroupOptions(11, ""));
        assertEquals("y", rankingController.applyAgeGroupOptions(11, "only_yob"));
        assertEquals("c", rankingController.applyAgeGroupOptions(11, "include_younger"));

        assertEquals("", rankingController.applyAgeGroupOptions(12, ""));
        assertEquals("y", rankingController.applyAgeGroupOptions(12, "only_yob"));
        assertEquals("c", rankingController.applyAgeGroupOptions(12, "include_younger"));

        assertEquals("y", rankingController.applyAgeGroupOptions(15, ""));
        assertEquals("y", rankingController.applyAgeGroupOptions(15, "only_yob"));
        assertEquals("c", rankingController.applyAgeGroupOptions(15, "include_younger"));

        assertEquals("", rankingController.applyAgeGroupOptions(18, ""));
        assertEquals("y", rankingController.applyAgeGroupOptions(18, "only_yob"));
        assertEquals("c", rankingController.applyAgeGroupOptions(18, "include_younger"));
    }
}
