package de.hdawg.rankinginfo.service.model.listing;

import de.hdawg.rankinginfo.service.model.Federation;
import de.hdawg.rankinginfo.service.model.Nationality;

/**
 * record class holding data relevant for the api response.
 *
 * @param position    position on the ranking
 * @param dtbId       dtbid
 * @param firstname   firstname
 * @param lastname    lastname
 * @param nationality nationality
 * @param club        club
 * @param federation  federation
 * @param points      points or pr value
 */
public record ListingItem(Integer position, String dtbId, String firstname, String lastname, Nationality nationality,
                          String club, Federation federation, String points) {
}
