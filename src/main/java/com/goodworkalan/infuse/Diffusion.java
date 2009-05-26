package com.goodworkalan.infuse;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;


public class Diffusion
{
    private final PropertyList properties;
    
    public Diffusion(String path) throws ParseException
    {
        this.properties = new PropertyList(path, true);
    }

    public Diffusion(PropertyList properties)
    {
        this.properties = properties;
    }

    public Object get(Object object) throws NavigateException
    {
        return get(object, properties, 0);
    }
    
    private Object get(Object object, PropertyList properties, int index) throws NavigateException
    {
        Property property = properties.get(index);
        if (property.getName().equals("this") && !property.isIndex())
        {
            return get(object, properties, index + 1);
        }
        if (object instanceof Map)
        {
            Map<String, Object> map = Casts.toStringToObject(object);
            return map.get(property.getName());
        }
        else if (object instanceof List)
        {
            return null;
        }
        else if (object.getClass().isArray())
        {
            return null;
        }
        PropertyInfo propertyInfo = new PropertyInfo(object.getClass(), property.getName());
        int arity = properties.arityAtIndex(index);
        Object child = null;
        ARITY: for (int i = arity; child == null && -1 < i; i--)
        {
            Method getter = propertyInfo.getGetter(i);
            if (getter == null)
            {
                continue ARITY;
            }
            Object[] parameters = new Object[i];
            for (int j = 0; j < parameters.length; j++)
            {
                if (String.class.isAssignableFrom(getter.getParameterTypes()[j]))
                {
                    try
                    {
                        parameters[j] = new Transmutator().transmute(getter.getParameterTypes()[j], properties.get(index + j + 1).getName());
                    }
                    catch (Exception e)
                    {
                        continue ARITY;
                    }
                }
            }
            try
            {
                child = getter.invoke(object, parameters);
            }
            catch (Exception e)
            {
                throw new NavigateException(100, e);
            }
            if (child == null)
            {
                continue ARITY;
            }
            if (index + i == properties.size() - 1)
            {
                return child;
            }
            else
            {
                return get(child, properties, index + i + 1);
            }
        }
        return child;
    }
}
