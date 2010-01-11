package com.goodworkalan.infuse;

import java.lang.reflect.Method;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class PropertyInfo
{
    private final Class<?> type;
    
    private final String name;
    
    private final static Pattern PROPERTY = Pattern.compile("^(get|set|is)(.*)$");
    
    public PropertyInfo(Class<?> type, Method method)
    {
        // This is because JavaNCSS gets confused parsing it where I use
        // it down below.
        Class<?> nativeBoolean = boolean.class;
        // No other reason for this local variable.

        Matcher matcher = PROPERTY.matcher(method.getName());
        if (!matcher.matches())
        {
            throw new IllegalArgumentException();
        }
        else if (matcher.group(1).equals("is") && !(nativeBoolean.isAssignableFrom(method.getReturnType()) || Boolean.class.isAssignableFrom(method.getReturnType())))
        {
            throw new IllegalArgumentException();
        }
        this.type = type;
        this.name = Character.toLowerCase(matcher.group(2).charAt(0)) + matcher.group(2).substring(1);
    }

    public PropertyInfo(Class<?> type, String name)
    {
        this.type = type;
        this.name = name;
    }
    
    public String getName()
    {
        return name;
    }
    
    private String methodName(String prefix)
    {
        return (prefix + name).toLowerCase();
    }
    
    private boolean isBooleanReturn(Method method)
    {
        return boolean.class.isAssignableFrom(method.getReturnType()) || Boolean.class.isAssignableFrom(method.getReturnType());
    }
    
    public Class<?> getPropertyType()
    {
        Method getter = getGetter(0);
        if (getter == null)
        {
            Method setter = getSetter(0);
            if (setter == null)
            {
                return null;
            }
            return setter.getParameterTypes()[0];
        }
        return getter.getReturnType();
    }
    
    // FIXME It would be nice to choose the right setter. Should I guess? Or
    // should I introduce an annotation? To guess would be to find the one
    // setter that has a complimentary getter.
    public Object[] getSetterParameters(Path path, Method setter, int index, Object object) throws NavigateException
    {
        int arity = setter.getParameterTypes().length - 1;

        if (setter != null)
        {
            Object[] parameters = new Object[arity + 1];
            if (index + arity < path.size())
            {
                for (int j = 0; j < parameters.length - 1; j++)
                {
                    try
                    {
                        parameters[j] = new Converter().fromString(setter.getParameterTypes()[j], path.get(index + j + 1).getName());
                    }
                    catch (TransmutationException e)
                    {
                        return null;
                    }
                }
                parameters[parameters.length - 1] = object;
                return parameters;
            }
        }

        return null;
    }
    
    public Object[] getGetterParameters(Path path, Method getter, int index)
    {
        int arity = getter.getParameterTypes().length;
        
        if (getter != null)
        {
            Object[] parameters = new Object[arity];
            if (index + arity < path.size() - 1)
            {
                for (int j = 0; j < parameters.length; j++)
                {
                    try
                    {
                        parameters[j] = new Converter().fromString(getter.getParameterTypes()[j], path.get(index + j + 1).getName());
                    }
                    catch (TransmutationException e)
                    {
                        return null;
                    }
                }
                return parameters;
            }
        }
        
        return null;
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
    public Method getSetter(int arity)
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
