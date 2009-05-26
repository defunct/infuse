package com.goodworkalan.infuse;

import java.lang.reflect.Method;

public class PropertyInfo
{
    private final Class<?> type;
    
    private final String name;
    
    public PropertyInfo(Class<?> type, String name)
    {
        this.type = type;
        this.name = name;
    }
    
    private String methodName(String prefix)
    {
        return (prefix + name).toLowerCase();
    }
    
    private boolean isBooleanReturn(Method method)
    {
        return boolean.class.isAssignableFrom(method.getReturnType()) || Boolean.class.isAssignableFrom(method.getReturnType());
    }

    /**
     * Return the getter method for the property. Need to add arity for indexes.
     * 
     * @return The getter method for the property.
     */
    public Method getGetter(int arity)
    {
        for (Method method : type.getMethods())
        {
            if (method.getParameterTypes().length == arity)
            {
                String matchName = method.getName().toLowerCase();
                if (isBooleanReturn(method) && methodName("is").equals(matchName))
                {
                    return method;
                }
                if (methodName("get").equals(matchName) && ! method.getReturnType().equals(Void.class))
                {
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * Return a setter method for the property. Need to add arity for indexes.
     * 
     * @return A setter method.
     */
    public Method getSetter(int arity, Class<?> propertyType)
    {
        for (Method method : type.getMethods())
        {
            if (method.getParameterTypes().length == arity + 1)
            {
                String matchName = method.getName().toLowerCase();
                if (methodName("set").equals(matchName))
                {
                    return method;
                }
            }
        }
        return null;
    }
}
