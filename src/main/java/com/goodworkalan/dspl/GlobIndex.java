package com.goodworkalan.dspl;

import static com.goodworkalan.dspl.Objects.toList;
import static com.goodworkalan.dspl.Objects.toMap;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

class GlobIndex implements Index
{
    public Class<?> getRawType()
    {
        throw new UnsupportedOperationException();
    }
    
    public Object getIndex(boolean escape)
    {
        return "*";
    }
    
    public boolean indexedBy(Class<?> cls)
    {
        throw new UnsupportedOperationException();
    }
    
    public Index duplicate()
    {
        return new GlobIndex();
    }
    
    public Type typeOf(Type type) throws PathException
    {
        throw new UnsupportedOperationException();
    }
    
    public Object get(Type type, Object container, ObjectFactory factory)
            throws PathException
    {
        throw new UnsupportedOperationException();
    }
    
    public void set(Type type, Object container, Object value)
            throws PathException
    {
        throw new UnsupportedOperationException();
    }
    
    public void glob(Object bean, PropertyPath path, List<PropertyPath> glob) throws PathException
    {
        Object object = path.get(bean);
        if (object instanceof Map)
        {
            Map<Object, Object> map = toMap(object);
            for (Map.Entry<Object, Object> entry : map.entrySet())
            {
                if (entry.getValue() != null)
                {
                    PropertyPath duplicate = path.duplicate();
                    duplicate.getLastProperty()
                             .addIndex(new MapIndex((String) entry.getKey()));
                    glob.add(duplicate);
                }
            }
        }
        else if (object instanceof List)
        {
            List<Object> list = toList(object);
            int index = 0;
            for (Object value : list)
            {
                if (value != null)
                {
                    PropertyPath duplicate = path.duplicate();
                    duplicate.getLastProperty()
                             .addIndex(new ListIndex(index));
                    glob.add(duplicate);
                }
                index++;
            }
        }
    }
}
