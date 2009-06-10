package com.goodworkalan.infuse;

import java.lang.reflect.Type;

// TODO Document.
public interface ObjectFactory
{
    // TODO Document.
    public Object create(Type type, Tree tree, Path context) throws FactoryException;
}