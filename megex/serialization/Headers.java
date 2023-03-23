/************************************************
 *
 * Author: Agm Islam
 * Assignment: Program 2
 * Class: CSI5321 - Advance Data Communication
 *
 ************************************************/

package megex.serialization;

import java.util.*;

/**
 * Note: The order of name/value pairs must be the insertion order.
 * Duplicate names are not allowed; adding a duplicate name results in a replacement name/value at the end of the list.
 * Given the insertion order a=dog, b=cat, a=bird, the name/value pair order will be b=cat, a=bird.
 * Any output involving names much preserve this order (getNames(), encoding, etc.)
 *
 * @version 1.4
 * @author Agm Islam
 * Date: 5 March 2023
 */
public class Headers extends Message {

    private boolean isEnd;                     // indicates end
    private static final int MINSTREAMID = 1;  // Minimum Stream ID for Header
    private static final byte CODE = 0x1;      // Type Code for Data
    private Map<String, String> headers;       // List of Headers

    // Range for VISCHAR Values
    private static final byte VISCHARSTARTVAL = 0x21;
    private static final byte VISCHARENDVAL = 0x7E;

    // Additional values for VCHAR Value
    private static final byte VCHAR1 = 0x20;
    private static final byte VCHAR2 = 0x9;

    // List of delimeter characters
    private static final List<Character> DELIM = Arrays.asList('(', ')', ',', '/',
                                                                    ';','<','=','>','?',
                                                                    '@','[','\'',']','{','}');
    /**
     * Creates Headers message from given values
     * @param streamID stream ID
     * @param isEnd true if last header
     * @throws BadAttributeException if attribute invalid (see protocol spec)
     */
    public Headers(int streamID, boolean isEnd) throws BadAttributeException{
        super(streamID);
        setEnd(isEnd);
        headers = new LinkedHashMap<>();
    }

    /**
     * Return end value
     * @return end Value
     */
    public boolean isEnd(){

        return isEnd;
    }

    /**
     * Set end value
     * @param end end Value
     */
    public void setEnd(boolean end){

        isEnd = end;
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
     * Returns string of the form
     * <pre>
     * Headers: StreamID=&lt;streamid&gt; isEnd=&lt;end&gt; ([&lt;name&gt; = &lt;value&gt;]...[&lt;name&gt; = &lt;value&gt;])
     * The name/value pairs should be output in sorted (natural) order by name. For example
     *
     * Headers: StreamID=5 isEnd=false ([color=blue][method=GET])
     * </pre>
     *
     * {@inheritDoc}
     */
    public String toString(){

        String output;
        output = "Headers: StreamID=" + getStreamID() + " isEnd=" + isEnd() + " (";
        for (String s : getNames()) {
            output += "[" + s + "=" + getValue(s) + "]";
        }
        output+=")";
        return output;
    }

    /**
     * Get the Headers value associated with the given name
     * @param name the name for which to find the associated value
     * @return the value associated with the name or null if the association cannot be found (e.g., no such name, invalid name, etc.)
     */
    public String getValue(String name){

        return headers.get(name);
    }

    /**
     * Get (potentially empty) set of names in Headers
     * @return (non-null) set of names in sort order
     */
    public Set<String> getNames(){

        return new LinkedHashSet<>(headers.keySet());

    }

    /**
     * Add name/value pair to header. If the name is already contained in the header, the corresponding value is replaced by the new value.
     * @param name name to add
     * @param value value to add/replace
     * @throws BadAttributeException if invalid name or value
     */
    public void addValue(String name, String value) throws BadAttributeException{

        if (!isValidKey(name) || name.equals("")) {
            throw new BadAttributeException("Invalid message: Invalid header key - " + name, "name", null);
        }
        if (!isValidValue(value) ||  value.equals("")) {
            throw new BadAttributeException("Invalid message: Invalid header value - " + value, "value", null);
        }

        if(headers.containsKey(name)){
            // Remove the header to maintain the insertion sort order
            headers.remove(name);
        }
        headers.put(name, value);

    }



    /**
     * returns whether the string is a valid Header Key
     * @param s A string
     * @return true if it NCHAR
     * @throws BadAttributeException if string is null
     */
    private boolean isValidKey(String s) throws BadAttributeException {

        if(s==null){
            throw new BadAttributeException("Invalid message: Empty Header", "s");
        }
        // check if s is a valid NCHAR
        for(int i=0; i<s.length(); i++){
            char c = s.charAt(i);
            if(c < VISCHARSTARTVAL || c> VISCHARENDVAL || DELIM.contains(c) || Character.isUpperCase(c)){
                return false;
            }

        }
        return true;

    }

    /**
     * returns whether the string is a valid Header Value
     * @param s A string
     * @return true if it VCHAR
     * @throws BadAttributeException if string is null
     */
    private boolean isValidValue(String s) throws BadAttributeException {

        if(s==null){
            throw new BadAttributeException("Invalid message: Empty Header", "s");
        }
        // check if s is a valid NCHAR
        for(int i=0; i<s.length(); i++){
            char c = s.charAt(i);
            if((c < VISCHARSTARTVAL || c> VISCHARENDVAL) && (c!=VCHAR1) && (c!=VCHAR2) ){
                return false;
            }

        }
        return true;

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
     * Calculate the hashcode
     * @return hash value of the object
     */

    @Override
    public int hashCode() {

        // include the header name and value

        int hashCode = 1;

        // Preserve the order of insertion
        for (Map.Entry<String, String> entry1 : headers.entrySet()) {
            hashCode = 31 * hashCode + Objects.hashCode(entry1.getKey());
            hashCode = 31 * hashCode + Objects.hashCode(entry1.getValue());
        }

        return Objects.hash(getStreamID(),getCode(), isEnd(),hashCode);
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
        Headers that = (Headers) o;

        if(!(getStreamID() == that.getStreamID() && isEnd == that.isEnd() && that.getCode()== getCode())) {
            return false;
        }


        if(headers.size()!=that.headers.size()){
            return false;
        }

        // Preserve the order of insertion
        if(headers.size()>0) {

            int i = 0;
            for (Map.Entry<String, String> entry1 : headers.entrySet()) {
                Map.Entry<String, String> entry2 = (Map.Entry<String, String>) that.headers.entrySet().toArray()[i++];
                if (!Objects.equals(entry1.getKey(), entry2.getKey()) ||
                        !Objects.equals(entry1.getValue(), entry2.getValue())) {
                    return false;
                }
            }
        }

        return true;

    }



}
