package com.goodworkalan.dspl;

import java.lang.reflect.Type;

public interface ObjectFactory
{
    public Object create(Type type) throws PathException;
    
    public Object newBean();
}