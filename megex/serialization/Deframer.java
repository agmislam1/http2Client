/************************************************
 *
 * Author: Agm Islam
 * Assignment: Program 2
 * Class: CSI5321 - Advance Data Communication
 *
 ************************************************/

package megex.serialization;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Deserialize frames from given input stream
 *
 * @version 1.1
 * @author Agm Islam
 * Date: 8 Feb 2023
 */
public class Deframer{

    private InputStream in;
    private static final int MAXPAYLOAD = 16384;     // maximum payload size
    private static final int HEADERSIZE = 6;         // header size
    private static final int PAYLOADHEADERLENGTH =3; // payload Header Length

    /**
     * Construct framer with given input stream
     *
     * @param in
     *        byte source
     *
     * @throws NullPointerException - if in is null
     */
    public Deframer(InputStream in){

        this.in = Objects.requireNonNull(in, "Null Object");
    }

    /**
     * Get the next frame
     *
     * @return next frame NOT including the length (but DOES include the header
     *
     * @throws java.io.EOFException - if premature EOF
     * @throws IOException - if I/O error occurs
     * @throws IllegalArgumentException - if bad value in input stream (e.g., bad length)
     */
    public byte[] getFrame() throws IOException{

        int payloadLength; // The length of the payload.

        byte[] paySize = new byte[PAYLOADHEADERLENGTH+1];                         // Byte Array for the payload length
        int payloadLengthData = in.readNBytes(paySize,1,PAYLOADHEADERLENGTH); // Read the Payload Length

        // First frame must contain 3 bytes for the payload length
        if(payloadLengthData!=PAYLOADHEADERLENGTH){
            throw new EOFException("Unable to parse: Bad Length - " + payloadLengthData);
        }

        payloadLength  = ByteBuffer.wrap(paySize).getInt(); // get the payload length.

        // Check for the Maximum Payload
        if(payloadLength>(MAXPAYLOAD)){
            throw  new IllegalArgumentException("Unable to parse: Too Large - "+payloadLength);
        }

        int totalpayload = payloadLength+HEADERSIZE;  // Frame Size (Header + payload)
        byte[] currentFrame = new byte[totalpayload]; // Declare a byte array for the frame
        int currentFramSize =  in.readNBytes(currentFrame,0,totalpayload); // read the frame

        // Check for valid Size: premature EOF
        if(currentFramSize<HEADERSIZE || currentFramSize!= totalpayload){
            throw  new EOFException("Premature EOF or Bad Length: " + currentFramSize);
        }

        return currentFrame;
    }
}
