package com.goodworkalan.dspl;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

class AbstractException extends Exception
{
    private final static long serialVersionUID = 1L;
    
    protected final List<Object> listOfArguments = new ArrayList<Object>(); 
    
    private final int code;
    
    public AbstractException(int code)
    {
        this.code = code;
    }
    
    public AbstractException(int code, Throwable cause)
    {
        super(cause);
        this.code = code;
    }
    
    public int getCode()
    {
        return code;
    }
    
    @Override
    public String getMessage()
    {
        String key = Integer.toString(code);
        ResourceBundle exceptions = ResourceBundle.getBundle("com.goodworkalan.dspl.exceptions");
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
