package com.goodworkalan.infuse;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Infusion
{
    private final InfusionBuilder builder;
    
    private final List<ObjectFactory> factories;
    
    public Infusion(String path, Object value) throws PathException
    {
        InfusionBuilder builder = new InfusionBuilder();
        builder.set(path, value);
        
        this.builder = builder;
        this.factories = new ArrayList<ObjectFactory>();
    }

    public Infusion(InfusionBuilder builder)
    {
        this.builder = builder;
        this.factories = new ArrayList<ObjectFactory>();
    }
    
    public void infuse(Object object) throws NavigateException, FactoryException
    {
        for (Path properties : builder)
        {
            set(object, properties, 0, null);
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
                            setParameters[arity] = new Transmutator().transmute(type, builder.get(path));
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
                            child = eachFactory.next().create(builder, type, path.subPath(0, index + i));
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
}
