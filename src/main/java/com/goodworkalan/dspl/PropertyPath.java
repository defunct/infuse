package com.goodworkalan.dspl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyVetoException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * <p>
 * A dirt simple bean property path navigator that gets and sets bean properties
 * within object graphs.
 * </p>
 * <p>
 * Is is not entirely possible to check against an potential property path using
 * a class. The <code>PropertyPath</code> class can navigate maps, lists, and
 * arrays. Although type information is available through generic types, it may
 * be the case that a generic container is declared with an Object as type
 * property. We won't know the real properties until we navigate an actual
 * object graph.
 * </p>
 * 
 * @author Alan Gutierrez
 */
public class PropertyPath
{
    /** The bean path. */
    private final Property[] properties;
    
    /**
     * Create a bean path from the specified string. The bean path will be
     * checked for syntax. The syntax check will not check the path against a
     * particular bean path, merely that the syntax does not include invalid
     * characters.
     * 
     * @param path
     *            The bean path.
     */
    public PropertyPath(String path) throws PropertyPath.Error
    {
        String[] parts = path.split("\\.");
        Property[] properties = new Property[parts.length];
        for (int i = 0; i < parts.length; i++)
        {
            properties[i] = newProperty(parts[i]);
        }
        this.properties = properties;
    }

    /**
     * Get the bean property matching this bean path.
     * 
     * @param bean
     *            The root bean of an object graph.
     * @throws PropertyPath.Error
     *             If the path does not exist or if an error occurs in
     *             reflection.
     */
    public Object get(Object bean) throws PropertyPath.Error
    {
        for (int i = 0; bean != null && i < properties.length - 1; i++)
        {
            bean = properties[i].get(bean, false);
        }

        if (bean != null)
        {
            return properties[properties.length - 1].get(bean, false);
        }
        
        throw new PropertyPath.Error();
    }
    
    public Type typeOf(Object bean) throws PropertyPath.Error
    {
        for (int i = 0; bean != null && i < properties.length - 1; i++)
        {
            bean = properties[i].get(bean, true);
        }

        if (bean != null)
        {
            return properties[properties.length - 1].typeOf(bean);
        }
        
        throw new PropertyPath.Error();
    }

    /**
     * Set the bean property matching this bean path to the specified value.
     * 
     * @param bean
     *            The root bean of an object graph.
     * @param value
     *            The value to set.
     * @throws PropertyPath.Error
     *             If the path does not exist, if the value is of the incorrect
     *             type, or if an error occurs in reflection.
     */
    public void set(Object bean, Object value) throws PropertyPath.Error
    {
        for (int i = 0; bean != null && i < properties.length - 1; i++)
        {
            bean = properties[i].get(bean, true);
        }
        if (bean != null)
        {
            properties[properties.length - 1].set(bean, value);
        }
    }
    
    /**
     * Skip whitespace between property names, array brackets, and property
     * indices.
     * 
     * @param part
     *            A part of a property path.
     * @param i
     *            The current index.
     * @return The index of the first non-whitespace character, current index
     *         included.
     */
    int eatWhite(String part, int i)
    {
        for (; i < part.length() && Character.isWhitespace(part.charAt(i)); i++)
        {
        }
        return i;
    }

    /**
     * Read a Java identifier from the start of a part of a property path.
     * 
     * @param part
     *            A part of a property path.
     * @param identifier
     *            A string builder to collect the identifier.
     * @return The index of the first character that is not a part of a Java
     *         identifier.
     */
    static int getIdentifier(String part, StringBuilder identifier)
    {
        identifier.append(part.charAt(0));
        int i = 1;
        for (; i < part.length() && Character.isJavaIdentifierPart(part.charAt(i)); i++)
        {
            identifier.append(part.charAt(i));
        }
        return i;
    }
    
    interface Property
    {
        public Object get(Object bean, boolean create) throws PropertyPath.Error;
        
        public Type typeOf(Object bean) throws PropertyPath.Error;
        
        public void set(Object bean, Object value) throws PropertyPath.Error;
    }
    
    interface Index
    {
        public Type typeOf(Type type) throws PropertyPath.Error;
    }
    
