package com.goodworkalan.infuse;

/**
 * An infuser that loads a class from a class name.
 *
 * @author Alan Gutierrez
 */
public class ClassInfuser implements ObjectInfuser {
    /**
     * Ignores the given type since this infuser will always return a class.
     * 
     * @param type
     *            The type to create when converting.
     */
    public ClassInfuser(Class<?> type) {
    }

    /**
     * Attempt to load the given class or else return null if the class cannot
     * be found.
     * 
     * @param string
     *            The class name.
     * @return The class or null if the class cannot be found.
     */
    public Object infuse(String string) {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(string);
        } catch (ClassNotFoundException e) {
            throw new InfusionException(ClassInfuser.class, "classNotFound", string);
        }
    }
}
