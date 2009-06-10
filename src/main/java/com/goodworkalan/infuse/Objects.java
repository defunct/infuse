package com.goodworkalan.infuse;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

// TODO Document.
class Objects
{
    // TODO Document.
    static Class<?> toClass(Type type)
    {
        if (type instanceof Class)
        {
            return (Class<?>) type;
        }
        else if (type instanceof ParameterizedType)
        {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
        return null;
    }
    
    // TODO Document.
    @SuppressWarnings("unchecked")
    static List<Object> toList(Object object)
    {
        return (List) object;
    }

    // TODO Make Map<String, Object>
    // TODO Document.
    @SuppressWarnings("unchecked")
    static Map<Object, Object> toMap(Object object)
    {
        return (Map) object;
    }
    
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
    
    /**
     * Return true if the class given by <code>to</code> is either the same as,
     * or is a superclass or superinterface of, the class or interface
     * represented by the class given by <code>from</code>.
     * <p>
     * Unlike {@link Class#isAssignableFrom(Class)}, this method takes Java
     * primitives into consideration.
     * 
     * @param to
     *            The class to assign to.
     * @param from
     *            The class to assign from.
     * @return True if to is the same as or is a superclass or superinterface of
     *         from.
     */
    static final boolean isAssignableFrom(Class<?> to, Class<?> from)
    {
        if (to.isPrimitive())
        {
            if (long.class.isAssignableFrom(to))
            {
                return Long.class.isAssignableFrom(from);
            }
            else if (int.class.isAssignableFrom(to))
            {
                return Integer.class.isAssignableFrom(from);
            }
            else if (short.class.isAssignableFrom(to))
            {
                return Short.class.isAssignableFrom(from);
            }
            else if (char.class.isAssignableFrom(to))
            {
                return Character.class.isAssignableFrom(from);
            }
            else if (byte.class.isAssignableFrom(to))
            {
                return Byte.class.isAssignableFrom(from);
            }
            else if (boolean.class.isAssignableFrom(to))
            {
                return Boolean.class.isAssignableFrom(from);
            }
            else if (float.class.isAssignableFrom(to))
            {
                return Float.class.isAssignableFrom(from);
            }
            else // if (double.class.isAssignableFrom(to))
            {
                return Double.class.isAssignableFrom(from);
            }
        }
        return to.isAssignableFrom(from);
    }
}
