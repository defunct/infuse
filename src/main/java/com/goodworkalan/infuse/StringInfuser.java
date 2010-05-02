package com.goodworkalan.infuse;

/**
 * An infuser that performs no conversion and simply returns the given string.
 *
 * @author Alan Gutierrez
 */
public class StringInfuser implements ObjectInfuser {
    /** The static instance for use outside of the infuser server. */
    public static ObjectInfuser INSTNACE = new StringInfuser(String.class);

    /**
     * Ignores the given type since this infuser will always return a string.
     * 
     * @param type
     *            The type to create when converting.
     */
    public StringInfuser(Class<?> type) {
    }

    /**
     * Performs no conversion, simply return the given string.
     * 
     * @param The
     *            string to convert.
     * @return The given string.
     */
    public Object infuse(String string) {
        return string;
    }
}
