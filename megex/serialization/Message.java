/************************************************
 *
 * Author: Agm Islam
 * Assignment: Program 2
 * Class: CSI5321 - Advance Data Communication
 *
 ************************************************/

package megex.serialization;

/**
 * Represents a message
 *
 * @version 1.4
 * @author Agm Islam
 * Date: 21 Feb 2023
 */

public abstract class Message{

    private int streamID; // Stream ID

    /**
     * Creates the Message
     * @param streamID streamID
     * @throws BadAttributeException if streamID is invalid
     */
    public Message(int streamID) throws BadAttributeException {
        setStreamID(streamID);
    }



    /**
     * Returns type code for message
     *
     * @return type code
     */
    public abstract byte getCode();

    /**
     * Returns the stream ID
     *
     * @return message stream ID
     */
    public int getStreamID(){

        return streamID;
    }


    /**
     * Sets the stream id in the frame. Stream ID validation depends on specific message type
     * @param streamID
     *          new stream id value
     * @throws BadAttributeException  if input stream id is invalid
     */
    public final void setStreamID(int streamID) throws BadAttributeException {
        this.streamID = validateStreamID(streamID);
    }

    /**
     * Validate the streamID
     * @param streamID streamID
     * @return streamID after validation
     * @throws BadAttributeException if streamID is less than the minimum streamID
     */
    public abstract int validateStreamID(int streamID) throws BadAttributeException;






}
