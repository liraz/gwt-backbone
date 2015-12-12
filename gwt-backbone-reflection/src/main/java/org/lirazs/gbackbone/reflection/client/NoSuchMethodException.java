package org.lirazs.gbackbone.reflection.client;


/**
 * Thrown when a particular method cannot be found.
 *
 * @author     unascribed
 * @version    1.13, 12/19/03
 * @since      JDK1.0
 */
public
class NoSuchMethodException extends Exception {
    /**
     * Constructs a <code>NoSuchMethodException</code> without a detail message.
     */
    public NoSuchMethodException() {
	super();
    }

    /**
     * Constructs a <code>NoSuchMethodException</code> with a detail message. 
     *
     * @param      s   the detail message.
     */
    public NoSuchMethodException(String s) {
	super(s);
    }
}