package com.goodworkalan.infuse;

// FIXME Rename to path parse exception.
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
