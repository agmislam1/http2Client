/************************************************
 *
 * Author: Agm Islam
 * Assignment: Program 2
 * Class: CSI5321 - Advance Data Communication
 *
 ************************************************/

package megex.serialization;


/**
 * Thrown if problem with attribute
 *
 * @version 1.0
 * @author Agm Islam
 * Date: 13 Feb 2023
 */
public class BadAttributeException extends Exception{

    /**
     * Attribute Name
     */
    private String attribute;                        // Name of the Attribute
    private static final long serialVersionUID = 1L; // Universal version identifier



    /**
     * Constructs a BadAttributeException with given message, attribute, and cause
     *
     * @param message
     *         detail message
     * @param attribute
     *         attribute related to problem
     */
    public BadAttributeException(String message, String attribute) {


        super(message);
        if (message==null || attribute==null) {
            throw new NullPointerException("Message and attribute cannot be null");
        }

        this.attribute = attribute;
    }

    /**
     * Constructs a BadAttributeException with given message and attribute with no given cause
     *
     * @param message
     *         detail message
     * @param attribute
     *         attribute related to problem
     * @param cause
     *         underlying cause (null is permitted and indicates no or unknown cause)
     */
    public BadAttributeException(String message, String attribute, Throwable cause) {

        super(message,cause);
        if (message==null || attribute==null) {
            throw new NullPointerException("Message and attribute cannot be null");
        }

        this.attribute = attribute;
    }

    /**
     * Return attribute related to problem
     *
     * @return attribute name
     */
    public String getAttribute() {

        return attribute;
    }


}
