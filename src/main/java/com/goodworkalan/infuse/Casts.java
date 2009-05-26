package com.goodworkalan.infuse;

import java.util.Map;

public class Casts
{
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toStringToObject(Object map)
    {
        return (Map<String, Object>) map;
    }
}
