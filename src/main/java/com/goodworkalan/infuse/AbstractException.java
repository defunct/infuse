package com.goodworkalan.infuse;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

// TODO Maybe there is a PathException and Parse/Navigate/Create exceptions.
/**
 * Base exception for exceptions thrown by this library.
 * 
 * @author Alan Gutierrez
 */
class AbstractException extends Exception
{
    /** Serial version id. */
    private final static long serialVersionUID = 1L;
    
    /** A list of arguments to the formatted error message. */
    protected final List<Object> arguments = new ArrayList<Object>(); 
    
    /** The error code. */
    private final int code;
    
    /**
     * Create an exception with the given error code.
     * 
     * @param code The error code.
     */
    public AbstractException(int code)
    {
        this.code = code;
    }

    /**
     * Create an exception with the given error code and the given cause.
     * 
     * @param code
     *            The error code.
     * @param cause
     *            The cause exception.
     */
    public AbstractException(int code, Throwable cause)
    {
        super(cause);
        this.code = code;
    }
    
    /**
     * Get the error code.
     * 
     * @return The error code.
     */
    public int getCode()
    {
        return code;
    }
    
    /**
     * Create an detail message from the error message format associated with
     * the error code and the format arguments.
     * 
     * @return The exception message.
     */
    @Override
    public String getMessage()
    {
        String key = Integer.toString(code);
        ResourceBundle exceptions = ResourceBundle.getBundle("com.goodworkalan.infuse.exceptions");
        String format;
        try
        {
            format = exceptions.getString(key);
        }
        catch (MissingResourceException e)
        {
            return key;
        }
        try
        {
            return String.format(format, arguments.toArray());
        }
        catch (Throwable e)
        {
            throw new Error(key, e);
        }
    }
}
