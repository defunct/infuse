package com.goodworkalan.dspl;

import java.lang.reflect.Type;
import java.util.List;


interface Index
{
    public Class<?> getRawType();

    public boolean indexedBy(Class<?> cls);
    
    public Object getIndex(boolean escape);
    
    public Index duplicate();

    public Type typeOf(Type type) throws PathException;
    
    public Object get(Type type, Object container, ObjectFactory factory) throws PathException;
    
    public void set(Type type, Object container, Object value) throws PathException;

    public void glob(Object bean, PropertyPath path, List<PropertyPath> glob) throws PathException;
}