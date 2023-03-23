/************************************************
 *
 * Author: Agm Islam
 * Assignment: Program 2
 * Class: CSI5321 - Advance Data Communication
 *
 ************************************************/

package megex.serialization;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Serialize framed messages to given output stream
 *
 * @version 1.1
 * @author Agm Islam
 * Date: 8 Feb 2023
 */

public class Framer{

    private OutputStream out;
    private static final int MAXPAYLOAD = 16384;     // maximum payload size
    private static final int HEADERSIZE = 6;         // header size
    private static final int PAYLOADHEADERLENGTH =3; // payload Header Length

    /**
     * Construct framer with given output stream
     *
     * @param out
     *        byte sink
     *
     * @throws NullPointerException - if out is null
     */
    public Framer(OutputStream out){

        this.out = Objects.requireNonNull(out,"Null Object");

    }

    /**
     * Create a frame by adding the prefix length to the given message and sending the entire frame (i.e., prefix length, header, and payload)
     *
     * @param message
     *        next frame NOT including the prefix length (but DOES include the header)
     *
     * @throws IOException - if out is null
     * @throws IllegalArgumentException -  if invalid message (e.g., frame payload too long)
     * @throws NullPointerException - if message is null
     */
    public void putFrame(byte[] message) throws IOException {

        // Check for empty message
        if(message==null){
            throw new NullPointerException("Unable to parse: Empty Message");
        }

        int msgLength = message.length; // Message Length

        // Check for minimum and maximum length of the message
        if(msgLength<HEADERSIZE || msgLength>(MAXPAYLOAD+HEADERSIZE)){
            throw new IllegalArgumentException("Unable to parse: Bad Length "+ msgLength);
        }

        msgLength -= HEADERSIZE; // Only the payload Length

        byte[] messageLengthByte = ByteBuffer.allocate(6).putInt(msgLength).array();      // convert to byte array
        byte[] payloadLengthHeader = new byte[3];                                         // 3 byte for the payload length
        System.arraycopy(messageLengthByte,1,payloadLengthHeader,0,3); // copy from the byte array

        int frameLength = PAYLOADHEADERLENGTH+HEADERSIZE+msgLength; // payload(3)+header(6)+msgLength(variable)
        byte[] frame = new byte[frameLength];


        System.arraycopy(payloadLengthHeader,0,frame,0,3); // copy the message Length
        System.arraycopy(message,0,frame,3,msgLength+6);   // copy the message including header


        out.write(frame);   // write the frame
        out.flush();        // flush the buffer

    }
}
