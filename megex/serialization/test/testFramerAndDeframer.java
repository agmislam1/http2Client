/************************************************
 *
 * Author: Agm Islam
 * Assignment: Program 2
 * Class: CSI5321 - Advance Data Communication
 *
 ************************************************/

package megex.serialization.test;

import megex.serialization.Deframer;
import megex.serialization.Framer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Test Cases for checking Framer and Deframer Class
 *
 * @version 1.1
 * @author Agm Islam
 * Date: 8 Feb 2023
 */

public class testFramerAndDeframer {

    /**
     * Test Case 1: payload Framed and deframed payload should be equal
     * @throws IOException - if IOException
     */
    @Test
    void testFrameDeframe() throws IOException {

        byte[] payload = new byte[15];  // payload that will be framed
        Arrays.fill(payload, (byte) 1); // filled with 1


        // Frame the payload and deframe
        ByteArrayOutputStream ouptutStream = new ByteArrayOutputStream();
        Framer framer = new Framer(ouptutStream);      // Declare a frame object
        framer.putFrame(payload);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(ouptutStream.toByteArray());
        Deframer deframer = new Deframer(inputStream);
        byte[] output = deframer.getFrame();

        // Compare the payload and the deframed payload
        assertArrayEquals(payload,output);

    }

}
