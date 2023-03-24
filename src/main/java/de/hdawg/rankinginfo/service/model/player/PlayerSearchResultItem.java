package de.hdawg.rankinginfo.service.model.player;

import de.hdawg.rankinginfo.service.model.Federation;
import de.hdawg.rankinginfo.service.model.Nationality;

public record PlayerSearchResultItem(String dtbId, String firstname, String lastname, Federation federation,
                                     Nationality nationality, String club) {
}
