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
 * Window_Update
 *
 * @version 1.4
 * @author Agm Islam
 * Date: 21 Feb 2023

 */

public class Window_Update extends Message{


    private int increment; // window_update frame increment value
    private static final int MININCREMENT = 1; // Minimum value for increment
    private static final int MINSTREAMID = 0;  // Minimum Stream ID for Window Update
    private static final byte CODE = 0x8;      // Type Code for Data
    /**
     * Creates Window_Update message from given values
     * @param increment
     *        increment value
     * @param streamID
     *        stream ID
     * @throws BadAttributeException if attribute invalid (see protocol spec)
     *
     */
    public Window_Update(int streamID,int increment) throws BadAttributeException{

        super(streamID);
        setIncrement(increment);

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
     * Get increment value
     *
     * @return increment value
     */
    public int getIncrement(){

        return increment;
    }

    /**
     * Set increment value
     * @param increment
     *         increment value
     * @throws BadAttributeException - if invalid
     */
    public void setIncrement(int increment) throws BadAttributeException {

        if (increment<MININCREMENT) {
            throw new BadAttributeException("Invalid Message: increment value not within the range "+increment, "increment",null);
        }
        this.increment = increment;
    }

    /**
     * Validate the streamID
     * @param streamID streamID
     * @return streamID after validation
     * @throws BadAttributeException if streamID is less than the minimum streamID
     */
    public int validateStreamID(int streamID) throws BadAttributeException{
        if(streamID<MINSTREAMID){
            throw new BadAttributeException("Invalid message: invalid stream ID: "+ streamID,"streamID");
        }
        return streamID;
    }


    /**
     * <pre> Returns string of the form </pre>
     * <pre>
     * Window_Update: StreamID=&lt;streamid&gt; increment=&lt;inc&gt;
     * For example
     * Window_Update: StreamID=5 increment=1024
     * </pre>
     *
     * {@inheritDoc}
     *
     */
    public String toString(){
        return "Window_Update: StreamID=" + getStreamID()+ " increment=" +increment;
    }


    /**
     * Calculate the hashcode
     * @return hash value of the object
     */

    @Override
    public int hashCode() {

        return Objects.hash(getStreamID(), increment,getCode());
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
        Window_Update that = (Window_Update) o;


        return getStreamID() == that.getStreamID() && increment == that.increment && that.getCode()== getCode();


    }
}
