package de.hdawg.tennis.rankinginfo.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Federation {
    NONE(""),
    BAD("Baden"),
    BB("Berlin-Brandenburg"),
    BTV("Bayern"),
    HAM("Hamburg"),
    HTV("Hessen"),
    RPF("Rheinland-Pfalz"),
    SLH("Schleswig-Holstein"),
    STB("Saarland"),
    STV("Sachsen"),
    TMV("Mecklenburg-Vorpommern"),
    TNB("Niedersachsen-Bremen"),
    TSA("Sachsen-Anhalt"),
    TTV("Thüringen"),
    TVM("Mittelrhein"),
    TVN("Niederrhein"),
    WTB("Würtemberg"),
    WTV("Westfalen");

    @Getter
    private final String longName;
}
