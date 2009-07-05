package com.goodworkalan.infuse;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Infusion
{
    /** The root object to populate with this infusion. */
    private final Object root;
    
    /** A set of factories to use to create objects during an infusion. */
    private final Set<ObjectFactory> factories;
    
    /**
     * A tree that tracks the sizes of lists before they are expanded to 
     * accommodate list items with indexed beyond the last index of the list. 
     */
    private final Tree listSizes;
    
    public static Infusion getInstance(Object root)
    {
        if (root == null)
        {
            throw new NullPointerException();
        }
        Set<ObjectFactory> factories = new LinkedHashSet<ObjectFactory>();
        factories.add(new CollectionFactory());
        factories.add(new DefaultConstructorFactory());
        return new Infusion(factories, root);
    }

    Infusion(Set<ObjectFactory> factories, Object root)
    {
        this.factories = factories;
        this.root = root;
        this.listSizes = new Tree();
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
        // FIXME Add "this". Or get rid of this. Why this?
        Part part = path.get(index);
        if (part.getName().equals("this") && !part.isIndex())
        {
            set(object, tree, path, index + 1, generics);
        }
        else
        {
            if (object instanceof Map<?, ?>)
            {
                if (generics instanceof Class<?>)
                {
                    throw new IllegalStateException();
                }
                ParameterizedType pt = (ParameterizedType) generics;
                Object key;
                try
                {
                    key = new Transmutator().transmute(Objects.toClass(pt.getActualTypeArguments()[0]), part.getName());
                }
                catch (TransmutationException e)
                {
                    throw new NavigateException(PathException.NO_REAL_MESSAGE, e);
                }
                Type type = pt.getActualTypeArguments()[1];
                Map<Object, Object> map = Objects.toObjectMap(object);
                if (index == path.size() - 1)
                {
                    Object value;
                    try
                    {
                        value = new Transmutator().transmute(Objects.toClass(type), (String) tree.get(path));
                    }
                    catch (TransmutationException e)
                    {
                        throw new NavigateException(PathException.NO_REAL_MESSAGE, e);
                    }
                    map.put(key, value);
                }
                else
                {
                    Object child = map.get(key);
                    if (child == null)
                    {
                        Iterator<ObjectFactory> eachFactory = factories.iterator();
                        while (eachFactory.hasNext() && child == null)
                        {
                            child = eachFactory.next().create(type, tree, path.subPath(0, index + 1));
                        }
                        if (child != null)
                        {
                            map.put(key, child);
                        }
                    }
                    if (child != null)
                    {
                        set(child, tree, path, index + 1, type);
                    }
                }
            }
            else if (object instanceof List<?>)
            {
                if (generics instanceof Class<?>)
                {
                    throw new IllegalStateException();
                }
                ParameterizedType pt = (ParameterizedType) generics;
                Type type = pt.getActualTypeArguments()[0];
                List<Object> list = Objects.toObjectList(object);
                int i;
                if (part.isAppend())
                {
                    Object value;
                    try
                    {
                        value = new Transmutator().transmute(Objects.toClass(type), (String) tree.get(path));
                    }
                    catch (TransmutationException e)
                    {
                        throw new NavigateException(PathException.NO_REAL_MESSAGE, e);
                    }
                    list.add(value);
                }
                else
                {
                    try
                    {
                        i = Integer.parseInt(part.getName());
                    }
                    catch (NumberFormatException e)
                    {
                        throw new IllegalStateException(e);
                    }
                    int size = 0;
                    Path sizePath = path.subPath(0, index).append(new Part("size"));
                    if (listSizes.containsPath(sizePath))
                    {
                        size = (Integer) listSizes.get(sizePath);
                    }
                    else
                    {
                        size = list.size();
                        listSizes.set(sizePath, (Integer) size);
                    }
                    if (i >= size)
                    {
                        int j;
                        for (j = i - 1; j >= size; j--)
                        {
                            Path subPath = path.subPath(0, index).append(new Part(Integer.toString(j), true, '\0'));
                            if (!tree.containsPath(subPath))
                            {
                                i--;
                            }
                        }
                        while (list.size() <= i)
                        {
                            list.add(null);
                        }
                    }
                    if (index == path.size() - 1)
                    {
                        Object value;
                        try
                        {
                            value = new Transmutator().transmute(Objects.toClass(type), (String) tree.get(path));
                        }
                        catch (TransmutationException e)
                        {
                            throw new NavigateException(PathException.NO_REAL_MESSAGE, e);
                        }
                        list.set(i, value);
                    }
                    else
                    {
                        Object child = list.get(i);
                        if (child == null)
                        {
                            Iterator<ObjectFactory> eachFactory = factories.iterator();
                            while (eachFactory.hasNext() && child == null)
                            {
                                child = eachFactory.next().create(type, tree, path.subPath(0, index + 1));
                            }
                            list.set(i, child);
                        }
                        
                        set(child, tree, path, index + 1, type);
                    }
                }
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
                    boolean isFinal = index + i == path.size() - 1;
                    if (isFinal)
                    {
                        Method setter = propertyInfo.getSetter(i);
                        if (setter == null)
                        {
                            continue ARITY;
                        }
                        Object[] parameters;
                        try
                        {
                            parameters = propertyInfo.getSetterParameters(path, setter, index, new Transmutator().transmute(Objects.toClass(setter.getParameterTypes()[i]), (String) tree.get(path)));
                        }
                        catch (TransmutationException e1)
                        {
                            continue ARITY;
                        }
                        if (parameters == null)
                        {
                            continue ARITY;
                        }
                        try
                        {
                            setter.invoke(object, parameters);
                        }
                        catch (Exception e)
                        {
                            throw new NavigateException(100, e);
                        }
                    }
                    else
                    {
                        Method getter = propertyInfo.getGetter(i);
                        if (getter == null)
                        {
                            continue ARITY;
                        }
                        Object[] parameters = propertyInfo.getGetterParameters(path, getter, index);
                        if (parameters == null)
                        {
                            continue ARITY;
                        }
                        try
                        {
                            child = getter.invoke(object, parameters);
                        }
                        catch (Exception e)
                        {
                            throw new NavigateException(100, e);
                        }
                        if (child == null)
                        {
                            Class<?> type = getter.getReturnType();
                            Method setter = propertyInfo.getSetter(i);
                            if (setter == null)
                            {
                                continue ARITY;
                            }
                            Iterator<ObjectFactory> eachFactory = factories.iterator();
                            while (eachFactory.hasNext() && child == null)
                            {
                                child = eachFactory.next().create(type, tree, path.subPath(0, index + 1 + i));
                            }
                            parameters = propertyInfo.getSetterParameters(path, setter, index, child);
                            if (parameters == null)
                            {
                                continue ARITY;
                            }
                            try
                            {
                                setter.invoke(object, parameters);
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
    }
}
