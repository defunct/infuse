package com.goodworkalan.dspl;

import static com.goodworkalan.dspl.Objects.toClass;
import static com.goodworkalan.dspl.Objects.toMap;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

final class Property
{
    final String name;
    
    final Index[] indexes;
    
    public Property(String name, Index...indexes)
    {
        this.name = name;
        this.indexes = indexes;
    }
    
    public String methodName()
    {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public Map<Class<?>, String> readMethodNames()
    {
        Map<Class<?>, String> map = new HashMap<Class<?>, String>();
        map.put(Boolean.class, "is" + methodName());
        map.put(boolean.class, "is" + methodName());
        map.put(Object.class, "get" + methodName());
        return map;
    }

    public Set<Method> readMethods(Object bean, int indexesLength)
    {
        Map<Class<?>, String> readerNames = readMethodNames();
        Set<Method> readers = new HashSet<Method>();
        for (Method method : bean.getClass().getMethods())
        {
            if (readerNames.values().contains(method.getName()))
            {
                for (Class<?> result : readerNames.keySet())
                {
                    if ((result.equals(Object.class) || result.isAssignableFrom(toClass(method.getGenericReturnType())))
                            && readerNames.get(result).equals(method.getName()))
                    {
                        readers.add(method);
                    }
                }
            }
        }
        Map<Integer, Method> priority = new TreeMap<Integer, Method>(Collections.reverseOrder());
        METHODS: for (Method method : readers)
        {
            Type[] types = method.getGenericParameterTypes();
            if (types.length <= indexesLength)
            {
                for (int i = 0; i < types.length; i++)
                {
                    Class<?> cls = toClass(types[i]);
                    if (!indexes[i].indexedBy(cls))
                    {
                        continue METHODS;
                    }
                }
                priority.put(types.length, method);
            }
        }
        return new LinkedHashSet<Method>(priority.values());
    }

    boolean create(Object bean, Type type, int indexLength, ObjectFactory factory) throws PathException
    {
        Method method = explicitSet(bean, type, indexLength);
        if (method != null)
        {
            Object value;
            try
            {
                value = factory.create(type);
            }
            catch (FactoryException e)
            {
                throw new PathException(130, e);
            }
            if (value == null)
            {
                throw new PathException(113);
            }
            set(method, bean, value);
            return true;
        }
        return false;
    }
    
    public Object get(Object bean, ObjectFactory factory) throws PathException
    {
        return get(bean, indexes.length, factory);
    }

    public Object get(Object bean, int indexesLength, ObjectFactory factory) throws PathException
    {
        if (name.equals("this") || (bean instanceof Map))
        {
            Object object = null;
            if (name.equals("this"))
            {
                object = bean;
            }
            else
            {
                Map<Object, Object> map = toMap(bean);
                object = map.get(name);
                if (object == null)
                {
                    object = factory.newBean();
                    map.put(name, object);
                }
            }
            for (int i = 0; object != null && i < indexes.length - 1; i++)
            {
                // TODO Change first property of get.
                Object value = indexes[i].get(null, object, null);
                if (value == null && factory != null)
                {
                    value = new ObjectMap();
                    indexes[i].set(null, object, value);
                }
                object = value;
            }
            if (indexes.length == 0)
            {
                return object;
            }
            Object value = indexes[indexes.length - 1].get(null, object, null);
            if (value == null)
            {
                value = factory.newBean();
                indexes[indexes.length - 1].set(ObjectMap.class.getGenericSuperclass(), object, value);
            }
            return value;
        }

        Set<Method> readers = readMethods(bean, indexesLength);
        Object object = null;
        Iterator<Method> methods = readers.iterator();
        while (object == null && methods.hasNext())
        {
            Method method = methods.next();
            Object[] args = new Object[method.getParameterTypes().length];
            for (int i = 0; i < args.length; i++)
            {
                args[i] = indexes[i].getIndex(false);
            }
            try
            {
                object = method.invoke(bean, args);
            }
            catch (Exception e)
            {
                throw new PathException(114, e);
            }
            if (object == null && factory != null && create(bean, method.getGenericReturnType(), args.length, factory))
            {
                object = get(bean, args.length, factory);
            }
            Type type = method.getGenericReturnType();
            for (int i = args.length; object != null && i < indexesLength; i++)
            {
                try
                {
                    object = indexes[i].get(type, object, factory);
                    type = indexes[i].typeOf(type);
                }
                catch (PathException e)
                {
                    e.add(Messages.stringEscape(toString()))
                     .add(Messages.stringEscape(toString(i + 1)));
                    throw e;
                }
            }
        }
        return object;
    }
    
    public Type typeOf(Object bean) throws PathException
    {
        return typeOf(bean, indexes.length);
    }

    public Type typeOf(Object bean, int indexesLength) throws PathException
    {
        Set<Method> readers = readMethods(bean, indexesLength);
        Type type = null;
        Iterator<Method> methods = readers.iterator();
        while (type == null && methods.hasNext())
        {
            Method method = methods.next();
            int args = method.getParameterTypes().length;
            type = method.getGenericReturnType();
            for (int i = args; i < indexesLength; i++)
            {
                type = indexes[i].typeOf(type);
            }
        }
        return type;
    }
    
    public void set(Object bean, Object value, ObjectFactory factory) throws PathException
    {
        try
        {
            if (bean instanceof Map)
            {
                if (indexes.length == 0)
                {
                    toMap(bean).put(name, value);
                }
                else
                {
                    Object container = null;
                    Object object = toMap(bean).get(name);
                    for (int i = 0; i < indexes.length; i++)
                    {
                        if (object == null)
                        {
                            try
                            {
                                object = factory.create(indexes[i].getRawType());
                            }
                            catch (FactoryException e)
                            {
                                throw new PathException(131, e);
                            }
                            if (i == 0)
                            {
                                toMap(bean).put(name, object);
                            }
                            else
                            {
                                indexes[i - 1].set(indexes[i - 1].getRawType(), container, value);
                            }
                        }
                        container = object;
                    }
                    indexes[indexes.length - 1].set(container.getClass().getGenericSuperclass(), container, value);
                }
            }
            else
            {
                Method method = explicitSet(bean, value == null ? null : value.getClass(), indexes.length);
                if (method == null && indexes.length != 0)
                {
                    Object object = get(bean, indexes.length - 1, factory);
                    if (object != null)
                    {
                        Type type = typeOf(bean, indexes.length - 1);
                        indexes[indexes.length - 1].set(type, object, value);
                    }
                }
                else if (method == null)
                {
                    throw new PathException(115);
                }
                else
                {
                    set(method, bean, value);
                }
            }
        }
        catch (PathException e)
        {
            e.add(Messages.stringEscape(toString()))
             .add(bean.getClass().getName())
             .add(value == null ? value : value.getClass().getName());
            throw e;
        }
    }

    @Override
    public String toString()
    {
        return toString(indexes.length);
    }
    
    public String toString(int indexCount)
    {
        StringBuilder newString = new StringBuilder();
        newString.append(name);
        for (int i = 0; i < indexCount; i++)
        {
            newString.append(indexes[i].toString());
        }
        return newString.toString();
    }

    void set(Method method, Object bean, Object value) throws PathException
    {
        Object[] args = new Object[method.getParameterTypes().length];
        for (int i = 0; i < args.length - 1; i++)
        {
            args[i] = indexes[i].getIndex(false);
        }
        args[args.length - 1] = value;
        try
        {
            method.invoke(bean, args);
        }
        catch (Exception e)
        {
            throw new PathException(116, e);
        }
    }
    
    Method explicitSet(Object bean, Type type, int indexLength) throws PathException
    {
        Class<?> cls = toClass(type);
        Set<Method> writers = new HashSet<Method>();
        String methodName = "set" + methodName();
        METHOD: for (Method method : bean.getClass().getMethods())
        {
            Type[] types = method.getGenericParameterTypes();
            if (method.getName().equals(methodName)
                && types.length == indexLength + 1
                && (cls == null || isAssignableFrom(toClass(types[indexLength]), cls)))
            {
                for (int i = 0; i < indexLength; i++)
                {
                    if (!indexes[i].indexedBy(toClass(types[i])))
                    {
                        continue METHOD;
                    }
                }
                writers.add(method);
            }
        }
        if (writers.size() == 1)
        {
            return writers.iterator().next();
        }
        return null;
    }
    
    static final boolean isAssignableFrom(Class<?> to, Class<?> from)
    {
        if (to.isPrimitive())
        {
            if (long.class.isAssignableFrom(to))
            {
                return Long.class.isAssignableFrom(from);
            }
            else if (int.class.isAssignableFrom(to))
            {
                return Integer.class.isAssignableFrom(from);
            }
            else if (short.class.isAssignableFrom(to))
            {
                return Short.class.isAssignableFrom(from);
            }
            else if (char.class.isAssignableFrom(to))
            {
                return Character.class.isAssignableFrom(from);
            }
            else if (byte.class.isAssignableFrom(to))
            {
                return Byte.class.isAssignableFrom(from);
            }
            else if (boolean.class.isAssignableFrom(to))
            {
                return Boolean.class.isAssignableFrom(from);
            }
            else if (float.class.isAssignableFrom(to))
            {
                return Float.class.isAssignableFrom(from);
            }
            else // if (double.class.isAssignableFrom(to))
            {
                return Double.class.isAssignableFrom(from);
            }
        }
        return from.isAssignableFrom(to);
    }
}