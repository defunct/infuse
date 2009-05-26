package com.goodworkalan.infuse;

public class Transmutator
{
    public Class<?> box(Class<?> type)
    {
        if (type.isPrimitive())
        {
            if (long.class.isAssignableFrom(type))
            {
                return Long.class;
            }
            else if (int.class.isAssignableFrom(type))
            {
                return Integer.class;
            }
            else if (short.class.isAssignableFrom(type))
            {
                return Short.class;
            }
            else if (char.class.isAssignableFrom(type))
            {
                return Character.class;
            }
            else if (byte.class.isAssignableFrom(type))
            {
                return Byte.class;
            }
            else if (boolean.class.isAssignableFrom(type))
            {
                return Boolean.class;
            }
            else if (float.class.isAssignableFrom(type))
            {
                return Float.class;
            }
            return Double.class;
        }
        return type;
    }
    
    public Object transmute(Class<?> target, String string) throws Exception
    {
        target = box(target);
        if (target.equals(Object.class) || String.class.isAssignableFrom(target))
        {
            return string;
        }
        else if (Character.class.isAssignableFrom(target))
        {
            if (string.length() == 1)
            {
                return new Character(string.charAt(0));
            }
            throw new Exception();
        }
        return target.getConstructor(new Class<?>[] { String.class }).newInstance(target);
    }
}
