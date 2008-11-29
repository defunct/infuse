package com.goodworkalan.dspl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

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
    
    private final boolean nullIsError;
    
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
        this(path, false);
    }

    public PropertyPath(String path, boolean nullIsError) throws PropertyPath.Error
    {
        String[] parts = path.split("\\.");
        Property[] properties = new Property[parts.length];
        for (int i = 0; i < parts.length; i++)
        {
            properties[i] = newProperty(parts[i], nullIsError);
        }
        this.properties = properties;
        this.nullIsError = nullIsError;
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
            bean = properties[i].get(bean);
        }

        if (bean != null)
        {
            return properties[properties.length - 1].get(bean);
        }
        else if (nullIsError)
        {
            throw new PropertyPath.Error();
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
            bean = properties[i].get(bean);
        }
        if (bean != null)
        {
            properties[properties.length - 1].set(bean, value);
        }
        else if (nullIsError)
        {
            throw new PropertyPath.Error();
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
    
    private interface Property
    {
        public Object get(Object bean) throws PropertyPath.Error;
        
        public void set(Object bean, Object value) throws PropertyPath.Error;
    }

    public final static class BeanProperty implements Property
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
        
        public Object get(Object bean) throws PropertyPath.Error
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
                    try
                    {
                        return descriptor.getReadMethod().invoke(bean);
                    }
                    catch (Exception e)
                    {
                        throw new PropertyPath.Error(e);
                    }
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
        
        private final boolean nullIsError;
        
        public MapProperty(String name, String key, boolean nullIsError)
        {
            this.property = new BeanProperty(name);
            this.key = key;
            this.nullIsError = nullIsError;
        }
        
        public Object get(Object bean) throws PropertyPath.Error
        {
            @SuppressWarnings("unchecked")
            Map map = (Map) property.get(bean); 
            if (map != null)
            {
                return map.get(key);
            }
            else if (nullIsError)
            {
                throw new PropertyPath.Error();
            }
            return null;
        }
        
        @SuppressWarnings("unchecked")
        public void set(Object bean, Object value) throws PropertyPath.Error
        {
            try
            {
                Map map = (Map) property.get(bean); 
                if (map != null)
                {
                    map.put(key, value);
                }
                else if (nullIsError)
                {
                    throw new PropertyPath.Error();
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
        
        private final boolean nullIsError;
        
        public ListProperty(String name, int index, boolean nullIsError)
        {
            this.property = new BeanProperty(name);
            this.index = index;
            this.nullIsError = nullIsError;
        }
        
        @SuppressWarnings("unchecked")
        public Object get(Object bean) throws PropertyPath.Error
        {
            Object object = property.get(bean);
            if (object != null)
            {
                if (object.getClass().isArray())
                {
                    return Array.get(object, index);
                }
                else if (object instanceof List)
                {
                    return ((List) object).get(index);
                }
                throw new PropertyPath.Error();
            }
            else if (nullIsError) 
            {
                throw new PropertyPath.Error();
            }
            return null;
        }
        
        @SuppressWarnings("unchecked")
        public void set(Object bean, Object value) throws PropertyPath.Error
        {
            Object list = property.get(value);
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
            else if (nullIsError) 
            {
                throw new PropertyPath.Error();
            }
        }
    }
    
    public Property newProperty(String part, boolean nullIsError) throws PropertyPath.Error
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
                return newIndexProperty(identifier, part, i, nullIsError);
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
    
    public Property newIndexProperty(String name, String part, int i, boolean nullIsError) throws PropertyPath.Error
    {
        if (part.charAt(i) != '[')
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
                case '\'':
                case '"':
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
                    default:
                        throw new PropertyPath.Error();
                    }
                default:
                    if (ch == quote)
                    {
                        break KEY;
                    }
                    newKey.append(ch);
                }
            }
            return new MapProperty(name, newKey.toString(), nullIsError);
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
            return new ListProperty(name, index, nullIsError);
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
