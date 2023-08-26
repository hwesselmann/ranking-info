package de.hdawg.rankinginfo.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RankingTest {

  @DisplayName("verify the generated equal-method is correct")
  @Test
  void verifyEquals() {
    Ranking sutA = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    boolean result = sutA.equals(null);
    assertFalse(result);

    Ranking sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertTrue(result);

    sutB = new Ranking(LocalDate.of(2022, 2, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(null, "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "123456789", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), null, "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Musterfrau",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", null,
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Klaus", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        null, "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "21", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", null, Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.AUT, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", null, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.WTV, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, null, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "TC Musterstadt", "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, null, "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U13", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", null, 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 21,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", null,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);
  }

  @DisplayName("verify the generated equal-method is correct for boolean values")
  @Test
  void verifyEqualsForBooleanValues() {
    Ranking sutA = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);

    Ranking sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        true, false, false);
    boolean result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        null, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, true, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, null, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, true);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, null);
    result = sutA.equals(sutB);
    assertFalse(result);
  }

  @DisplayName("verify the generated equal-method is correct reverse")
  @Test
  void verifyEqualsReverse() {
    Ranking sutA = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);

    boolean result = sutA.equals(null);
    assertFalse(result);

    Ranking sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutB.equals(sutA);
    assertTrue(result);

    sutB = new Ranking(LocalDate.of(2022, 2, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutB.equals(sutA);
    assertFalse(result);

    sutB = new Ranking(null, "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutB.equals(sutA);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "123456789", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), null, "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutB.equals(sutA);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Musterfrau",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutB.equals(sutA);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", null,
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutB.equals(sutA);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Klaus", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutB.equals(sutA);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        null, "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutB.equals(sutA);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "21", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutB.equals(sutA);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", null, Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutB.equals(sutA);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.AUT, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutB.equals(sutA);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", null, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutB.equals(sutA);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.WTV, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutB.equals(sutA);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, null, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutB.equals(sutA);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "TC Musterstadt", "U12", 20,
        false, false, false);
    result = sutB.equals(sutA);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, null, "U12", 20,
        false, false, false);
    result = sutB.equals(sutA);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U13", 20,
        false, false, false);
    result = sutB.equals(sutA);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", null, 20,
        false, false, false);
    result = sutB.equals(sutA);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 21,
        false, false, false);
    result = sutB.equals(sutA);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", null,
        false, false, false);
    result = sutB.equals(sutA);
    assertFalse(result);
  }

  @DisplayName("verify the generated equal-method is correct for boolean values in reverse mode")
  @Test
  void verifyEqualsForBooleanValuesReverse() {
    Ranking sutA = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);

    Ranking sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        true, false, false);
    boolean result = sutB.equals(sutA);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        null, false, false);
    result = sutB.equals(sutA);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, true, false);
    result = sutB.equals(sutA);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, null, false);
    result = sutB.equals(sutA);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, true);
    result = sutB.equals(sutA);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, null);
    result = sutB.equals(sutA);
    assertFalse(result);
  }

  @DisplayName("verify the generated equal-method is correct if some compared values are both null")
  @Test
  void verifyEqualsForNullValues() {
    Ranking sutA = new Ranking(null, null, null,
        null, null, null, null, null, null, null,
        null, null, null);

    Ranking sutB = new Ranking(null, "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    boolean result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), null, "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", null,
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        null, "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", null, Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", null, Federation.BB, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, null, "Musterstadt", "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, null, "U12", 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", null, 20,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", null,
        false, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        null, false, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, null, false);
    result = sutA.equals(sutB);
    assertFalse(result);

    sutB = new Ranking(LocalDate.of(2022, 1, 1), "12345678", "Mustermann",
        "Max", "22", Nationality.GER, Federation.BB, "Musterstadt", "U12", 20,
        false, false, null);
    result = sutA.equals(sutB);
    assertFalse(result);
  }
}
