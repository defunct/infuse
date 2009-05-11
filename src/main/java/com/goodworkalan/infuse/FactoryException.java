package com.goodworkalan.infuse;

// TODO Document.
public class FactoryException extends AbstractException
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
        listOfArguments.add(argument);
        return this;
    }
}
