package com.goodworkalan.infuse;

import java.util.Date;

import com.goodworkalan.reflective.ReflectiveException;
import com.goodworkalan.reflective.ReflectiveFactory;

public class Converter {
    private final ReflectiveFactory reflectiveFactory;

    /**
     * Construct a converter that converts objects using the given reflective
     * factory.
     * 
     * @param reflectiveFactory The reflective factory.
     */
    public Converter(ReflectiveFactory reflectiveFactory) {
        this.reflectiveFactory = reflectiveFactory;
    }
    
    /**
     * Default constructor.
     */
    public Converter() {
        this(new ReflectiveFactory());
    }

    /**
     * Convert a primitive type to an Object derived type, or return the given
     * type if it is already and Object derived type.
     * 
     * @param type
     *            The type to convert.
     * @return An Object derived type.
     */
    public static Class<?> box(Class<?> type) {
        if (type.isPrimitive()) {
            if (long.class.isAssignableFrom(type)) {
                return Long.class;
            } else if (int.class.isAssignableFrom(type)) {
                return Integer.class;
            } else if (short.class.isAssignableFrom(type)) {
                return Short.class;
            } else if (char.class.isAssignableFrom(type)) {
                return Character.class;
            } else if (byte.class.isAssignableFrom(type)) {
                return Byte.class;
            } else if (boolean.class.isAssignableFrom(type)) {
                return Boolean.class;
            } else if (float.class.isAssignableFrom(type)) {
                return Float.class;
            }
            return Double.class;
        }
        return type;
    }

    public Object fromString(Class<?> target, String string)
    throws TransmutationException {
        if (string == null) {
            if (target.isPrimitive()) {
                throw new TransmutationException(PathException.NO_REAL_MESSAGE);
            }
            return null;
        }
        target = box(target);
        if (target.equals(Object.class) || String.class.isAssignableFrom(target)) {
            return string;
        } else if (Character.class.isAssignableFrom(target)) {
            if (string.length() == 1) {
                return new Character(string.charAt(0));
            }
            throw new TransmutationException(PathException.NO_REAL_MESSAGE);
        } else if (Date.class.isAssignableFrom(target)) {
            return new Date((Long) fromString(Long.class, string));
        }
        try {
            return reflectiveFactory.getConstructor(target, String.class).newInstance(string);
        } catch (ReflectiveException e) {
            throw new TransmutationException(PathException.NO_REAL_MESSAGE, e);
        }
    }
    
    public String toString(Object target) {
        if (target instanceof Date) {
            return Long.toString(((Date) target).getTime());
        }
        return target.toString();
    }
}
