package com.goodworkalan.infuse;

// TODO Document.
public class FactoryException extends PathException
{
    // TODO Document.
    private final static long serialVersionUID = 1L;

    // TODO Document.
    public FactoryException(int code, Throwable cause)
    {
        super(code, cause);
    }
    
    // TODO Document.
    public FactoryException add(Object argument)
    {
        arguments.add(argument);
        return this;
    }
}