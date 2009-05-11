package com.goodworkalan.infuse;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


// TODO Document.
final class CoreObjectFactory implements ObjectFactory
{
    // TODO Document.
    public Object create(Type type) throws FactoryException
    {
        Object created = null;
        if (type instanceof ParameterizedType)
        {
            created = create((Class<?>) ((ParameterizedType) type).getRawType());
        }
        else if (type instanceof Class)
        {
            created = create((Class<?>) type);
        }
        return created;
    }
    
    // TODO Document.
    public Object create(Class<?> cls) throws FactoryException
    {
        if (!cls.isInterface())
        {
            try
            {
                return cls.newInstance();
            }
            catch (Exception e)
            {
                throw new FactoryException(112, e).add(cls.getName());
            }
        }
        else if (SortedMap.class.isAssignableFrom(cls))
        {
            return new TreeMap<Object, Object>();
        }
        else if (Map.class.isAssignableFrom(cls))
        {
            return new HashMap<Object, Object>();
        }
        else if (List.class.isAssignableFrom(cls))
        {
            return new ArrayList<Object>();
        }
        throw new UnsupportedOperationException();
    }
    
    // TODO Document.
    public Object newBean()
    {
        return new ObjectMap();
    }
}