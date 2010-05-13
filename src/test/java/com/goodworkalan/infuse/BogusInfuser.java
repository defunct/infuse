package com.goodworkalan.infuse;

/**
 * A bogus infuser to test infuser assignment.
 * 
 * @author Alan Gutierrez
 */
public class BogusInfuser implements ObjectInfuser {
    /**
     * Create a new bogus infuser.
     * 
     * @param type
     *            The type to convert.
     */
    public BogusInfuser(Class<?> type ){
    }
    
    /**
     * Return the number 1.
     * 
     * @param string
     *            The string to convert.
     */
    public Object infuse(String string) {
        return 1;
    }
}
