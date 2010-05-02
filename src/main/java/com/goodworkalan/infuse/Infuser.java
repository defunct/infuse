package com.goodworkalan.infuse;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.goodworkalan.reflective.ReflectiveException;
import com.goodworkalan.reflective.ReflectiveFactory;
import com.goodworkalan.utility.ClassAssociation;
import com.goodworkalan.utility.Primitives;

public class Infuser {
    private final ReflectiveFactory reflective;
    
    private final ConcurrentMap<Class<?>, ObjectInfuser> infusers = new ConcurrentHashMap<Class<?>, ObjectInfuser>();

    private final ClassAssociation<Class<? extends ObjectInfuser>> associations = new ClassAssociation<Class<? extends ObjectInfuser>>();
    
    public Infuser() {
        this(new ReflectiveFactory());
    }
    
    Infuser(ReflectiveFactory reflective) {
        this.reflective = reflective;
        
        associations.assignable(Boolean.class, PrimitiveInfuser.class);
        associations.assignable(Byte.class, PrimitiveInfuser.class);
        associations.assignable(Character.class, CharacterInfuser.class);
        associations.assignable(Short.class, PrimitiveInfuser.class);
        associations.assignable(Integer.class, PrimitiveInfuser.class);
        associations.assignable(Long.class, PrimitiveInfuser.class);
        associations.assignable(Float.class, PrimitiveInfuser.class);
        associations.assignable(Double.class, PrimitiveInfuser.class);
        associations.assignable(String.class, StringInfuser.class);
        associations.assignable(Object.class, StringConstructorInfuser.class);
    }
    
    public void setInfuser(Class<?> type, Class<? extends ObjectInfuser> infuser) {
        infusers.clear();
        associations.assignable(type, infuser);
    }
    
    public ObjectInfuser getInfuser(Class<?> type) {
        ObjectInfuser infuser = infusers.get(type);
        if (infuser == null) {
            Class<? extends ObjectInfuser> infuserClass = associations.get(Primitives.box(type));
            try {
                infuser = reflective.getConstructor(infuserClass, Class.class).newInstance(type);
            } catch (ReflectiveException e) {
                throw new InfusionException(0, e, infuserClass, type);
            }
            infusers.put(type, infuser);
        }
        return infuser;
    }

    public Object infuse(Class<?> type, String string) {
        return getInfuser(type).infuse(string);
    }
}
