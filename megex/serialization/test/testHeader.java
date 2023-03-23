/************************************************
 *
 * Author: Agm Islam
 * Assignment: Program 2
 * Class: CSI5321 - Advance Data Communication
 *
 ************************************************/

package megex.serialization.test;

import megex.serialization.BadAttributeException;
import megex.serialization.Headers;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Cases for checking MessageFactory Class
 *
 * @version 1.3
 * @author Agm Islam
 * Date: 8 March 2023
 */
public class testHeader {

    /**
     * Test Case 1: Test add Header
     * @throws BadAttributeException - Bad Attribute
     */
    @Test
    public void testAddHeader() throws BadAttributeException {

        Headers headers = new Headers(1, true);
        assertTrue(headers.isEnd());
        assertTrue(headers.getStreamID()==1);

        headers.addValue("saad1","1");
        headers.addValue("saad2","2");
        headers.addValue("saad3","3");
        headers.addValue("saad1","4");

        Headers headers1 = new Headers(1, true);

        headers1.addValue("saad1","1");
        headers1.addValue("saad2","2");
        headers1.addValue("saad3","3");
        headers1.addValue("saad1","10");
        headers1.addValue("saad1","4");

        assertEquals(headers.hashCode(),headers1.hashCode());
        assertTrue(headers.equals(headers1));
        assertEquals(headers,headers1);

    }

    /**
     * Test Case 2: Test get Names
     * @throws BadAttributeException - Bad Attribute
     */
    @Test
    public void testGetNames() throws BadAttributeException {

        Set<String> headerNames = emptySet();
        Headers headers = new Headers(1, true);
        assertEquals(headers.getNames(),(headerNames));

        headers.addValue("saad1","1");
        headers.addValue("saad2","2");

        Set<String> headerNames1 = new LinkedHashSet<>();
        headerNames1.add("saad1");
        headerNames1.add("saad2");
        assertTrue(headers.getNames().equals(headerNames1));

    }

    /**
     * Test Case 3: Test Invalid Header Names and Value
     * @throws BadAttributeException - Bad Attribute
     */
    @Test
    public void testBadNamesHeaders() throws BadAttributeException {

        Headers headers = new Headers(1, true);

        // Upper case header key should throw exception
        assertThrows(BadAttributeException.class, () ->  headers.addValue("Saad","1"));

        // Delimeter in the header should thorw exception
        assertThrows(BadAttributeException.class, () ->  headers.addValue("sa,ad","1"));
        assertThrows(BadAttributeException.class, () ->  headers.addValue("sa(ad","1"));
        assertThrows(BadAttributeException.class, () ->  headers.addValue("sa)ad","1"));

    }

    /**
     * Test Case 4: Insertion order of headers do not match
     * @throws BadAttributeException - Bad Attribute
     */
    @Test
    public void testAddHeaderInsertionOrder() throws BadAttributeException {

        Headers headers = new Headers(1, true);
        assertTrue(headers.isEnd());
        assertTrue(headers.getStreamID()==1);

        headers.addValue("saad1","1");
        headers.addValue("saad2","2");
        headers.addValue("saad3","3");


        Headers headers1 = new Headers(1, true);

        headers1.addValue("saad3","3");
        headers1.addValue("saad1","1");
        headers1.addValue("saad2","2");


        assertNotEquals(headers.hashCode(),headers1.hashCode());
        assertNotEquals(headers,headers1);
        assertNotEquals(headers.hashCode(),headers1.hashCode());

    }

    /**
     * Test Case 4: Equality Check for Empty Headers
     * @throws BadAttributeException - Bad Attribute
     */
    @Test
    public void testAddHeaderEmpty() throws BadAttributeException {

        Headers headers = new Headers(1, false);
        Headers headers1 = new Headers(1, false);

        assertTrue(!headers.isEnd());
        assertTrue(headers.getStreamID()==1);

        assertEquals(headers.hashCode(),headers1.hashCode());
        assertEquals(headers,headers1);
        assertEquals(headers.hashCode(),headers1.hashCode());

        assertEquals(headers,headers1);

    }

    /**
     * Test Case 5: Empty header Key or value
     * @throws BadAttributeException - Bad Attribute
     */
    @Test
    public void testAddHeaderBlank() throws BadAttributeException {

        Headers headers = new Headers(1, false);
        assertThrows(BadAttributeException.class, () ->  headers.addValue("","1"));
        assertThrows(BadAttributeException.class, () ->  headers.addValue("s",""));



    }
}
