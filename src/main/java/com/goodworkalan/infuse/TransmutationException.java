package com.goodworkalan.infuse;

public class TransmutationException extends PathException
{
    private static final long serialVersionUID = 1L;
    
    public TransmutationException(int code)
    {
        super(code);
    }

    public TransmutationException(int code, Throwable cause)
    {
        super(code, cause);
    }
}
