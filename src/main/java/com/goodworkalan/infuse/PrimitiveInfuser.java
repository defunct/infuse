package com.goodworkalan.infuse;

import com.goodworkalan.reflective.ReflectiveFactory;
import com.goodworkalan.utility.Primitives;

public class PrimitiveInfuser implements ObjectInfuser {
    private final ObjectInfuser infuser;
    
    private final String defaultValue;
    
    public PrimitiveInfuser(Class<?> type) {
        this(new ReflectiveFactory(), type);
    }
    
    public PrimitiveInfuser(ReflectiveFactory reflective, Class<?> type) {
        this.infuser = new StringConstructorInfuser(Primitives.box(type));
        this.defaultValue = type.isPrimitive() ? "0" : null;
    }
    
    public Object infuse(String string) {
        if (string == null) {
            if (defaultValue != null) {
                string = defaultValue;
            }
        }
        return infuser.infuse(string);
    }
}
