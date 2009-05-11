package com.goodworkalan.infuse;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

// TODO Maybe there is a PathException and Parse/Navigate/Create exceptions.
// TODO Document.
class AbstractException extends Exception
{
    // TODO Document.
    private final static long serialVersionUID = 1L;
    
    // TODO Document.
    protected final List<Object> listOfArguments = new ArrayList<Object>(); 
    
    // TODO Document.
    private final int code;
    
    // TODO Document.
    public AbstractException(int code)
    {
        this.code = code;
    }
    
    // TODO Document.
    public AbstractException(int code, Throwable cause)
    {
        super(cause);
        this.code = code;
    }
    
    // TODO Document.
    public int getCode()
    {
        return code;
    }
    
    // TODO Document.
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
            return String.format(format, listOfArguments.toArray());
        }
        catch (Throwable e)
        {
            throw new Error(key, e);
        }
    }
}
