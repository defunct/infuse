package com.goodworkalan.infuse;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Infusion
{
    private final Map<String, Object> tree;
    
    private final List<Path> paths;
    
    private final List<ObjectFactory> factories;
    
    public Infusion(String path, Object value) throws PathException
    {
        InfusionBuilder builder = new InfusionBuilder();
        builder.set(path, value);
        this.tree = builder.getTree();
        this.paths = builder.getPaths();
        this.factories = new ArrayList<ObjectFactory>();
    }

    public Infusion(Map<String, Object> tree, List<Path> paths)
    {
        this.tree = tree;
        this.paths = paths;
        this.factories = new ArrayList<ObjectFactory>();
    }
    
    public void infuse(Object object) throws NavigateException, FactoryException
    {
        for (Path properties : paths)
        {
            set(object, tree, properties, 0, null);
        }
    }

    private void set(Object object, Map<String, Object> map, Path properties, int index, Type generics) throws NavigateException, FactoryException
    {
        Property property = properties.get(index);
        if (property.getName().equals("this") && !property.isIndex())
        {
            set(object, map, properties, index + 1, generics);
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
                int arity = properties.arityAtIndex(index);
                Object child = null;
                ARITY: for (int i = arity; child == null && -1 < i; i--)
                {
                    Method getter = propertyInfo.getGetter(i);
                    if (getter == null)
                    {
                        continue ARITY;
                    }
                    Object[] parameters = new Object[i];
                    if (index + i < properties.size() - 1)
                    {
                        for (int j = 0; j < parameters.length; j++)
                        {
                            if (String.class.isAssignableFrom(getter.getParameterTypes()[j]))
                            {
                                try
                                {
                                    parameters[j] = new Transmutator().transmute(getter.getParameterTypes()[j], properties.get(index + j + 1).getName());
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
                    if (index + i == properties.size() - 1)
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
                            setParameters[arity] = new Transmutator().transmute(type, (String) new Diffusion(properties).get(map));
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
                            child = eachFactory.next().create(type);
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
                        set(child, map, properties, index + i + 1, getter.getGenericReturnType());
                    }
                }
            }
        }
    }
}
