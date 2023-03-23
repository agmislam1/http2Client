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
 * Data message
 *
 * @version 1.4
 * @author Agm Islam
 * Date: 21 Feb 2023
 */
public class Data extends Message{

    private boolean isEnd; // indicate end stream
    private byte[] data;   // Data
    private static final int MAXDATASIZE = 16384; // Maximum payload Size
    private static final int MINSTREAMID = 1;     // Minimum Stream ID for Data
    private static final byte CODE = 0x0;         // Type Code for Data

    /**
     * Creates Data message from given values
     *
     * @param streamID
     *        stream ID
     * @param isEnd
     *        true if last data message
     * @param data
     *        bytes of application data
     * @throws BadAttributeException if attribute invalid
     */
    public Data(int streamID, boolean isEnd, byte[] data) throws BadAttributeException {

        super(streamID);
        setEnd(isEnd);
        setData(data);

    }

    /**
     * Validate the streamID
     * @param streamID streamID
     * @return streamID after validation
     * @throws BadAttributeException if streamID is less than the minimum streamID
     */
    public int validateStreamID(int streamID) throws BadAttributeException{
        if(streamID<MINSTREAMID){
            throw new BadAttributeException("INVALID STREAMID FOR DATA","streamID");
        }
        return streamID;
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
     * Set end value
     *
     * @param end
     *        end value
     */
    public void setEnd(boolean end){
        this.isEnd = end;
    }

    /**
     * Returns end value
     *
     * @return end value
     */
    public boolean isEnd(){
        return isEnd;
    }

    /**
     * Set data
     *
     * @param data
     *        data to set
     * @throws BadAttributeException if invalid
     */
    public void setData(byte[] data) throws BadAttributeException{

        if(data==null ){
            throw new BadAttributeException("Invalid message: null data","data",null);
        }

        if( data.length>MAXDATASIZE){
            throw new BadAttributeException("Invalid message: Data Size is too big","data");
        }

        this.data = data;

    }

    /**
     * Return Data's data
     *
     * @return data
     */
    public byte[] getData(){
        return data;
    }


    /**
     * <pre>Returns string of the form</pre>
     * <pre>
     * Data: StreamID=&lt;streamid&gt; isEnd=&lt;end&gt; data=&lt;length &gt;
     *
     * For example
     *
     * For example: Data: StreamID=5 isEnd=true data=5
     * </pre>
     *
     * {@inheritDoc}
     *
     */
    public String toString(){

        return "Data: StreamID=" + getStreamID()+ " isEnd=" +this.isEnd() + " data=" + this.data.length;
    }


    /**
     * Calculate the hashcode
     * @return hash value of the object
     */

    @Override
    public int hashCode() {

        return Objects.hash(getStreamID(), isEnd, data,getCode());
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
        Data that = (Data) o;

        return getStreamID() == that.getStreamID() && isEnd == that.isEnd && data == that.data && that.getCode()== getCode();

    }
}
