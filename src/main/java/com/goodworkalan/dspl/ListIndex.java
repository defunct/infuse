package com.goodworkalan.dspl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

final class ListIndex implements Index
{
    private final int index;
    
    public ListIndex(int index)
    {
        this.index = index;
    }
    
    public Class<?> getRawType()
    {
        return ObjectList.class;
    }

    public Object getIndex()
    {
        return index;
    }
    
    public boolean indexedBy(Class<?> cls)
    {
        return int.class.isAssignableFrom(cls) || Integer.class.isAssignableFrom(cls);
    }

    public Type typeOf(Type type) throws PathException
    {
        if (type instanceof ParameterizedType)
        {
            ParameterizedType parameterized = (ParameterizedType) type;
            if (((Class<?>) parameterized.getRawType()).isAssignableFrom(List.class))
            {
                return parameterized.getActualTypeArguments()[0];
            }
        }
        return null;
    }

    public Object get(Type type, Object container, ObjectFactory factory) throws PathException
    {
        Object got = null;
        List<Object> list = PropertyPath.toList(container);
        if (index < list.size())
        {
            got = list.get(index);
        }
        if (got == null && factory != null)
        {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            got = factory.create(types[0]);
            if (got == null)
            {
                throw new PathException(117).add(types[0]);
            }
            while (list.size() <= index)
            {
                list.add(null);
            }
            list.set(index, got);
        }
        return got;
    }
    
    public void set(Type type, Object container, Object value) throws PathException
    {
        Type[] types = ((ParameterizedType) type).getActualTypeArguments();
        if (value == null || PropertyPath.toClass(types[0]).isAssignableFrom(value.getClass()))
        {
            List<Object> list = PropertyPath.toList(container);
            while (list.size() <= index)
            {
                list.add(null);
            }
            PropertyPath.toList(container).set(index, value);
        }
        else
        {
            throw new PathException(118).add(type);
        }
    }
    
    @Override
    public String toString()
    {
        return "[" + index + "]";
    }
}