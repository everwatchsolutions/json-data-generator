/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.types;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author andrewserff
 */
public class DateTypeTest {

    private String minDate = "2015/05/01T00:00:00";
    private String maxDate = "2015/05/05T23:59:59";

    private Date min;
    private Date max;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss");

    public DateTypeTest() {
    }

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        try {
            min = sdf.parse(minDate);
            max = sdf.parse(maxDate);
        } catch (ParseException pe) {

        }
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getNextRandomValue method, of class DateType.
     */
    @Test
    public void testGetNextRandomValueWithMin() {
        System.out.println("getNextRandomValue");
        String[] launchArguments = {minDate};
        DateType instance = new DateType();
        instance.setLaunchArguments(launchArguments);

        //test it 1000 times
        for (int i = 0; i < 1000; i++) {
            Date now = new Date();
            Date result = instance.getNextRandomValue();
            assertTrue("Testing that " + result + " is after " + min, result.after(min) || result.equals(min));
            assertTrue("Testing that " + result + " is before " + now, result.before(now) || result.equals(now));
        }
    }
    
    @Test
    public void testGetNextRandomValueWithMinAndMax() {
        System.out.println("getNextRandomValue");
        String[] launchArguments = {minDate, maxDate};
        DateType instance = new DateType();
        instance.setLaunchArguments(launchArguments);

        //test it 1000 times
        for (int i = 0; i < 1000; i++) {
            Date result = instance.getNextRandomValue();
            assertTrue(result.after(min) || result.equals(min));
            assertTrue(result.before(max) || result.equals(max));
        }
    }

    /**
     * Test of getName method, of class DateType.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        DateType instance = new DateType();
        String expResult = "date";
        String result = instance.getName();
        assertEquals(expResult, result);
    }

}
