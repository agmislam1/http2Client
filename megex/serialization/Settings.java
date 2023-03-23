/************************************************
 *
 * Author: Agm Islam
 * Assignment: Program 2
 * Class: CSI5321 - Advance Data Communication
 *
 ************************************************/

package megex.serialization;

import java.util.Objects;

/**
 * Settings message
 *
 * @version 1.4
 * @author Agm Islam
 * Date: 21 Feb 2023
 */

public class Settings extends Message{


    private static final int STREAMID = 0;     // Only valid Stream ID for Settings
    private static final byte CODE = 0x4;      // Type Code for SETTINGS


    /**
     * Creates Settings message
     *
     * @throws BadAttributeException if attribute invalid (not thrown in this case)
     */
    public Settings() throws BadAttributeException{

        super(STREAMID);
    }


    /**
     * Returns type code for message
     *
     * @return type code
     */
    public byte getCode(){

        return CODE;
    }

    /**
     * Validate the streamID
     * @param streamID streamID
     * @return streamID after validation
     * @throws BadAttributeException if streamID is less than the minimum streamID
     */
    public int validateStreamID(int streamID) throws BadAttributeException{
        if(streamID != STREAMID){
            throw new BadAttributeException("Invalid stream ID: "+ streamID,"streamID");
        }
        return streamID;
    }


    /**
     * <pre>Returns string of the form</pre>
     * <pre>
     * Settings: StreamID=0
     *
     * For example
     *
     * Settings: StreamID=0
     * </pre>
     * {@inheritDoc}
     *
     */
    public String toString(){
        return "Settings: StreamID="+ getStreamID();
    }

    /**
     * Calculate the hashcode
     * @return hash value of the object
     */

    @Override
    public int hashCode() {

        return Objects.hash(getStreamID(),getCode());
    }

    /**
     * check equality of the objects
     * @param o
     *          Object
     * @return true if both object are equal
     */
    @Override
    public boolean equals(Object o) {


        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Settings that = (Settings) o;


        return getStreamID() == that.getStreamID() && that.getCode()== getCode();


    }



}
