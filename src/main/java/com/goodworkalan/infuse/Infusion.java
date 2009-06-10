package com.goodworkalan.infuse;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Infusion
{
    private final Object root;
    
    private final Set<ObjectFactory> factories;
    
    public static Infusion getInstance(Object root)
    {
        if (root == null)
        {
            throw new NullPointerException();
        }
        return new Infusion(Collections.<ObjectFactory>singleton(new BasicObjectFactory()), root);
    }

    Infusion(Set<ObjectFactory> factories, Object root)
    {
        this.factories = factories;
        this.root = root;
    }
    
    public void infuse(Tree tree) throws NavigateException, FactoryException
    {
        for (Path path : tree)
        {
            set(root, tree, path, 0, null);
        }
    }
    
    public void infuse(String path, Object value) throws NavigateException, FactoryException, ParseException
    {
        infuse(new Tree().add(path, value));
    }

    private void set(Object object, Tree tree, Path path, int index, Type generics) throws NavigateException, FactoryException
    {
        Part part = path.get(index);
        if (part.getName().equals("this") && !part.isIndex())
        {
            set(object, tree, path, index + 1, generics);
        }
        else
        {
            if (object instanceof Map)
            {
                if (generics == null)
                {
                    throw new IllegalStateException();
                }
                ParameterizedType pt = (ParameterizedType) generics;
                Type type = pt.getActualTypeArguments()[1];
                Map<Object, Object> map = Objects.toObjectMap(object);
                if (index == path.size() - 1)
                {
                    Object value = tree.get(path);
                    if (value != null && !Objects.toClass(type).isAssignableFrom(value.getClass()))
                    {
                        throw new IllegalStateException();
                    }
                    map.put(part.getName(), value);
                }
                else
                {
                    Object child = map.get(part.getName());
                    if (child == null)
                    {
                        Iterator<ObjectFactory> eachFactory = factories.iterator();
                        while (eachFactory.hasNext() && child == null)
                        {
                            child = eachFactory.next().create(this, type, path.subPath(0, index));
                        }
                        if (child != null)
                        {
                            map.put(part.getName(), child);
                        }
                    }
                    if (child != null)
                    {
                        set(child, tree, path, index + 1, type);
                    }
                }
            }
            else if (object instanceof List)
            {
                
            }
            else if (object.getClass().isArray())
            {
                
            }
            else
            {
                PropertyInfo propertyInfo = new PropertyInfo(object.getClass(), part.getName());
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
                            setParameters[arity] = new Transmutator().transmute(type, (String) tree.get(path));
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
                        Object[] setParameters = new Object[i + 1];
                        System.arraycopy(parameters, 0, setParameters, 0, i);
                        setParameters[i] = child;
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
                        set(child, tree, path, index + i + 1, getter.getGenericReturnType());
                    }
                }
            }
        }
    }
    
//    public String get(String path) throws PathException
//    {
//        return get(new Path(path, false), tree, 0);
//    }
//    
//    public String get(Path path) throws PathException
//    {
//        return get(path, tree, 0);
//    }

//    private String get(Path path, Map<String, Object> map, int index)
//    {
//        Part property = path.get(index);
//        Object current = map.get(property.getName());
//        if (current == null)
//        {
//            return null;
//        }
//        if (index == path.size() - 1)
//        {
//            if (current instanceof Map)
//            {
//                return null;
//            }
//            return (String) current;
//        }
//        return get(path, Objects.toStringToObject(current), index + 1);
//    }
}
