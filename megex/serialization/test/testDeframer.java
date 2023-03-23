/************************************************
 *
 * Author: Agm Islam
 * Assignment: Program 2
 * Class: CSI5321 - Advance Data Communication
 *
 ************************************************/

package megex.serialization.test;

import megex.serialization.Deframer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Cases for checking DeFramer Class
 *
 * @version 1.1
 * @author Agm Islam
 * Date: 8 Feb 2023
 */
public class testDeframer {

    /**
     * Test Case 1: Test the null constructor
     * Should throw NullPointerException
     */

    @Test
    public void testNullCOnstructor(){

        // Declare a framer object with null constructor
        try{
            Deframer deframer = new Deframer(null);
            assertTrue(false);
        }
        catch(NullPointerException e){
            assertTrue(true);
        }

    }

    /**
     * Test Case 2: Test Invalid Length: Less than 3 (payload length size)
     * Should throw EOFException
     * @throws IOException - if IOException
     */

    @Test
    public void testInvalidLength() throws IOException {

        byte[] payload1 = new byte[2]; // Invalid Length Frame
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(payload1);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        Deframer deframer = new Deframer(inputStream);
        assertThrows(EOFException.class, () ->  deframer.getFrame());

    }

    /**
     * Test Case 3: Test Invalid Length: Less than 3 (payload length size)
     * Should throw EOFException
     * @throws IOException - if IOException
     */
    @Test
    public void testInvalidLengthHeader() throws IOException {

        byte[] payload1 = new byte[8]; // Invalid Length Frame, Header not found
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(payload1);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        Deframer deframer = new Deframer(inputStream);
        assertThrows(EOFException.class, () ->  deframer.getFrame());

    }

    /**
     * Test Case 4: Test too long payload: greater than the maximum size
     * Should throw IllegalArgumentException
     * @throws IOException - if IOException
     */
    @Test
    public void testTooLongPayload() throws IOException {

        byte[] payload1 = new byte[16387]; // Frame is greater than maximum payload+header
        byte[] payloadLength = new byte[]{1,64,0};
        System.arraycopy(payloadLength,0,payload1,0,3);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(payload1);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        Deframer deframer = new Deframer(inputStream);
        assertThrows(IllegalArgumentException.class, () ->  deframer.getFrame());

    }

    /**
     * Test Case 5: Payload Length is less than the given in the frame
     * Should throw EOFException
     * @throws IOException - if IOException
     */
    @Test
    public void testBadLengthPayload() throws IOException {

        byte[] payload1 = new byte[]{0,0,1,0,0,0,0,0,0}; //
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(payload1);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        Deframer deframer = new Deframer(inputStream);
        assertThrows(EOFException.class, () ->  deframer.getFrame());

    }
    
    /**
     * Test Case 6: Happy Path(Valid Frame)
     * @throws IOException - if IOException
     */
    @Test
    public void testValidPayload() throws IOException {

        byte[] payload1 = new byte[]{0,0,1,0,0,0,0,0,0,1};
        byte[] expected = new byte[]{0,0,0,0,0,0,1};

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(payload1);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        Deframer deframer = new Deframer(inputStream);
        byte[] output = deframer.getFrame();

        assertArrayEquals(expected,output);

    }


}
