/************************************************
 *
 * Author: Agm Islam
 * Assignment: Program 2
 * Class: CSI5321 - Advance Data Communication
 *
 ************************************************/

package megex.serialization.test;

import megex.serialization.Framer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Cases for checking Framer Class
 *
 * @version 1.1
 * @author Agm Islam
 * Date: 8 Feb 2023
 */
public class testFramer {

    /**
     * Test Case 1: Test the null constructor
     * Should throw NullPointerException
     */
    @Test
    public void testNullConstructor(){

        // Declare a framer object with null constructor
        try{
            Framer framer = new Framer(null);
            assertTrue(false);
        }
        catch(NullPointerException e){
            assertTrue(true);
        }

    }

    /**
     * Test Case 2: Too short data should throw IllegalArgumentException
     */
    @Test
    public void testTooShortData(){

        byte[] payload1 = new byte[5]; // Too short data with 5 bytes
        ByteArrayOutputStream ouputStream = new ByteArrayOutputStream();
        Framer framer = new Framer(ouputStream);
        assertThrows(IllegalArgumentException.class, () ->  framer.putFrame(payload1));

        byte[] payload2 = new byte[2]; // Too short data with 2 bytes
        assertThrows(IllegalArgumentException.class, () ->  framer.putFrame(payload2));

    }

    /**
     * Test Case 3: Empty message should throw NullPointerException
     */
    @Test
    public void testEmptyMessage(){

        byte[] payload1 = null; // empty message
        ByteArrayOutputStream ouputStream = new ByteArrayOutputStream();
        Framer framer = new Framer(ouputStream);
        assertThrows(NullPointerException.class, () ->  framer.putFrame(payload1));

    }

    /**
     * Test Case 4: Too long data
     * Exceeds maximum payload size of 16384 + header
     * @throws IOException - if IOException
     */
    @Test
    public void testTooLongData() throws IOException{

        byte[] payload1 = new byte[16391]; // payload exceeds the maximum size including header
        ByteArrayOutputStream ouputStream = new ByteArrayOutputStream();
        Framer framer = new Framer(ouputStream);
        assertThrows(IllegalArgumentException.class, () ->  framer.putFrame(payload1));

    }

    /**
     * Test Case 5: Valid Frame
     * @throws IOException - if IOException
     */
    @Test
    public void testValidData() throws IOException {

        byte[] payload1 = new byte[20];      // payload including header
        Arrays.fill(payload1,(byte) 0xFF);   // Fill the data with 0xFF

        ; // Expected Frame
        byte[] frameExpected = new byte[]{0, 0, 14, -1, -1, -1, -1, -1,
                                            -1,-1, -1, -1, -1, -1, -1, -1,
                                            -1, -1, -1, -1, -1, -1, -1};


        // create the frame
        ByteArrayOutputStream ouputStream = new ByteArrayOutputStream();
        Framer framer = new Framer(ouputStream);
        framer.putFrame(payload1);

        // Now read the frame
        byte[] outputFrame = new byte[23];
        ByteArrayInputStream inputStream = new ByteArrayInputStream(ouputStream.toByteArray());
        inputStream.readNBytes(outputFrame,0,23);

        // compare the frame
        assertArrayEquals(frameExpected,outputFrame);
    }
}
