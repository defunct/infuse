package com.goodworkalan.infuse;

import java.util.List;
import java.util.Map;

// TODO Meld with Objects.
public class Casts
{
    @SuppressWarnings("unchecked")
    public static Map<Object, Object> toObjectMap(Object map)
    {
        return (Map<Object, Object>) map;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> toStringToObject(Object map)
    {
        return (Map<String, Object>) map;
    }
    
    @SuppressWarnings("unchecked")
    public static List<Object> toObjectList(Object list)
    {
        return (List<Object>) list;
    }
}
