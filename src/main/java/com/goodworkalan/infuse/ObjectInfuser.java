package com.goodworkalan.infuse;

/**
 * Create a Java object from a string.
 * <p>
 * Split out object infuser for use without the object infuser server.
 * 
 * @author Alan Gutierrez
 */
public interface ObjectInfuser {
    /**
     * Convert the given string into an object.
     * 
     * @param string
     *            The string.
     * @return An object.
     */
    public Object infuse(String string);
}
