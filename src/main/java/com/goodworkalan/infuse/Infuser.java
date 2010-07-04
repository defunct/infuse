package com.goodworkalan.infuse;

import static com.goodworkalan.infuse.InfusionException.$;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.goodworkalan.utility.ClassAssociation;
import com.goodworkalan.utility.Primitives;

/**
 * A collection that associates classes with strategies for creating instances
 * of the classes from strings. The default constructor for <code>Infuser</code>
 * creates associations for all of the primitive types, for the
 * <code>String</code> class, and uses the {@link StringConstructorInfuser} as
 * the default infuser strategy for unassociated types.
 * 
 * @author Alan Gutierrez
 */
public class Infuser {
    /** The cache of resolved object infusers by type. */
    private final ConcurrentMap<Class<?>, ObjectInfuser> infusers = new ConcurrentHashMap<Class<?>, ObjectInfuser>();

    /** The type to object infuser associations. */
    private final ClassAssociation<Class<? extends ObjectInfuser>> associations;

    /**
     * Create a default infuser with default strategies for primitive Java
     * types, <code>String</code>, with the single string constructor strategy
     * as the default for unmapped types.
     */
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

    /**
     * Create an <code>Infuser</code> that is a copy of the given
     * <code>Infuser</code>. The new infuser will reference none of the objects
     * referenced by the given <code>Infuser</code>.
     * 
     * @param infuser
     *            The infuser.
     */
    public Infuser(Infuser infuser) {
        associations = new ClassAssociation<Class<? extends ObjectInfuser>>(infuser.associations);
    }

    /**
     * Associate the given <code>Class</code> with the given
     * <code>ObjectInfuser</code>.
     * <p>
     * This method is thread-safe, in that concurrent updates to the collection
     * will not put the collection in an inconsistent state. The construction
     * strategy for <code>Infuser</code> is to create a default instance, then
     * create associations, most likely, as new types are loaded into a running
     * application. This method will create a new association that will be
     * available to any callers after the method completes.
     * <p>
     * You can use this behavior to associate an object infuser by calling a
     * statically referenced instance of <code>Infuser</code> in the static
     * initializer of the class, to that its serialization strategy is
     * registered before an instance of the class can even be created.
     * <p>
     * Once you've associated a serialization strategy, you can associate a
     * different one in the future, but you cannot disassociate the strategy for
     * a type, but there is no need to change this association, since it is
     * really a constitutional property of the type for any givne application of
     * an <code>Infuser</code>. That is, you're not going to change the way you
     * represent an integer halfway through your document, nor are you going to
     * change it from document to document. The JSON specification, for example,
     * is not going to change its representation of integers from one invocation
     * to the next.
     * 
     * @param type
     *            Type type to associate.
     * @param infuser
     *            The object infuser to use for the type.
     */
    public void setInfuser(Class<?> type, Class<? extends ObjectInfuser> infuser) {
        associations.assignable(type, infuser);
        infusers.clear(); // Clear the cache.
    }

    /**
     * Get the object infuser for the given type. The object infuser is chosen
     * by the assignability of the type, so if there is no object infuser
     * explicitly defined for the type, then the <code>ObjectInfuser</code>
     * associated with <code>Object</code> will be used.
     * 
     * @param type
     *            The type.
     * @return The <code>ObjectInfuser</code> associated with the type.
     */
    public ObjectInfuser getInfuser(Class<?> type) {
       ObjectInfuser infuser = infusers.get(type);
        if (infuser == null) {
            Class<? extends ObjectInfuser> infuserClass = associations.get(Primitives.box(type));
            try {
                infuser = infuserClass.getConstructor(Class.class).newInstance(type);
            } catch (Throwable e) {
                throw new InfusionException($(e), Infuser.class, "new.infuser", e, infuserClass, type);
            }
            infusers.put(type, infuser);
        }
        return infuser;
    }

    /**
     * Create an object of the given type using the given string. This method
     * will chose the appropriate <code>ObjectInfuser</code> for the given type
     * and use it to create an <code>Object</code>.
     * 
     * @param type
     *            The type.
     * @param string
     *            The string.
     * @return A new instance of the type created from the string.
     */
    public Object infuse(Class<?> type, String string) {
        return getInfuser(type).infuse(string);
    }
}
