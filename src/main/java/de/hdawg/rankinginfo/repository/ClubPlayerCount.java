package de.hdawg.rankinginfo.repository;

/// Aggregated player counts for a single club at one ranking date, produced by a `GROUP BY club`
/// query so that club search never has to load the underlying ranking rows.
///
/// @param club the club name exactly as stored
/// @param youthCount number of youth ranking rows (`age_group = "overall"`)
/// @param adultCount number of adult ranking rows (`age_group` in `m00`/`w00`)
public record ClubPlayerCount(String club, long youthCount, long adultCount) {}
