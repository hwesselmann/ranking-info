package de.hdawg.rankinginfo.rest.model.club;

/**
 * a model for a player in a club.
 *
 * @param dtbId     unique player id
 * @param lastname  lastname
 * @param firstname firstname
 * @param club      club
 * @param points    points in the current ranking period
 * @param position  position in the current ranking period
 */
public record ClubPlayer(String dtbId, String lastname, String firstname, String club,
                         String points, int position) {
}
