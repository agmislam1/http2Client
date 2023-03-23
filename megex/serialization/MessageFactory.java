/************************************************
 *
 * Author: Agm Islam
 * Assignment: Program 2
 * Class: CSI5321 - Advance Data Communication
 *
 ************************************************/

package megex.serialization;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;


/**
 * Factory for deserialization and serializing messages
 *
 * @version 1.7
 * @author Agm Islam
 * Date: 4 March 2023
 */

public class MessageFactory{

    private static final int HEADERLENGTH = 6;       // header length in bytes
    private static final int MASKVALUE = 2147483647; // Masking value to ignore the 1st bit in the stream ID
    private static final int WINDOWDATASIZE = 4;     // Window increment data size
    private static final int STREAMIDSIZE = 4;       // Stream ID size
    private static final int ERRORBIT = 8;           // Error bit masking value for Data
    private static final int ENDBIT = 1;             // End Stream masking value
    private static final int STREAIDSTARTBYTE = 2;   // Starting position of the Data
    private static final int DATASTARTBYTE = 6;      // Starting position of the Data
    private static final byte SETTINGSFLAG = 0x1;    // Flag for Settings Frame
    private static final byte WINDOWFLAG = 0x0;      // Flag for Window Frame
    private static final int CODEBYTE = 0;           // Byte position for Code
    private static final int FLAGBYTE = 1;           // Byte position for Flag

    private static final int HEADERFLAG = 4;         // Required FLAG for Header
    private static final int HEADERERRORFLAG1 = 8;   // ERROR FLAG 1 for Header
    private static final int HEADERERRORFLAG2 = 32;  // ERROR FLAG 2 for Header


    // Type Codes for different data framw
    private static final byte DATACODE = 0x0;
    private static final byte HEADERSCODE = 0x1;
    private static final byte SETTINGSCODE = 0x4;
    private static final byte WINDOWCODE = 0x8;

    // HPACK Encoding data
    private static final int MAXHEADERSZ = 1024;
    private static final int MAXHEADERTBLSZ = 1024;
    private static final Charset CHARENC = StandardCharsets.US_ASCII;

    private static  Encoder encoder ;
    private static  Decoder decoder ;


    /**
     * Message Factory
     */
    public MessageFactory(){
        encoder = new Encoder(MAXHEADERTBLSZ);
        decoder = new Decoder(MAXHEADERSZ, MAXHEADERTBLSZ);
    }

    /**
     * returns the flag from the byte array
     * @param data data byte array
     * @return stream ID
     */
    private int getStreamID(byte[] data){

        int sid;
        byte[] pSid = new byte[4];
        System.arraycopy(data,STREAIDSTARTBYTE,pSid,0,STREAMIDSIZE);
        sid = ByteBuffer.wrap(pSid).getInt();
        sid = sid & MASKVALUE; // Ignore the "R" bit
        return sid;

    }

    /**
     * returns data from the byte array
     * @param data data byte array
     * @return data
     */
    private byte[] getData(byte[] data){

        int payloadLength;
        payloadLength = data.length - HEADERLENGTH;
        byte[] payLoad = new byte[payloadLength];
        System.arraycopy(data,DATASTARTBYTE, payLoad,0,payloadLength);
        return payLoad;

    }

    /**
     * Validate the Data flag from the error bit
     * @param flag Flag
     * @return true if it is the end of the stream
     * @throws BadAttributeException if error bit is set
     */
    private boolean validateFlagData(byte flag) throws BadAttributeException{
        if((flag & ERRORBIT)==ERRORBIT){
            throw  new BadAttributeException("Invalid Message: Error bit Set for Data","Flag");
        }

        return (flag & ENDBIT)==ENDBIT ? true: false;
    }

    /**
     * Validate the Header flag from the error bit
     * @param flag Flag
     * @return true if it is the end of the stream
     * @throws BadAttributeException if invalid flag
     */
    private boolean validateFlagHeaders(byte flag) throws BadAttributeException{
        if((flag & HEADERFLAG)!=HEADERFLAG || (flag & HEADERERRORFLAG1)==HEADERERRORFLAG1 || (flag & HEADERERRORFLAG2)==HEADERERRORFLAG2){
            throw  new BadAttributeException("Invalid Message: Invalid Flag","Flag");
        }

        return (flag & ENDBIT)==ENDBIT ? true: false;
    }


    /**
     * get the increment value from the byte array
     * @param data data byte array
     * @return increment
     */
    private int getIncrement(byte[] data){

        byte[] incrementByte = new byte[WINDOWDATASIZE];
        System.arraycopy(data,DATASTARTBYTE, incrementByte,0,WINDOWDATASIZE);
        int increment = ByteBuffer.wrap(incrementByte).getInt();
        increment = increment & MASKVALUE;
        return increment;

    }

    /**
     * Create the byte array from the Data Message
     * @param msg Message
     * @return byte array
     * @throws IOException if IOException
     */
    private byte[] getDataFrame(Message msg) throws IOException{

        Data dataFrame = (Data) msg;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(DATACODE);
        out.write((byte) ((dataFrame.isEnd() ==true) ? 0x1 : 0x0));
        byte[] streamByte = ByteBuffer.allocate(STREAMIDSIZE).putInt(dataFrame.getStreamID()).array();
        out.write(streamByte);
        out.write(dataFrame.getData());


        return out.toByteArray();

    }

    /**
     * Create the byte array from the Settings Message
     * @param msg Message
     * @return byte array
     * @throws IOException if IOException
     */
    private byte[] getSettingsFrame(Message msg) throws IOException{

        Settings settingFrame = (Settings) msg;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(SETTINGSCODE);
        out.write(SETTINGSFLAG);
        byte[] streamByte = ByteBuffer.allocate(STREAMIDSIZE).putInt(settingFrame.getStreamID()).array();
        out.write(streamByte);


        return out.toByteArray();


    }


