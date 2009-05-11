package com.goodworkalan.infuse;

import java.lang.reflect.Type;

// TODO Document.
public interface ObjectFactory
{
    // TODO Document.
    public Object create(Type type) throws FactoryException;
    
    // TODO Document.
    public Object newBean();
}