package de.hdawg.tennis.rankinginfo.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FederationTest {

    @Test
    public void testLongName() {
        assertEquals("", Federation.NONE.getLongname());
        assertEquals("Baden", Federation.BAD.getLongname());
        assertEquals("Berlin-Brandenburg", Federation.BB.getLongname());
        assertEquals("Bayern", Federation.BTV.getLongname());
        assertEquals("Hamburg", Federation.HAM.getLongname());
        assertEquals("Hessen", Federation.HTV.getLongname());
        assertEquals("Rheinland-Pfalz", Federation.RPF.getLongname());
        assertEquals("Schleswig-Holstein", Federation.SLH.getLongname());
        assertEquals("Saarland", Federation.STB.getLongname());
        assertEquals("Sachsen", Federation.STV.getLongname());
        assertEquals("Mecklenburg-Vorpommern", Federation.TMV.getLongname());
        assertEquals("Niedersachsen-Bremen", Federation.TNB.getLongname());
        assertEquals("Sachsen-Anhalt", Federation.TSA.getLongname());
        assertEquals("Thüringen", Federation.TTV.getLongname());
        assertEquals("Mittelrhein", Federation.TVM.getLongname());
        assertEquals("Niederrhein", Federation.TVN.getLongname());
        assertEquals("Würtemberg", Federation.WTB.getLongname());
        assertEquals("Westfalen", Federation.WTV.getLongname());
    }
}
