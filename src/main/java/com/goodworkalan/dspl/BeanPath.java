package com.goodworkalan.dspl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

public class BeanPath
{
    private final String path;
    
    public BeanPath(String path)
    {
        this.path = path;
    }
    
    public Object get(Object bean) throws BeanPath.Error
    {
        String[] parts = path.split("\\.");
        for (int i = 0; bean != null && i < parts.length - 1; i++)
        {
            bean = get(bean, parts[i]);
        }
        return get(bean, parts[parts.length - 1]);
    }
    
    public void set(Object bean, ValueServer value) throws BeanPath.Error
    {
        String[] parts = path.split("\\.");
        for (int i = 0; bean != null && i < parts.length - 1; i++)
        {
            bean = get(bean, parts[i]);
        }
        if (bean != null)
        {
        }
    }
    
    private int eatWhite(String part, int i)
    {
        for (; i < part.length() && Character.isWhitespace(part.charAt(i)); i++)
        {
        }
        return i;
    }
    
    private int getIdentifier(String part, StringBuilder identifier)
    {
        identifier.append(part.charAt(0));
        int i = 1;
        for (; i < part.length() && Character.isJavaIdentifierPart(part.charAt(i)); i++)
        {
            identifier.append(part.charAt(i));
        }
        return i;
    }
    
    private Object get(Object bean, String part) throws BeanPath.Error
    {
        part = part.trim();
        if (part.length() == 0 || !Character.isJavaIdentifierStart(part.charAt(0)))
        {
            throw new Error();
        }

        // Read the Java bean identifier.
        StringBuilder newIdentifier = new StringBuilder();
        int i = getIdentifier(part, newIdentifier);
        String identifier = newIdentifier.toString();
        
        // Skip an whitespace.
        i = eatWhite(part, i);
        
        // Check for an optional indexed parameter.
        if (i < part.length() && part.charAt(i) == '[')
        {
            
        }
        
        if (i != part.length())
        {
            throw new BeanPath.Error();
        }
        
        BeanInfo beanInfo;
        try
        {
            beanInfo = Introspector.getBeanInfo(bean.getClass());
        }
        catch (IntrospectionException e)
        {
            throw new BeanPath.Error(e);
        }
        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors())
        {
            if (descriptor.getName().equals(identifier))
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
                    throw new BeanPath.Error(e);
                }
            }
        }
        
        throw new BeanPath.Error();
    }
    
    public interface ValueServer
    {
        public Object getValue(Class<?> valueClass);
    }
    
    public final static class CoreValueServer implements ValueServer
    {
        private Object value;
        
        public CoreValueServer(Object value)
        {
            this.value = value;
        }
        
        public Object getValue(Class<?> valueClass)
        {
            return value;
        }
    }
    
    public final static class ArrayValueServer implements ValueServer
    {
        public Object[] array;
        
        public ArrayValueServer(Object[] array)
        {
            this.array = array;
        }
        
        public Object getValue(Class<?> valueClass)
        {
            if (array == null)
            {
                return null;
            }
            return valueClass.isArray() ? array : array.length == 0 ? null : array[0];
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
