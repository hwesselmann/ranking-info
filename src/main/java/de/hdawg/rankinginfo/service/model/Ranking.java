package de.hdawg.rankinginfo.service.model;

import java.time.LocalDate;

/**
 * Record for easy straightforward reading of rankings from the database.
 *
 * @param rankingPeriod       ranking period
 * @param dtbId               dtb id
 * @param lastname            lastname
 * @param firstname           firstname
 * @param points              points for this ranking
 * @param nationality         nationality
 * @param federation          federation
 * @param club                club
 * @param ageGroup            age group of this ranking
 * @param position            position according to parameters
 * @param yearOfBirthRanking  listing parameter
 * @param overallYouthRanking listing parameter
 * @param endOfYearRanking    listing parameter
 */
public record Ranking(LocalDate rankingPeriod, String dtbId, String lastname, String firstname,
                      String points, Nationality nationality, Federation federation, String club,
                      String ageGroup, Integer position, Boolean yearOfBirthRanking,
                      Boolean overallYouthRanking, Boolean endOfYearRanking) {
}
