package de.hdawg.rankinginfo.app.model.club;

/**
 * a model for a player in a club.
 *
 * @param dtbId     unique player id
 * @param lastname  lastname
 * @param firstname firstname
 * @param points    points in the current ranking period
 * @param position  position in the current ranking period
 */
public record ClubPlayer(String dtbId, String lastname, String firstname, String points,
                         int position) {

}
