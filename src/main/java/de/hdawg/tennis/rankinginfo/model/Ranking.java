package de.hdawg.tennis.rankinginfo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Ranking {
    private int id;
    private String dtbId;
    private String firstname;
    private String lastname;
    private Federation federation;
    private String club;
    private String nationality;
    private LocalDate period;
    private String gender;
    private int yob;
    private String points;

    private String ageGroup;
    private boolean includeAllPlayers;
    private boolean onlyYoBPlayers;
    private boolean endOfYearRanking;
    private int rank;
}
