package com.goodworkalan.infuse;

public class ParseException extends PathException
{
    private static final long serialVersionUID = 1L;

    public ParseException(int code)
    {
        super(code);
    }
    
    public ParseException(int code, Throwable cause)
    {
        super(code, cause);
    }
    
    public ParseException add(Object argument)
    {
        arguments.add(argument);
        return this;
    }
}
