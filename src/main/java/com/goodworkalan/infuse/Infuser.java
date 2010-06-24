package com.goodworkalan.infuse;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.goodworkalan.reflective.Reflective;
import com.goodworkalan.reflective.ReflectiveException;
import com.goodworkalan.utility.ClassAssociation;
import com.goodworkalan.utility.Primitives;

// TODO Document.
public class Infuser {
    // TODO Document.
    private final ConcurrentMap<Class<?>, ObjectInfuser> infusers = new ConcurrentHashMap<Class<?>, ObjectInfuser>();

    // TODO Document.
    private final ClassAssociation<Class<? extends ObjectInfuser>> associations;
    
    // TODO Document.
    public Infuser() {
        associations = new ClassAssociation<Class<? extends ObjectInfuser>>();
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
    
    // TODO Document.
    public Infuser(Infuser infuser) {
        associations = new ClassAssociation<Class<? extends ObjectInfuser>>(infuser.associations);
    }
    
    // TODO Document.
    public void setInfuser(Class<?> type, Class<? extends ObjectInfuser> infuser) {
        infusers.clear(); // XXX What is this for?
        associations.assignable(type, infuser);
    }
    
    // TODO Document.
    public ObjectInfuser getInfuser(final Class<?> type) {
        ObjectInfuser infuser = infusers.get(type);
        if (infuser == null) {
            Class<? extends ObjectInfuser> infuserClass = associations.get(Primitives.box(type));
            try {
                try {
                    infuser = infuserClass.getConstructor(Class.class).newInstance(type);
                } catch (Throwable e) {
                    throw new ReflectiveException(Reflective.encode(e), e);
                }
            } catch (ReflectiveException e) {
                throw new InfusionException(Infuser.class, "new.infuser", e, infuserClass, type);
            }
            infusers.put(type, infuser);
        }
        return infuser;
    }

    // TODO Document.
    public Object infuse(Class<?> type, String string) {
        return getInfuser(type).infuse(string);
    }
}
