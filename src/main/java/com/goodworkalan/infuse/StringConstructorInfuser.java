package com.goodworkalan.infuse;

import com.goodworkalan.reflective.Constructor;
import com.goodworkalan.reflective.ReflectiveException;
import com.goodworkalan.reflective.ReflectiveFactory;

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
     * Create a string constructor infuser using the given single string
     * argument constructor.
     * 
     * @param constructor
     *            The constructor.
     */
    public StringConstructorInfuser(Constructor<?> constructor) {
        this.constructor = constructor;
    }

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
    public StringConstructorInfuser(Class<?> type) {
        this(new ReflectiveFactory(), type);
    }

    /**
     * Create a string constructor converter that converts strings to the given
     * type that discovers the constructor using the given reflective factory.
     * The given type must implement a public single string argument
     * constructor.
     * <p>
     * The reflective factory is used in unit testing to simulate reflection
     * failures.
     * 
     * @param reflective
     *            The reflective factory to use for reflection.
     * @param type
     *            The type to convert to.
     * @exception InfusionException
     *                If a public single string argument constructor cannot be
     *                found for the target type.
     */
    public StringConstructorInfuser(ReflectiveFactory reflective, Class<?> type) {
        try {
            this.constructor = reflective.getConstructor(type, String.class);
        } catch (ReflectiveException e) {
            throw new InfusionException(0, e, type);
        }
    }

    /**
     * Convert the given string into the type associated with this object
     * infuser by calling the single string argument constructor for the target
     * type. Returns null if the given string is null.
     * 
     * @param The
     *            string to convert.
     * @return An instance of the target type or null.
     * @exception InfusionException
     *                If the reflective constructor invocation raises an
     *                exception.
     */
    public Object infuse(String string) {
        if (string == null) {
            return null;
        }
        try {
            return constructor.newInstance(string);
        } catch (ReflectiveException e) {
            throw new InfusionException(0, e, string);
        }
    }
}
