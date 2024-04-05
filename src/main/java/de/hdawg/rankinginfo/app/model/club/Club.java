package de.hdawg.rankinginfo.app.model.club;

import de.hdawg.rankinginfo.model.Federation;

/**
 * domain object for a club.
 *
 * @param name       name of the club
 * @param federation federation
 */
public record Club(String name, Federation federation) {

}
