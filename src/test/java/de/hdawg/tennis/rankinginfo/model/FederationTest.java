package de.hdawg.tennis.rankinginfo.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FederationTest {

    @Test
    public void testLongName() {
        assertEquals("", Federation.NONE.getLongName());
        assertEquals("Baden", Federation.BAD.getLongName());
        assertEquals("Berlin-Brandenburg", Federation.BB.getLongName());
        assertEquals("Bayern", Federation.BTV.getLongName());
        assertEquals("Hamburg", Federation.HAM.getLongName());
        assertEquals("Hessen", Federation.HTV.getLongName());
        assertEquals("Rheinland-Pfalz", Federation.RPF.getLongName());
        assertEquals("Schleswig-Holstein", Federation.SLH.getLongName());
        assertEquals("Saarland", Federation.STB.getLongName());
        assertEquals("Sachsen", Federation.STV.getLongName());
        assertEquals("Mecklenburg-Vorpommern", Federation.TMV.getLongName());
        assertEquals("Niedersachsen-Bremen", Federation.TNB.getLongName());
        assertEquals("Sachsen-Anhalt", Federation.TSA.getLongName());
        assertEquals("Thüringen", Federation.TTV.getLongName());
        assertEquals("Mittelrhein", Federation.TVM.getLongName());
        assertEquals("Niederrhein", Federation.TVN.getLongName());
        assertEquals("Würtemberg", Federation.WTB.getLongName());
        assertEquals("Westfalen", Federation.WTV.getLongName());
    }
}
