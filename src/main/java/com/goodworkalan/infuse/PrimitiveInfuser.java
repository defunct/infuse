package com.goodworkalan.infuse;

import com.goodworkalan.utility.Primitives;

/**
 * Create an instance of the <code>Object</code> version wrapper of a Java
 * primitive from a string, using the default value for the primitive if an
 * actual primitive, and now a wrapper object is expected, and if the string is
 * null.
 * 
 * @author Alan Gutierrez
 */
public class PrimitiveInfuser implements ObjectInfuser {
    /** The string constructor infuser. */
    private final ObjectInfuser infuser;
    
    /** The default value. */
    private final String defaultValue;

    /**
     * Create a primitive infuser for the given type. If the type is an actual
     * primitive type, as opposed to a wrapper for the primitive type, then use
     * zero as the default value. Otherwise, use null as the default value.
     * 
     * @param type
     *            The type.
     */
    public PrimitiveInfuser(Class<?> type) {
        this.infuser = new StringConstructorInfuser(Primitives.box(type));
        this.defaultValue = type.isPrimitive() ? "0" : null;
    }

    /**
     * Create a new primitive or primitive wrapper object using the given
     * string.
     * 
     * @param string
     *            The string.
     * @return A primitive or primitive wrapper object.
     */
    public Object infuse(String string) {
        if (string == null) {
            if (defaultValue != null) {
                string = defaultValue;
            }
        }
        return infuser.infuse(string);
    }
}