    /**
     * Create the byte array from the Window_Update Message
     * @param msg Message
     * @return byte array
     * @throws IOException if IOException
     */
    private byte[] getWindowFrame(Message msg) throws IOException{

        Window_Update windowUpdate = (Window_Update) msg;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(WINDOWCODE);
        out.write(WINDOWFLAG);
        byte[] streamByte = ByteBuffer.allocate(STREAMIDSIZE).putInt(windowUpdate.getStreamID()).array();
        out.write(streamByte);
        byte[] increment = ByteBuffer.allocate(STREAMIDSIZE).putInt(windowUpdate.getIncrement()).array();

        out.write(increment);
        return out.toByteArray();

    }

    /**
     * Converts string to bytes
     * @param v a String
     * @return byte array
     */
    private static byte[] s2b(String v) {
        return v.getBytes(CHARENC);
    }

    /**
     * converts byte array to string
     * @param b byte array
     * @return string
     */
    private static String b2s(byte[] b) {
        return new String(b, CHARENC);
    }

    /**
     * decode the headers from the byte array
     * @param h Header Object
     * @param bytes byte array
     * @throws IOException if IOException
     */
    private void addHeaders(Headers h, byte[] bytes) throws IOException {

        int dataLen = bytes.length;

        // check for the header data existence
        if(dataLen<=HEADERLENGTH){
            return;
        }

        int headerDataLen = dataLen-HEADERLENGTH;
        byte[] headerData = new byte[headerDataLen];
        System.arraycopy(bytes,DATASTARTBYTE,headerData, 0, headerDataLen);


        ByteArrayInputStream in = new ByteArrayInputStream(headerData);
        decoder.decode(in, (name, value, sensitive) -> {
            try {
                h.addValue(b2s(name),b2s(value));
            } catch (BadAttributeException e) {

            }
        });
        decoder.endHeaderBlock();

    }




    /**
     * Create the byte array from the Header Message
     * @param msg Message
     * @return byte array
     */
    private byte[] getHeaderFrame(Message msg) throws IOException {

        Headers headers = (Headers) msg;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(HEADERSCODE);

        int headerflag = HEADERFLAG ;
        if(headers.isEnd()){
            headerflag = headerflag | ENDBIT; // if end stream is true set the end stream flag
        }

        out.write((byte) headerflag);


        byte[] streamByte = ByteBuffer.allocate(STREAMIDSIZE).putInt(headers.getStreamID()).array();
        out.write(streamByte);

        // Encode the Header Block
        Set<String> headerKeys;
        headerKeys = headers.getNames();

        for(String key: headerKeys){
            String val = headers.getValue(key);
            encoder.encodeHeader(out, s2b(key), s2b(val), false);

        }
        return out.toByteArray();


    }

    /**
     * Deserializes message from given bytes
     *
     * @param msgBytes
     *         message bytes
     * @return specific Message resulting from deserialization
     *
     * @throws BadAttributeException if validation failure
     * @throws NullPointerException if msgBytes is null
     * @throws IOException if IOException
     */

    public Message decode(byte[] msgBytes) throws BadAttributeException, IOException {

        if(msgBytes==null){
            throw new NullPointerException("Null Message");
        }

        if(msgBytes.length<HEADERLENGTH){
            throw new BadAttributeException("Invalid Data","msgBytes",null);
        }


        byte ptypeCode = msgBytes[CODEBYTE]; // get the Type Code
        byte flag = msgBytes[FLAGBYTE];      // get the flag
        int sid = getStreamID(msgBytes);     // get the stream ID


        // check for valid Type
        switch (ptypeCode) {

            case DATACODE:
                boolean isEnd = validateFlagData(flag);
                byte[] data = getData(msgBytes);
                return new Data(sid, isEnd,data);

            case SETTINGSCODE:
                // StreamID must be 0 for Settings Frame
                if(sid != 0){
                    throw new BadAttributeException("Bad Stream ID","streamID",null);
                }
                return new Settings();

            case WINDOWCODE:
                if(msgBytes.length != (HEADERLENGTH+WINDOWDATASIZE)){
                    throw new BadAttributeException("Too Long", "msgBytes",null);
                }
                int increment = getIncrement(msgBytes);
                return new Window_Update(sid, increment);

            case HEADERSCODE:

                // decode the headers
                boolean isEndStream = validateFlagHeaders(flag);
                Headers h = new Headers(sid,isEndStream);
                addHeaders(h,msgBytes);
                return h;

            default:
                throw new BadAttributeException("Received unknown type: "+ ptypeCode,"msgBytes",null );
        }

    }

    /**
     * Serializes message
     *
     * @param msg
     *        message to serialize
     * @return serialized message
     *
     * @throws NullPointerException if msg is null
     * @throws IOException if IOException
     */
    public byte[] encode(Message msg) throws IOException{

        // Check for NUll Message
        if(msg==null){
            throw new NullPointerException("Null Message");
        }

        byte code = msg.getCode(); // get the Type Code
        byte[] encodedData;        // byte array for the encoded data

        switch (code) {
            case DATACODE:
                encodedData = getDataFrame(msg);
                return encodedData;

            case SETTINGSCODE:
                encodedData = getSettingsFrame(msg);
                return encodedData;

            case WINDOWCODE:
                encodedData = getWindowFrame(msg);
                return encodedData;

            case HEADERSCODE:
                encodedData = getHeaderFrame(msg);
                return encodedData;

            default:
                throw new NullPointerException("Invalid Message Type: "+ code);
        }

    }
}
