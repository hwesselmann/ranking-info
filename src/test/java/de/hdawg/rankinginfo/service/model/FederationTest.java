package de.hdawg.rankinginfo.service.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FederationTest {

  @DisplayName("test federations and mappings")
  @Test
  void checkFederations() {
    assertEquals("Berlin-Brandenburg", Federation.BB.value());
    assertEquals("Bayern", Federation.BTV.value());
    assertEquals("Baden", Federation.BAD.value());
    assertEquals("Hamburg", Federation.HAM.value());
    assertEquals("Schleswig-Holstein", Federation.SLH.value());
    assertEquals("Rheinland-Pfalz", Federation.RPF.value());
    assertEquals("Hessen", Federation.HTV.value());
    assertEquals("Saarland", Federation.STB.value());
    assertEquals("Sachsen", Federation.STV.value());
    assertEquals("Westfalen", Federation.WTV.value());
    assertEquals("Würtemberg", Federation.WTB.value());
    assertEquals("Mecklenburg-Vorpommern", Federation.TMV.value());
    assertEquals("Niedersachsen-Bremen", Federation.TNB.value());
    assertEquals("Sachsen-Anhalt", Federation.TSA.value());
    assertEquals("Thüringen", Federation.TTV.value());
    assertEquals("Mittelrhein", Federation.TVM.value());
    assertEquals("Niederrhein", Federation.TVN.value());
  }
}
