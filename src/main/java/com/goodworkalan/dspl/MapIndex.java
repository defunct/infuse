package com.goodworkalan.dspl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

final class MapIndex implements Index
{
    final String index;
    
    public MapIndex(String index)
    {
        this.index = index;
    }
    
    public Class<?> getRawType()
    {
        return ObjectMap.class;
    }
    
    public Object getIndex()
    {
        return index;
    }
    
    public boolean indexedBy(Class<?> cls)
    {
        return String.class.isAssignableFrom(cls);
    }

    public Type typeOf(Type type) throws PathException
    {
        if (Map.class.isAssignableFrom(PropertyPath.toClass(type)))
        {
            return ((ParameterizedType) type).getActualTypeArguments()[1];
        }
        return null;
    }
    
    public Object get(Type type, Object container, ObjectFactory factory) throws PathException
    {
        Map<Object, Object> map = PropertyPath.toMap(container);
        Object got = map.get(index);
        if (got == null && factory != null)
        {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            got = factory.create(types[1]);
            if (got == null)
            {
                throw new PathException(119).add(types[1]);
            }
            map.put(index, got);
        }
        return got;
    }
    
    public void set(Type type, Object container, Object value) throws PathException
    {
        Type[] types = ((ParameterizedType) type).getActualTypeArguments();
        if (value == null || PropertyPath.toClass(types[1]).isAssignableFrom(value.getClass()))
        {
            PropertyPath.toMap(container).put(index, value);
        }
        else
        {
            throw new PathException(120).add(type);
        }
    }
    
    @Override
    public String toString()
    {
        return "[" + escape(index) + "]";
    }
    
    final static String escape(String index)
    {
        return "'" + index.replaceAll("['\t\b\r\n\f]", "\\($1)") + "'";
    }
}