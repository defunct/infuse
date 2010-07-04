package com.goodworkalan.infuse;

import static com.goodworkalan.infuse.InfusionException.$;

import java.lang.reflect.Constructor;

/**
 * Converts strings to objects by calling the single string argument constructor
 * of the converted type.
 * 
 * @author Alan Gutierrez
 */
public class StringConstructorInfuser implements ObjectInfuser {
    /** The constructor. */
    private final Constructor<?> constructor;

    /**
     * Create a string constructor converter that converts strings to the given
     * type. The given type must implement a public single string argument
     * constructor.
     * 
     * @param type
     *            The type to convert to.
     * @exception InfusionException
     *                If a public single string argument constructor cannot be
     *                found for the target type.
     */
    public StringConstructorInfuser(final Class<?> type) {
        try {
            this.constructor = type.getConstructor(String.class);
        } catch (Throwable e) {
            throw new InfusionException($(e), StringConstructorInfuser.class, "get.constructor", e, type);
        }
    }

	/**
	 * Convert the given string into the type associated with this object
	 * infuser by calling the single string argument constructor for the target
	 * type. Returns null if the given string is null.
	 * 
	 * @param string
	 *            The string to convert.
	 * @return An instance of the target type or null.
	 * @exception InfusionException
	 *                If the reflective constructor invocation raises an
	 *                exception.
	 */
    public Object infuse(final String string) {
        if (string == null) {
            return null;
        }
        try {
            return constructor.newInstance(string);
        } catch (Throwable e) {
            throw new InfusionException($(e), StringConstructorInfuser.class, "new.instance", constructor.getDeclaringClass(), string);
        }
    }
}
