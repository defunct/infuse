package com.goodworkalan.infuse;

import static com.goodworkalan.infuse.Objects.toClass;
import static com.goodworkalan.infuse.Objects.toList;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

// TODO Index arrays.
final class ListIndex implements Index
{
    // TODO Document.
    private final int index;
    
    // TODO Document.
    public ListIndex(int index)
    {
        this.index = index;
    }
    
    // TODO Document.
    public Class<?> getRawType()
    {
        return ObjectList.class;
    }

    // TODO Document.
    public Object getIndex(boolean escape)
    {
        return index;
    }
    
    // TODO Document.
    public boolean indexedBy(Class<?> cls)
    {
        return int.class.isAssignableFrom(cls) || Integer.class.isAssignableFrom(cls);
    }
    
    // TODO Document.
    public Index duplicate()
    {
        return new ListIndex(index);
    }

    // TODO Document.
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

    // TODO Document.
    public Object get(Type type, Object container, ObjectFactory factory) throws PathException
    {
        Object got = null;
        List<Object> list = toList(container);
        if (index < list.size())
        {
            got = list.get(index);
        }
        if (got == null && factory != null)
        {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            try
            {
                got = factory.create(types[0]);
            }
            catch (FactoryException e)
            {
                throw new PathException(133, e).add(types[0]);
            }
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
    
    // TODO Document.
    public void set(Type type, Object container, Object value) throws PathException
    {
        Type[] types = ((ParameterizedType) type).getActualTypeArguments();
        if (value == null || toClass(types[0]).isAssignableFrom(value.getClass()))
        {
            List<Object> list = toList(container);
            while (list.size() <= index)
            {
                list.add(null);
            }
            toList(container).set(index, value);
        }
        else
        {
            throw new PathException(118).add(type);
        }
    }

    // TODO Document.
    public void glob(Object bean, PropertyPath path, List<PropertyPath> glob) throws PathException
    {
        List<Object> list = toList(path.get(bean));
        if (index < list.size() && list.get(index) != null)
        {
            path.getLastProperty().addIndex(new ListIndex(index));
            glob.add(path);
        }
    }

    // TODO Document.
    @Override
    public String toString()
    {
        return "[" + index + "]";
    }
}