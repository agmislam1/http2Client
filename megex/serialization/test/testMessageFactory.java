/************************************************
 *
 * Author: Agm Islam
 * Assignment: Program 2
 * Class: CSI5321 - Advance Data Communication
 *
 ************************************************/

package megex.serialization.test;

import megex.serialization.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test Cases for checking MessageFactory Class
 *
 * @version 1.4
 * @author Agm Islam
 * Date: 14 Feb 2023
 */

public class testMessageFactory {

    /**
     * Test Case 12: Provided Test Case
     * @throws IOException - for IO Exception
     * @throws BadAttributeException - Bad Attribute
     */
    @Test
    public void testWindowDecodeHappyPath() throws IOException, BadAttributeException {

        assertTrue(true);


        MessageFactory f = new MessageFactory();
        Window_Update w = (Window_Update) f.decode(new byte[] { 8, 0, 0, 0, 0, 10, 0, 0, 0, 10 });


        assertAll(() -> assertEquals(10, w.getStreamID()), () -> assertEquals(10, w.getIncrement()),
                () -> assertEquals(8, w.getCode()));

    }

    /**
     * Test Case 1: Check null Message should throw NullPointerException
     */
    @Test
    public void testNullMessageEncode(){

        MessageFactory messageFactory = new MessageFactory();
        assertThrows(NullPointerException.class, () ->  messageFactory.encode(null));

    }

    /**
     * Test Case 2: Check null Message should throw NullPointerException
     */
    @Test
    public void testNullMessageDecode(){

        MessageFactory messageFactory = new MessageFactory();
        assertThrows(NullPointerException.class, () ->  messageFactory.decode(null));

    }

    /**
     * Test Case 3: Check Window_Update decoding happy path
     * @throws BadAttributeException - Bad Attribute
     * @throws IOException if IOException
     */
    @Test
    public void testWindowMessageDecodeHappyPath() throws BadAttributeException,IOException {

        MessageFactory f = new MessageFactory();
        Window_Update w = (Window_Update) f.decode(new byte[] { 8, 0, 0, 0, 0, 5, 0, 0, 0, 10 });

        assertAll(() -> assertEquals(5, w.getStreamID()), () -> assertEquals(10, w.getIncrement()),
                () -> assertEquals(8, w.getCode()));

    }

    /**
     * Test Case 4: Check Window_Update for long Message
     * @throws BadAttributeException - Bad Attribute
     */
    @Test
    public void testWindowMessageTooLong() throws BadAttributeException {

        MessageFactory f = new MessageFactory();

        try{
            Window_Update w = (Window_Update) f.decode(new byte[] { 8, 0, 0, 0, 0, 5, 0, 0, 0, 10, 11 });
            fail("Test Case Failed");
        }
        catch (Exception e){
            assertEquals(BadAttributeException.class, e.getClass());
        }

    }

    /**
     * Test Case 5: Check Data for Happy Path
     * @throws BadAttributeException - Bad Attribute
     * @throws IOException if IOException
     */
    @Test
    public void tesDecodeDataHappyPath() throws BadAttributeException,IOException {

        MessageFactory f = new MessageFactory();
        Data data = (Data) f.decode(new byte[] { 0, 1, 127, -1, -1, -1, 1, 2, 3, 4, 5});
        byte[] expectedData = {1, 2, 3, 4, 5};

        assertAll(() -> assertEquals(2147483647 , data.getStreamID()), () -> assertArrayEquals(expectedData,data.getData()),
                () -> assertEquals(0, data.getCode()));
        assertTrue(data.isEnd());

    }

    /**
     * Test Case 6: Check Data for error bit set and isend is false
     * @throws BadAttributeException - Bad Attribute
     */
    @Test
    public void testDataIsEndFalseAndErrorFlagOn() throws BadAttributeException {

        MessageFactory f = new MessageFactory();
        try{
            Data data = (Data) f.decode(new byte[] { 0, 8, 0, 0, 0, 5, 0, 0, 0, 10 });
        }
        catch (Exception e){
            assertEquals(BadAttributeException.class, e.getClass());
        }

    }

    /**
     * Test Case 7: Check Setting for Happy Path
     * @throws BadAttributeException - Bad Attribute
     * @throws IOException if IOException
     */
    @Test
    public void testSettingHappyPat() throws BadAttributeException,IOException {

        MessageFactory f = new MessageFactory();
        Settings s = (Settings) f.decode(new byte[] { 4, 0, 0, 0, 0, 0, 0, 0, 0, 10 });

        String expected = "Settings: StreamID=0";
        assertEquals(s.toString(),expected);


    }