    final static class ListIndex implements Index
    {
        public Type typeOf(Type type) throws PropertyPath.Error
        {
            if (type instanceof ParameterizedType)
            {
                ParameterizedType parameterized = (ParameterizedType) type;
                if (parameterized.getRawType() instanceof Class)
                {
                    if (((Class<?>) parameterized.getRawType()).isAssignableFrom(List.class))
                    {
                        return parameterized.getActualTypeArguments()[0];
                    }
                }
            }
            return null;
        }
    }

    final static class MapIndex implements Index
    {
        public Type typeOf(Type type) throws Error
        {
            if (type instanceof ParameterizedType)
            {
                ParameterizedType parameterized = (ParameterizedType) type;
                if (parameterized.getRawType() instanceof Class)
                {
                    if (((Class<?>) parameterized.getRawType()).isAssignableFrom(Map.class))
                    {
                        return parameterized.getActualTypeArguments()[1];
                    }
                }
            }
            return null;
        }
    }

    final static class BeanProperty implements Property
    {
        private final String name;
        
        public BeanProperty(String name)
        {
            this.name = name;
        }
        
        public String getName()
        {
            return name;
        }
        
        public Object get(Object bean, boolean create) throws PropertyPath.Error
        {
            BeanInfo beanInfo;
            try
            {
                beanInfo = Introspector.getBeanInfo(bean.getClass());
            }
            catch (IntrospectionException e)
            {
                throw new PropertyPath.Error(e);
            }
            for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors())
            {
                if (descriptor.getName().equals(name))
                {
                    if (descriptor.getReadMethod() == null)
                    {
                        throw new Error();
                    }
                    Object object = null;
                    try
                    {
                        object = descriptor.getReadMethod().invoke(bean);
                    }
                    catch (Exception e)
                    {
                        throw new PropertyPath.Error(e);
                    }
                    if (object == null && create && descriptor.getWriteMethod() != null)
                    {
                        Type type = typeOf(bean);
                        if (type instanceof ParameterizedType)
                        {
                            ParameterizedType parameterized = (ParameterizedType) type;
                            Type rawType = parameterized.getRawType();
                            if (rawType instanceof Class<?>)
                            {
                                Class<?> cls = (Class<?>) rawType;
                                if (!cls.isInterface())
                                {
                                    if (cls.isArray())
                                    {
                                        object = Array.newInstance(cls.getComponentType(), 0);
                                    }
                                    else
                                    {
                                        try
                                        {
                                            object = cls.newInstance();
                                        }
                                        catch (Exception e)
                                        {
                                            throw new PropertyPath.Error(e);
                                        }
                                    }
                                }
                                else if (SortedMap.class.isAssignableFrom(cls))
                                {
                                    if (parameterized.getActualTypeArguments()[0] instanceof Class<?>
                                    && parameterized.getActualTypeArguments()[1] instanceof Class<?>)
                                {
                                    object = new TreeMap<Object, Object>(); 
                                }
                                }
                                else if (Map.class.isAssignableFrom(cls))
                                {
                                    object = new HashMap<Object, Object>(); 
                                }
                                else if (List.class.isAssignableFrom(cls))
                                {
                                    object = new ArrayList<Object>();
                                }
                            }
                        }
                        else if (type instanceof Class<?>)
                        {
                            try
                            {
                                object = descriptor.getPropertyType().newInstance();
                            }
                            catch (Exception e)
                            {
                                throw new PropertyPath.Error(e);
                            }
                        }
                        if (object != null)
                        {
                            try
                            {
                                descriptor.getWriteMethod().invoke(bean, object);
                            }
                            catch (Exception e)
                            {
                                throw new PropertyPath.Error(e);
                            }
                        }
                    }
                    return object;
                }
            }
            throw new PropertyPath.Error();
        }
        
