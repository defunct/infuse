package com.goodworkalan.infuse;

// TODO Document.
public final class NavigateException extends PathException
{
    // TODO Document.
    private final static long serialVersionUID = 1L;
        
    // TODO Document.
    public NavigateException(int code)
    {
        super(code);
    }
    
    // TODO Document.
    public NavigateException(int code, Throwable cause)
    {
        super(code, cause);
    }
    
    // TODO Document.
    public NavigateException add(Object argument)
    {
        arguments.add(argument);
        return this;
    }
}