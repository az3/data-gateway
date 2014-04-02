package com.cagricelebi.datagw.lib;

import com.cagricelebi.datagw.lib.Helper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class HelperTest {

    // <editor-fold defaultstate="collapsed" desc="Standart setUp & tearDown.">
    public HelperTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    // </editor-fold>

    @Test
    public void testIsEmpty() {
        String nullValue = null;
        boolean expResult1 = true;
        System.out.println("Helper.isEmpty(" + nullValue + ") =? " + expResult1);
        boolean result1 = Helper.isEmpty(nullValue);
        assertEquals(expResult1, result1);

        String emptyString = "";
        boolean expResult2 = true;
        System.out.println("Helper.isEmpty(" + emptyString + ") =? " + expResult2);
        boolean result2 = Helper.isEmpty(emptyString);
        assertEquals(expResult2, result2);

        String stringWithTextNull = "null";
        boolean expResult3 = true;
        System.out.println("Helper.isEmpty(" + stringWithTextNull + ") =? " + expResult3);
        boolean result3 = Helper.isEmpty(stringWithTextNull);
        assertEquals(expResult3, result3);

        String stringWithSomeText = "kedi";
        boolean expResult4 = false;
        System.out.println("Helper.isEmpty(" + stringWithSomeText + ") =? " + expResult4);
        boolean result4 = Helper.isEmpty(stringWithSomeText);
        assertEquals(expResult4, result4);
    }

}