        public Type typeOf(Object bean) throws Error
        {
            BeanInfo beanInfo;
            try
            {
                beanInfo = Introspector.getBeanInfo(bean.getClass());
            }
            catch (IntrospectionException e)
            {
                throw new PropertyPath.Error(e);
            }
            for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors())
            {
                if (descriptor.getName().equals(name))
                {
                    if (descriptor.getReadMethod() != null)
                    {
                        return descriptor.getReadMethod().getGenericReturnType();
                    }
                    else if (descriptor.getWriteMethod() != null)
                    {
                        return descriptor.getWriteMethod().getGenericParameterTypes()[0];
                    }
                    throw new PropertyPath.Error();
                }
            }
            throw new PropertyPath.Error();
        }
        
        public void set(Object bean, Object value) throws Error
        {
            BeanInfo beanInfo;
            try
            {
                beanInfo = Introspector.getBeanInfo(bean.getClass());
            }
            catch (IntrospectionException e)
            {
                throw new PropertyPath.Error(e);
            }
            for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors())
            {
                if (descriptor.getName().equals(name))
                {
                    if (descriptor.getWriteMethod() == null)
                    {
                        throw new PropertyPath.Error();
                    }
                    try
                    {
                        descriptor.getWriteMethod().invoke(bean, value);
                        return;
                    }
                    catch (Exception e)
                    {
                        throw new PropertyPath.Error(e);
                    }
                }
            }
            throw new PropertyPath.Error();
        }
    }
    
    public final static class MapProperty implements Property
    {
        private final Property property;
        
        private final String key;
        
        public MapProperty(String name, String key)
        {
            this.property = new BeanProperty(name);
            this.key = key;
        }
        
        @SuppressWarnings("unchecked")
        public Object get(Object bean, boolean create) throws PropertyPath.Error
        {
            Map map = (Map) property.get(bean, create);
            if (map != null)
            {
                Object object = map.get(key);
                if (object == null)
                {
                    Type type = typeOf(bean);
                    if (type instanceof Class)
                    {
                        try
                        {
                            object = ((Class<?>) type).newInstance();
                        }
                        catch (Exception e)
                        {
                            throw new PropertyPath.Error(e);
                        }
                        map.put(key, object);
                    }
                }
                return object;
            }
            return null;
        }
        
        public Type typeOf(Object bean) throws Error
        {
            Type type = property.typeOf(bean);
            if (type != null)
            {
                ParameterizedType pt = (ParameterizedType) type;
                return pt.getActualTypeArguments()[1];
            }
            return null;
        }
        
        @SuppressWarnings("unchecked")
        public void set(Object bean, Object value) throws PropertyPath.Error
        {
            try
            {
                Map map = (Map) property.get(bean, true); 
                if (map != null)
                {
                    map.put(key, value);
                }
            }
            catch (ClassCastException e)
            {
                throw new PropertyPath.Error(e);
            }
            
        }
    }

    final static class ListProperty implements Property
    {
        private final Property property;
        
        private final int index;
        
        public ListProperty(String name, int index)
        {
            this.property = new BeanProperty(name);
            this.index = index;
        }
        
        @SuppressWarnings("unchecked")
        public Object get(Object bean, boolean create) throws PropertyPath.Error
        {
            Object list = property.get(bean, create);
            if (list != null)
            {
                if (list.getClass().isArray())
                {
                    Object object = null;
                    Object[] array = (Object[]) list;
                    if (index < array.length)
                    {
                        object = array[index];
                    }
                    if (object == null && create)
                    {
                        try
                        {
                            object = list.getClass().getComponentType().newInstance();
                        }
                        catch (Exception e)
                        {
                            throw new PropertyPath.Error(e);
                        }
                        if (index >= array.length)
                        {
                            array = (Object[]) Array.newInstance(list.getClass().getComponentType(), index + 1);
                        }
                        array[index] = object;
                    }
                    return object;
                }
                else if (list instanceof List)
                {
                    Object object = null;
                    List snert = (List) list;
                    if (index < snert.size())
                    {
                        object = snert.get(index);
                    }
                    if (object == null && create)
                    {
                        Type type = typeOf(bean);
                        if (type instanceof Class)
                        {
                            try
                            {
                                object = ((Class<?>) type).newInstance();
                            }
                            catch (Exception e)
                            {
                                throw new PropertyPath.Error(e);
                            }
                            snert.add(index, object);
                        }
                    }
                    return object;
                }
                throw new PropertyPath.Error();
            }
            return null;
        }
        
        public Type typeOf(Object bean) throws Error
        {
            Object list = property.get(bean, true);
            if (list != null)
            {
                if (list.getClass().isArray())
                {
                    list.getClass().getComponentType();
                }
                else if (list instanceof List)
                {
                    Method method;
                    try
                    {
                        method = list.getClass().getMethod("get", Integer.class);
                    }
                    catch (Exception e)
                    {
                        throw new PropertyPath.Error(e);
                    }
                    return method.getGenericReturnType();
                }
            }
            return null;
        }
        
        @SuppressWarnings("unchecked")
        public void set(Object bean, Object value) throws PropertyPath.Error
        {
            Object list = property.get(value, true);
            if (list != null)
            {
                if (list.getClass().isArray())
                {
                    Array.set(list, index, value);
                }
                else if (list instanceof List)
                {
                    ((List) list).add(index, value);
                }
                else
                {
                    throw new PropertyPath.Error();
                }
            }
        }
    }
    
    public Property newProperty(String part) throws PropertyPath.Error
    {
        part = part.trim();
        if (part.length() == 0 || !Character.isJavaIdentifierStart(part.charAt(0)))
        {
            throw new PropertyPath.Error();
        }

        // Read the Java bean identifier.
        StringBuilder newIdentifier = new StringBuilder();
        int i = getIdentifier(part, newIdentifier);
        String identifier = newIdentifier.toString();
        
        // Skip an whitespace.
        i = eatWhite(part, i);
        
        // Check for an optional indexed parameter.
        if (i != part.length())
        {
            try
            {
                return newIndexProperty(identifier, part, i);
            }
            catch (StringIndexOutOfBoundsException e)
            {
                throw new PropertyPath.Error(e);
            }
            catch (NumberFormatException e)
            {
                throw new PropertyPath.Error(e);
            }
        }
        
        return new BeanProperty(identifier);
    }
    
    public Property newIndexProperty(String name, String part, int i) throws PropertyPath.Error
    {
        if (part.charAt(i++) != '[')
        {
            throw new PropertyPath.Error();
        }
        i = eatWhite(part, i);
        if ("\"'".indexOf(part.charAt(i)) != -1)
        {
            StringBuilder newKey = new StringBuilder();
            char quote = part.charAt(i++);
            KEY: for (;;)
            {
                char ch = part.charAt(i++);
                switch (ch)
                {
                case 0:
                    throw new PropertyPath.Error();
                case '\'':
                case '"':
                    if (ch == quote)
                    {
                        break KEY;
                    }
                    throw new PropertyPath.Error();
                case '\\':
                    ch = part.charAt(i++);
                    switch (ch)
                    {
                    case 'b':
                        newKey.append('\b');
                        break;
                    case 't':
                        newKey.append('\t');
                        break;
                    case 'n':
                        newKey.append('\n');
                        break;
                    case 'f':
                        newKey.append('\f');
                        break;
                    case 'r':
                        newKey.append('\r');
                        break;
                    case 'u':
                        newKey.append((char) Integer.parseInt(part.substring(i, i += 4), 16));
                        break;
                    case 'x':
                        newKey.append((char) Integer.parseInt(part.substring(i, i += 2), 16));
                        break;
                    case '\'':
                        newKey.append('\'');
                        break;
                    case '"':
                        newKey.append('"');
                        break;
                    default:
                        throw new PropertyPath.Error();
                    }
                default:
                    newKey.append(ch);
                }
            }
            i = eatWhite(part, i);
            if (part.charAt(i++) != ']')
            {
                throw new PropertyPath.Error();
            }
            i = eatWhite(part, i);
            if (i != part.length())
            {
                throw new PropertyPath.Error();
            }
            return new MapProperty(name, newKey.toString());
        }
        else
        {
            int index = 0;
            do
            {
                int n = Integer.parseInt(new Character(part.charAt(i++)).toString(), 10);
                index = index * 10 + n;
            }
            while ("] ".indexOf(part.charAt(i)) == -1);
            return new ListProperty(name, index);
        }
    }

    public final static class Error extends Exception
    {
        private static final long serialVersionUID = 1L;
        
        public Error()
        {
        }
        
        public Error(Throwable cause)
        {
            super(cause);
        }
    }
}
