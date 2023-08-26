package de.hdawg.rankinginfo.rest.model.player;

/**
 * intermediate object presentation for json output.
 *
 * @param ageGroup         age group
 * @param changeInPosition changes in position (negative = up, positive = down)
 * @param changeInPoints   changes in position (negative = up, positive = down)
 */
public record Trend(String ageGroup, int changeInPosition, float changeInPoints) {
}
