package com.goodworkalan.infuse;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class CollectionFactory implements ObjectFactory
{
    public Object create(Type type, Tree tree, Path context) throws FactoryException
    {
        Class<?> cls = Objects.toClass(type);
        if (cls != null)
        {
            if (SortedMap.class.isAssignableFrom(cls))
            {
                return new TreeMap<Object, Object>();
            }
            else if (Map.class.isAssignableFrom(cls))
            {
                return new LinkedHashMap<Object, Object>();
            }
            else if (List.class.isAssignableFrom(cls))
            {
                return new ArrayList<Object>();
            }
        }
        return null;
    }
}
