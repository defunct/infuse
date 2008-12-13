package com.goodworkalan.dspl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
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
            bean = properties[i].get(bean, null);
        }

        if (bean != null)
        {
            return properties[properties.length - 1].get(bean, null);
        }
        
        throw new PropertyPath.Error();
    }
    
    public Type typeOf(Object bean, Factory factory) throws PropertyPath.Error
    {
        for (int i = 0; bean != null && i < properties.length - 1; i++)
        {
            bean = properties[i].get(bean, factory);
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
    public void set(Object bean, Object value, Factory factory) throws PropertyPath.Error
    {
        for (int i = 0; bean != null && i < properties.length - 1; i++)
        {
            bean = properties[i].get(bean, factory);
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
    static int eatWhite(String part, int i)
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
    
    interface Index
    {
        public Type typeOf(Type type) throws PropertyPath.Error;
        
        public Object get(Type type, Object object, Factory factory) throws PropertyPath.Error;
    }
    
    final static class ListIndex implements Index
    {
        private final int index;
        
        public ListIndex(int index)
        {
            this.index = index;
        }

        public Type typeOf(Type type) throws PropertyPath.Error
        {
            if (type instanceof ParameterizedType)
            {
                ParameterizedType parameterized = (ParameterizedType) type;
                if (((Class<?>) parameterized.getRawType()).isAssignableFrom(List.class))
                {
                    return parameterized.getActualTypeArguments()[0];
                }
            }
            return null;
        }
        
        @SuppressWarnings("unchecked")
        private List<Object> toList(Object object)
        {
            return (List) object;
        }

        public Object get(Type type, Object object, Factory factory) throws PropertyPath.Error
        {
            Object got = null;
            try
            {
                List<Object> list = toList(object);
                if (index < list.size())
                {
                    got = list.get(index);
                }
                if (got == null && factory != null)
                {
                    if (type instanceof Class)
                    {
                        got = factory.create((Class<?>) type);
                    }
                    else
                    {
                        got = factory.create((Class<?>) ((ParameterizedType) type).getRawType());
                    }
                    list.add(index, got);
                }
            }
            catch (Exception e)
            {
                throw new PropertyPath.Error(e);
            }
            return got;
        }
    }
    
    final static class Factory
    {
        public Object create(Type type) throws PropertyPath.Error
        {
            Object created = null;
            if (type instanceof ParameterizedType)
            {
                created = create((Class<?>) ((ParameterizedType) type).getRawType());
            }
            else if (type instanceof Class)
            {
                created = create((Class<?>) type);
            }
            return created;
        }
        
        public Object create(Class<?> cls) throws PropertyPath.Error
        {
            if (!cls.isInterface())
            {
                try
                {
                    return cls.newInstance();
                }
                catch (Exception e)
                {
                    throw new PropertyPath.Error(e);
                }
            }
            else if (SortedMap.class.isAssignableFrom(cls))
            {
                return new TreeMap<Object, Object>();
            }
            else if (Map.class.isAssignableFrom(cls))
            {
                return new HashMap<Object, Object>();
            }
            else if (List.class.isAssignableFrom(cls))
            {
                return new ArrayList<Object>();
            }
            throw new UnsupportedOperationException();
        }
    }

    final static class MapIndex implements Index
    {
        private final String index;
        
        public MapIndex(String index)
        {
            this.index = index;
        }

        public Type typeOf(Type type) throws Error
        {
            if (type instanceof ParameterizedType)
            {
                ParameterizedType parameterized = (ParameterizedType) type;
                if (((Class<?>) parameterized.getRawType()).isAssignableFrom(Map.class))
                {
                    return parameterized.getActualTypeArguments()[1];
                }
            }
            return null;
        }
        
        public Object get(Type type, Object object, Factory factory) throws PropertyPath.Error
        {
            index.charAt(0);
            return null;
        }
    }

    final static class Property
    {
        private final String name;
        
        private final Index[] indexes;
        
        public Property(String name, Index...indexes)
        {
            this.name = name;
            this.indexes = indexes;
        }
        
        public String getName()
        {
            return name;
        }
        
        public Object get(Object bean, Factory factory) throws PropertyPath.Error
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
                    if (object == null && factory != null && descriptor.getWriteMethod() != null)
                    {
                        object = factory.create(typeOf(bean));
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
                    Type type = typeOf(bean);
                    for (Index index : indexes)
                    {
                        if (object == null)
                        {
                            break;
                        }
                        type = index.typeOf(type);
                        object = index.get(type, object, factory);
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
    


    Property newProperty(String part) throws PropertyPath.Error
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
        while (i != part.length())
        {
            List<Index> indices = new ArrayList<Index>();
            try
            {
                i = newIndex(part, i, indices);
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
        
        return new Property(identifier);
    }
    
    static int newIndex(String part, int i, List<Index> indexes)
        throws PropertyPath.Error
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

            indexes.add( new MapIndex(newKey.toString()));
            
            return eatWhite(part, i);
        }

        int index = 0;
        do
        {
            int n = Integer.parseInt(new Character(part.charAt(i++)).toString(), 10);
            index = index * 10 + n;
        }
        while ("] ".indexOf(part.charAt(i)) == -1);
        
        i = eatWhite(part, i);
        
        if (part.charAt(i) != ']')
        {
            throw new PropertyPath.Error();
        }
        
        indexes.add(new ListIndex(index));
        
        return eatWhite(part, i + 1);
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
