package de.hdawg.tennis.rankinginfo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Ranking {
    private int id;
    private String period;
    private String dtbId;
    private String yob;
    private String firstname;
    private String lastname;
    private String club;
    private String federation;
    private String points;

    // rankings for each age group
    // without suffix = published official rank
    // c = complete ranking including younger players
    // y = ranking only in players of the specific age group
    private int rankOverall;
    private int rankU18;
    private int rankU18c;
    private int rankU18y;
    private int rankU17c;
    private int rankU17y;
    private int rankU16;
    private int rankU16c;
    private int rankU16y;
    private int rankU15c;
    private int rankU15y;
    private int rankU14;
    private int rankU14c;
    private int rankU14y;
    private int rankU13c;
    private int rankU13y;
    private int rankU12;
    private int rankU12c;
    private int rankU12y;
    private int rankU11y;
}
