package com.goodworkalan.infuse;

import java.lang.reflect.Type;
import java.util.List;

/**
 * A strategy to dereference a numeric or string index.
 * 
 * @author Alan Gutierrez
 */
interface Index
{
    // TODO Document.
    public Class<?> getRawType();

    // TODO Document.
    public boolean indexedBy(Class<?> cls);
    
    // TODO Document.
    public Object getIndex(boolean escape);
    
    // TODO Document.
    public Index duplicate();

    // TODO Document.
    public Type typeOf(Type type) throws PathException;
    
    // TODO Document.
    public Object get(Type type, Object container, ObjectFactory factory) throws PathException;
    
    // TODO Document.
    public void set(Type type, Object container, Object value) throws PathException;

    // TODO Document.
    public void glob(Object bean, PropertyPath path, List<PropertyPath> glob) throws PathException;
}