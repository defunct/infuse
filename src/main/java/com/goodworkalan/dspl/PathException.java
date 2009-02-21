package com.goodworkalan.dspl;

// TODO Document.
public final class PathException extends AbstractException
{
    // TODO Document.
    private final static long serialVersionUID = 1L;
        
    // TODO Document.
    public PathException(int code)
    {
        super(code);
    }
    
    // TODO Document.
    public PathException(int code, Throwable cause)
    {
        super(code, cause);
    }
    
    // TODO Document.
    public PathException add(Object argument)
    {
        listOfArguments.add(argument);
        return this;
    }
}