package com.goodworkalan.dspl;

public final class PathException extends AbstractException
{
    private final static long serialVersionUID = 1L;
        
    public PathException(int code)
    {
        super(code);
    }
    
    public PathException(int code, Throwable cause)
    {
        super(code, cause);
    }
    
    public PathException add(Object argument)
    {
        listOfArguments.add(argument);
        return this;
    }
}