package com.goodworkalan.infuse;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Infusion
{
    private final List<Path> paths;
    
    private final Map<String, Object> tree;
    
    private final Set<ObjectFactory> factories;
    
    public static Infusion getInstance(String path, Object value) throws PathException
    {
        InfusionBuilder builder = new InfusionBuilder();
        builder.set(path, value);
        return builder.getInstance();
    }

    Infusion(Set<ObjectFactory> factories, Map<String, Object> tree, List<Path> paths)
    {
        this.paths = paths;
        this.tree = tree;
        this.factories = factories;
    }
    
    public void infuse(Object object) throws NavigateException, FactoryException
    {
        for (Path path : paths)
        {
            set(object, path, 0, null);
        }
    }

    private void set(Object object,Path path, int index, Type generics) throws NavigateException, FactoryException
    {
        Part property = path.get(index);
        if (property.getName().equals("this") && !property.isIndex())
        {
            set(object, path, index + 1, generics);
        }
        else
        {
            if (object instanceof Map)
            {
                
            }
            else if (object instanceof List)
            {
                
            }
            else if (object.getClass().isArray())
            {
                
            }
            else
            {
                PropertyInfo propertyInfo = new PropertyInfo(object.getClass(), property.getName());
                int arity = path.arityAtIndex(index);
                Object child = null;
                ARITY: for (int i = arity; child == null && -1 < i; i--)
                {
                    Method getter = propertyInfo.getGetter(i);
                    if (getter == null)
                    {
                        continue ARITY;
                    }
                    Object[] parameters = new Object[i];
                    if (index + i < path.size() - 1)
                    {
                        for (int j = 0; j < parameters.length; j++)
                        {
                            if (String.class.isAssignableFrom(getter.getParameterTypes()[j]))
                            {
                                try
                                {
                                    parameters[j] = new Transmutator().transmute(getter.getParameterTypes()[j], path.get(index + j + 1).getName());
                                }
                                catch (Exception e)
                                {
                                    continue ARITY;
                                }
                            }
                        }
                        try
                        {
                            child = getter.invoke(object, parameters);
                        }
                        catch (Exception e)
                        {
                            throw new NavigateException(100, e);
                        }
                    }
                    if (index + i == path.size() - 1)
                    {
                        Method setter = propertyInfo.getSetter(i, null);
                        if (setter == null)
                        {
                            continue ARITY;
                        }
                        Class<?>[] types = setter.getParameterTypes();
                        Class<?> type = types[types.length - 1];
                        Object[] setParameters = new Object[arity + 1];
                        System.arraycopy(parameters, 0, setParameters, 0, arity);
                        try
                        {
                            setParameters[arity] = new Transmutator().transmute(type, get(path));
                        }
                        catch (Exception e)
                        {
                            continue ARITY;
                        }
                        try
                        {
                            setter.invoke(object, setParameters);
                        }
                        catch (Exception e)
                        {
                            throw new NavigateException(100, e);
                        }
                    }
                    else if (child == null)
                    {
                        Class<?> type = getter.getReturnType();
                        Method setter = propertyInfo.getSetter(i, type);
                        if (setter == null)
                        {
                            continue ARITY;
                        }
                        Iterator<ObjectFactory> eachFactory = factories.iterator();
                        while (eachFactory.hasNext() && child == null)
                        {
                            child = eachFactory.next().create(this, type, path.subPath(0, index + i));
                        }
                        Object[] setParameters = new Object[arity + 1];
                        System.arraycopy(parameters, 0, setParameters, 0, arity);
                        setParameters[arity] = child;
                        try
                        {
                            setter.invoke(object, setParameters);
                        }
                        catch (Exception e)
                        {
                            throw new NavigateException(100, e);
                        }
                    }
                    if (child != null)
                    {
                        set(child, path, index + i + 1, getter.getGenericReturnType());
                    }
                }
            }
        }
    }
    
    public String get(String path) throws PathException
    {
        return get(new Path(path, false), tree, 0);
    }
    
    public String get(Path path) throws PathException
    {
        return get(path, tree, 0);
    }

    private String get(Path properties, Map<String, Object> map, int index)
    {
        Part property = properties.get(index);
        Object current = map.get(property.getName());
        if (current == null)
        {
            return null;
        }
        if (index == properties.size() - 1)
        {
            if (current instanceof Map)
            {
                return null;
            }
            return (String) current;
        }
        return get(properties, Casts.toStringToObject(current), index + 1);
    }
}
