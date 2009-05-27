package com.goodworkalan.infuse;

import java.lang.reflect.Type;

// TODO Document.
public interface ObjectFactory
{
    // TODO Document.
    public Object create(InfusionBuilder builder, Type type, Path context) throws FactoryException;
}