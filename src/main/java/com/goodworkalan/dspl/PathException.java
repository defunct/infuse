package com.goodworkalan.dspl;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class PathException extends Exception
{
    private final static long serialVersionUID = 1L;
    
    private final List<Object> listOfArguments = new ArrayList<Object>(); 
    
    private final int code;
    
    public PathException(int code)
    {
        this.code = code;
    }
    
    public String getKey()
    {
        return Integer.toString(code);
    }
    
    public PathException(int code, Throwable cause)
    {
        super(cause);
        this.code = code;
    }
    
    public PathException add(Object argument)
    {
        listOfArguments.add(argument);
        return this;
    }
    
    @Override
    public String getMessage()
    {
        String key = getKey();
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
            throw new java.lang.Error(key, e);
        }
    }
}