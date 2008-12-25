package com.goodworkalan.dspl;

import java.lang.reflect.Type;

public interface ObjectFactory
{
    public Object create(Type type) throws FactoryException;
    
    public Object newBean();
}