    /**
     * Test Case 8: Invalid Stream ID for Settingg
     * @throws BadAttributeException - Bad Attribute
     */
    @Test
    public void testSettingInvalidStreamID() throws BadAttributeException {

        MessageFactory f = new MessageFactory();
        try{
            Settings data = (Settings) f.decode(new byte[] { 4, 8, 0, 0, 0, 5, 0, 0, 0, 10 });
        }
        catch (Exception e){
            assertEquals(BadAttributeException.class, e.getClass());
        }

    }

    // ** Test Cases for Encoding **//

    /**
     * Test Case 9: HappyPath for Settings
     * @throws BadAttributeException Bad Attribute
     * @throws IOException if IO Exception
     */
    @Test
    public void testSettingEncodeHappyPath() throws BadAttributeException,IOException {

        MessageFactory f = new MessageFactory();
        Settings s = new Settings();
        byte[] output  = f.encode((Message) s);
        byte[] expected = new byte[]{4,1,0,0,0,0};
        byte[] expected1 = new byte[]{4,5,0,0,0,0};

        assertArrayEquals(output,expected);

        Window_Update s1 = new Window_Update(6,5000);
        Window_Update s2 = new Window_Update(6,5000);

        assertEquals(s1.hashCode(),s2.hashCode());
        assertTrue(s1.equals(s2));
        assertEquals(s1,s2);

    }

    /**
     * Test Case 10: HappyPath for Data
     * @throws BadAttributeException Bad Attribute
     * @throws IOException if IO Exception
     */
    @Test
    public void testDataEncodeHappyPath() throws BadAttributeException,IOException {


        MessageFactory f = new MessageFactory();
        byte[] bytedata =new byte[] {1, 2, 3, 4, 5};
        Data s = new Data(2147483647 ,true, bytedata);

        byte[] output  = f.encode((Message) s);
        byte[] expected = new byte[]{0, 1, 127, -1, -1, -1, 1, 2, 3, 4, 5 };
        int len = expected.length-6;

        String original = s.toString();
        String expectedString = "Data: StreamID=2147483647 isEnd=true data="+len;

        String a = "abc";
        String b = "abc";

        assertTrue(a==b);

        assertTrue(original.equals(expectedString));
        assertEquals(original,expectedString);

    }

    /**
     * Test Case 11: HappyPath for Windows Frame
     * @throws BadAttributeException - Bad Attribute
     * @throws IOException if IO Exception
     */
    @Test
    public void testWindowHappyPath() throws BadAttributeException,IOException {

        MessageFactory f = new MessageFactory();
        Window_Update s = new Window_Update(875313400,875313400);

        //00110100 52
        //00101100 44
        //00111000 56
        //11111000 248

        byte[] output  = f.encode((Window_Update) s);
        byte[] expected = new byte[]{8,0,52,44,56,-8,52,44,56,-8};


        byte[] b = new byte[4];
        System.arraycopy(output,6,b,0,4);
        int val =  ByteBuffer.wrap(b).getInt();


        System.arraycopy(output,2,b,0,4);
        int val1 =  ByteBuffer.wrap(b).getInt();



        String original = s.toString();
        String expectedString = "Window_Update: StreamID=1 increment=875313400";
        assertArrayEquals(output,expected);

    }


    /**
     * Test Case 12: HappyPath for Header Encode
     * @throws BadAttributeException Bad Attribute
     * @throws IOException if IO Exception
     */
    @Test
    public void testDataEncodeHeader() throws BadAttributeException,IOException {


        MessageFactory f = new MessageFactory();
        byte[] expectedData =new byte[] {0x1, 0x4, 0x0, 0x0, 0x0, 0x1};
        Headers s = new Headers(1 ,false);

        byte[] encodedData = f.encode(s);
        Headers h1 = (Headers) f.decode(encodedData);

        assertArrayEquals(expectedData,encodedData);

    }

    /**
     * Test Case 13: Invalid Flag for Headers
     * @throws BadAttributeException Bad Attribute
     * @throws IOException if IO Exception
     */
    @Test
    public void testInvalidFlagHeader() throws BadAttributeException,IOException {

        MessageFactory f = new MessageFactory();

        byte[] headerdata =new byte[] {0x1, 0x8, 0x0, 0x0, 0x0, 0x1};
        assertThrows(BadAttributeException.class, () -> f.decode(headerdata));

        byte[] headerdata1 =new byte[] {0x1, 0x20, 0x0, 0x0, 0x0, 0x1};
        assertThrows(BadAttributeException.class, () -> f.decode(headerdata1));

        byte[] headerdata2 =new byte[] {0x1, 0x3, 0x0, 0x0, 0x0, 0x1};
        assertThrows(BadAttributeException.class, () -> f.decode(headerdata2));

    }



}
