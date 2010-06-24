package com.goodworkalan.infuse;

import com.goodworkalan.utility.Primitives;

// TODO Document.
public class PrimitiveInfuser implements ObjectInfuser {
    // TODO Document.
    private final ObjectInfuser infuser;
    
    // TODO Document.
    private final String defaultValue;
    
    // TODO Document.
    public PrimitiveInfuser(Class<?> type) {
        this.infuser = new StringConstructorInfuser(Primitives.box(type));
        this.defaultValue = type.isPrimitive() ? "0" : null;
    }
    
    // TODO Document.
    public Object infuse(String string) {
        if (string == null) {
            if (defaultValue != null) {
                string = defaultValue;
            }
        }
        return infuser.infuse(string);
    }
}
