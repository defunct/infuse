package com.goodworkalan.dspl;

import java.lang.reflect.Type;

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
 * 
 * @author Alan Gutierrez
 */
public class PropertyPath extends PropertyList
{
    public PropertyPath(String path) throws PathException
    {
        super(path, false);
    }

    /**
     * Get the bean property matching this bean path.
     * 
     * @param bean
     *            The root bean of an object graph.
     * @throws PathException
     *             If the path does not exist or if an error occurs in
     *             reflection.
     */
    public Object get(Object bean) throws PathException
    {
        if (bean == null)
        {
            throw new IllegalArgumentException();
        }

        int last = properties.size() - 1;
        for (int i = 0; bean != null && i < last; i++)
        {
            bean = properties.get(i).get(bean, null);
        }

        if (bean != null)
        {
            return properties.get(last).get(bean, null);
        }
        
        throw new PathException(101);
    }
    
    public Type typeOf(Object bean, ObjectFactory factory) throws PathException
    {
        if (bean == null)
        {
            throw new IllegalArgumentException();
        }

        try
        {
            int last = properties.size() - 1;
            for (int i = 0; bean != null && i < last; i++)
            {
                bean = properties.get(i).get(bean, factory);
            }
    
            Type type = null;
            if (bean != null)
            {
                type = properties.get(last).typeOf(bean);
            }
    
            if (type == null && factory != null)
            {
                throw new PathException(102);
            }

            return type;
        }
        catch (PathException e)
        {
            e.add(Messages.stringEscape(toString())).add(bean.getClass().getName());
            throw e;
        }
    }
    
    public Type typeOf(Object bean, boolean create) throws PathException
    {
        return typeOf(bean, create ? new CoreObjectFactory() : null);
    }

    /**
     * Set the bean property matching this bean path to the specified value.
     * 
     * @param bean
     *            The root bean of an object graph.
     * @param value
     *            The value to set.
     * @throws PathException
     *             If the path does not exist, if the value is of the incorrect
     *             type, or if an error occurs in reflection.
     */
    public void set(Object bean, Object value, ObjectFactory factory) throws PathException
    {
        if (bean == null)
        {
            throw new IllegalArgumentException();
        }

        try
        {
            Object object = bean;
            int last = properties.size() - 1;
            for (int i = 0; object != null && i < last; i++)
            {
                object = properties.get(i).get(object, factory);
            }
            if (object != null)
            {
                properties.get(last).set(object, value, factory);
            }
            else if (factory != null)
            {
                throw new PathException(103);
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
    
    public void set(Object bean, Object value, boolean create) throws PathException
    {
        set(bean, value, create ? new CoreObjectFactory() : null);
    }
}
