package com.goodworkalan.dspl;


public class FactoryException extends AbstractException
{
    private final static long serialVersionUID = 1L;

    public FactoryException(int code, Throwable cause)
    {
        super(code, cause);
    }
    
    public FactoryException add(Object argument)
    {
        listOfArguments.add(argument);
        return this;
    }
}
