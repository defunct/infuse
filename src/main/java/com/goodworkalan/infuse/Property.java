package com.goodworkalan.infuse;

import static com.goodworkalan.infuse.Objects.toClass;
import static com.goodworkalan.infuse.Objects.toMap;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

// TODO Document.
final class Property
{
    // TODO Document.
    final String name;
    
    // TODO Document.
    private final List<Index> indexes = new ArrayList<Index>();
    
    // TODO Document.
    public Property(String name, Index...indexes)
    {
        this.name = name;
        this.indexes.addAll(Arrays.asList(indexes));
    }
    
    // TODO Document.
    public String methodName()
    {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    // TODO Document.
    public Map<Class<?>, String> readMethodNames()
    {
        Map<Class<?>, String> map = new HashMap<Class<?>, String>();
        map.put(Boolean.class, "is" + methodName());
        map.put(boolean.class, "is" + methodName());
        map.put(Object.class, "get" + methodName());
        return map;
    }

    // TODO Document.
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
                    if (!indexes.get(i).indexedBy(cls))
                    {
                        continue METHODS;
                    }
                }
                priority.put(types.length, method);
            }
        }
        return new LinkedHashSet<Method>(priority.values());
    }

    // TODO Document.
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
    
    // TODO Document.
    public Object get(Object bean, ObjectFactory factory) throws PathException
    {
        return get(bean, indexes.size(), factory);
    }

    // TODO Document.
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
            for (int i = 0; object != null && i < indexes.size() - 1; i++)
            {
                // TODO Change first property of get.
                Object value = indexes.get(i).get(null, object, null);
                if (value == null && factory != null)
                {
                    value = new ObjectMap();
                    indexes.get(i).set(null, object, value);
                }
                object = value;
            }
            if (indexes.size() == 0)
            {
                return object;
            }
            Object value = indexes.get(indexes.size() - 1).get(null, object, null);
            if (value == null)
            {
                value = factory.newBean();
                indexes.get(indexes.size() - 1).set(ObjectMap.class.getGenericSuperclass(), object, value);
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
                args[i] = indexes.get(i).getIndex(false);
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
                    object = indexes.get(i).get(type, object, factory);
                    type = indexes.get(i).typeOf(type);
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
    
    // TODO Document.
    public Type typeOf(Object bean) throws PathException
    {
        return typeOf(bean, indexes.size());
    }

    // TODO Document.
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
                type = indexes.get(i).typeOf(type);
            }
        }
        return type;
    }
    
    // TODO Document.
    public void set(Object bean, Object value, ObjectFactory factory) throws PathException
    {
        try
        {
            if (bean instanceof Map)
            {
                if (indexes.size() == 0)
                {
                    toMap(bean).put(name, value);
                }
                else
                {
                    Object container = null;
                    Object object = toMap(bean).get(name);
                    for (int i = 0; i < indexes.size(); i++)
                    {
                        if (object == null)
                        {
                            try
                            {
                                object = factory.create(indexes.get(i).getRawType());
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
                                indexes.get(i - 1).set(indexes.get(i - 1).getRawType(), container, value);
                            }
                        }
                        container = object;
                    }
                    indexes.get(indexes.size() - 1).set(container.getClass().getGenericSuperclass(), container, value);
                }
            }
            else
            {
                Method method = explicitSet(bean, value == null ? null : value.getClass(), indexes.size());
                if (method == null && indexes.size() != 0)
                {
                    Object object = get(bean, indexes.size() - 1, factory);
                    if (object != null)
                    {
                        Type type = typeOf(bean, indexes.size() - 1);
                        indexes.get(indexes.size() - 1).set(type, object, value);
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

    // TODO Document.
    public void glob(Object bean, LinkedList<PropertyPath> glob) throws PathException
    {
        ListIterator<PropertyPath> iterator = glob.listIterator();
        while (iterator.hasNext())
        {
            PropertyPath path = iterator.next();
            Property property = new Property(name);
            path.addProperty(property);
            if (path.get(bean) == null)
            {
                iterator.remove();
            }
        }
        for (Index index : indexes)
        {
            int count = glob.size();
            while (count-- != 0)
            {
                PropertyPath path = glob.removeFirst();
                index.glob(bean, path, glob);
            }
        }
    }
    
    // TODO Document.
    public void addIndex(Index index)
    {
        indexes.add(index);
    }
    
    // TODO Document.
    public Property duplicate()
    {
        Property duplicate = new Property(name);
        for (Index index : indexes)
        {
            duplicate.addIndex(index.duplicate());
        }
        return duplicate;
    }
    
    // TODO Document.
    public void toList(List<String> list, boolean escape)
    {
        list.add(name);
        for (Index index : indexes)
        {
            list.add(index.getIndex(escape).toString());
        }
    }
    
    // TODO Document.
    @Override
    public String toString()
    {
        return toString(indexes.size());
    }
    
    // TODO Document.
    public String toString(int indexCount)
    {
        StringBuilder newString = new StringBuilder();
        newString.append(name);
        for (int i = 0; i < indexCount; i++)
        {
            newString.append(indexes.get(i).toString());
        }
        return newString.toString();
    }

    // TODO Document.
    void set(Method method, Object bean, Object value) throws PathException
    {
        Object[] args = new Object[method.getParameterTypes().length];
        for (int i = 0; i < args.length - 1; i++)
        {
            args[i] = indexes.get(i).getIndex(false);
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
    
    // TODO Document.
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
                && (cls == null || Objects.isAssignableFrom(toClass(types[indexLength]), cls)))
            {
                for (int i = 0; i < indexLength; i++)
                {
                    if (!indexes.get(i).indexedBy(toClass(types[i])))
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
}