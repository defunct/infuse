package com.goodworkalan.infuse;

import static com.goodworkalan.infuse.Objects.toList;
import static com.goodworkalan.infuse.Objects.toMap;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

// TODO Document.
class GlobIndex implements Index
{
    /**
     * Throws an {@link UnsupportedOperationException} to indicate that there is
     * not raw type for a glob index.
     * 
     * @return Does not return.
     */
    public Class<?> getRawType()
    {
        throw new UnsupportedOperationException();
    }
    
    // TODO Document.
    public Object getIndex(boolean escape)
    {
        return "*";
    }
    
    // TODO Document.
    public boolean indexedBy(Class<?> cls)
    {
        throw new UnsupportedOperationException();
    }
    
    // TODO Document.
    public Index duplicate()
    {
        return new GlobIndex();
    }
    
    // TODO Document.
    public Type typeOf(Type type) throws PathException
    {
        throw new UnsupportedOperationException();
    }
    
    // TODO Document.
    public Object get(Type type, Object container, ObjectFactory factory)
            throws PathException
    {
        throw new UnsupportedOperationException();
    }
    
    // TODO Document.
    public void set(Type type, Object container, Object value)
            throws PathException
    {
        throw new UnsupportedOperationException();
    }
    
    // TODO Document.
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
