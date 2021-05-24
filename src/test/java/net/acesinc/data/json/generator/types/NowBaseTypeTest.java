/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.types;

import org.junit.*;

import static org.junit.Assert.*;

/**
 *
 * @author denisgillespie
 */
public class NowBaseTypeTest {

    public NowBaseTypeTest() {
    }

    @Before
    public void setUp() {

    }

    /**
     * Test of getTimeOffset method, of class NowBaseType.
     */
    @Test
    public void testTimeOffsetReturnsCorrectTimeOffsetIfLargeNumber() {
        assertTrue(NowBaseType.getTimeOffset("4_y") == 126144000000L && NowBaseType.getTimeOffset("4_y") > Integer.MAX_VALUE);
        assertTrue(NowBaseType.getTimeOffset("-4_y") == -126144000000L && NowBaseType.getTimeOffset("-4_y") < Integer.MIN_VALUE);

        assertTrue(NowBaseType.getTimeOffset("600_d") == 51840000000L && NowBaseType.getTimeOffset("600_y") > Integer.MAX_VALUE);
        assertTrue(NowBaseType.getTimeOffset("-600_d") == -51840000000L && NowBaseType.getTimeOffset("-600_y") < Integer.MIN_VALUE);

        assertTrue(NowBaseType.getTimeOffset("40000_h") == 144000000000L && NowBaseType.getTimeOffset("40000_d") > Integer.MAX_VALUE);
        assertTrue(NowBaseType.getTimeOffset("-40000_h") == -144000000000L && NowBaseType.getTimeOffset("-40000_d") < Integer.MIN_VALUE);

        assertTrue(NowBaseType.getTimeOffset(Integer.MAX_VALUE + "_m") == 128849018820000L && NowBaseType.getTimeOffset(Integer.MAX_VALUE + "_m")  > Integer.MAX_VALUE);
        assertTrue(NowBaseType.getTimeOffset("-" + Integer.MAX_VALUE + "_m") == -128849018820000L && NowBaseType.getTimeOffset("-" + Integer.MAX_VALUE + "_m") < Integer.MIN_VALUE);
    }

}
