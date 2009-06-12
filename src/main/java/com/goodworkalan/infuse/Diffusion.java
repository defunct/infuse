package com.goodworkalan.infuse;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Extracts values from an object according to an object path.  
 *
 * @author Alan Gutierrez
 */
public class Diffusion
{
    /** The root object to navigate with this diffusion. */
    private final Object root;

    /**
     * Create a diffusion that will extract values from the given root object.
     * 
     * @param root
     *            The root object.
     */
    public Diffusion(Object root)
    {
        if (root == null)
        {
            throw new NullPointerException();
        }
        this.root = root;
    }
    
    // TODO Document.
    public Object get(String path) throws ParseException, NavigateException
    {
        return get(root, new Path(path, false), 0);
    }
    
    // TODO Document.
    public Object get(Path path) throws NavigateException
    {
        if (path == null)
        {
            throw new NullPointerException();
        }
        return get(root, path, 0);
    }
    
    // TODO Document.
    private Object get(Object object, Path path, int index) throws NavigateException
    {
        Part property = path.get(index);
        if (property.getName().equals("this") && !property.isIndex())
        {
            if (index + 1 == path.size())
            {
                return object;
            }
            return get(object, path, index + 1);
        }
        if (object instanceof Map)
        {
            Map<String, Object> map = Objects.toStringToObject(object);
            return map.get(property.getName());
        }
        else if (object instanceof List)
        {
            return null;
        }
        else if (object.getClass().isArray())
        {
            return null;
        }
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
            if (child == null)
            {
                continue ARITY;
            }
            if (index + i == path.size() - 1)
            {
                return child;
            }
            else
            {
                return get(child, path, index + i + 1);
            }
        }
        return child;
    }
    
    public List<Path> all(String path) throws NavigateException, ParseException
    {
        List<Path> paths = new ArrayList<Path>();
        glob(root, new Path(path, true), new Path(), 0, paths);
        return paths;
    }

    public List<Path> all(Path path) throws NavigateException
    {
        List<Path> paths = new ArrayList<Path>();
        glob(root, path, new Path(), 0, paths);
        return paths;
    }
    
    private void glob(Object object, Path fullPath, Path base, int from, List<Path> paths) throws NavigateException
    {
        int i;
        for (i = from; i < fullPath.size(); i++)
        {
            Part property = fullPath.get(i);
            if (property.isGlob())
            {
                Path subPath = fullPath.subPath(from, i);
                Object collection = new Diffusion(object).get(subPath);
                Path path = base.appendAll(subPath);
                if (collection instanceof List)
                {
                    List<Object> list = Objects.toObjectList(collection);
                    for (int j = 0; j < list.size(); j++)
                    {
                        Object item = list.get(j);
                        if (item != null)
                        {
                            Path unglobbed = path.append(new Part(Integer.toString(j), true, '\0'));
                            glob(item, fullPath, unglobbed, i + 1, paths);
                        }
                    }
                }
                else if (collection.getClass().isArray())
                {
                    Object[] array = (Object[]) collection;
                    for (int j = 0; j < array.length; j++)
                    {
                        Object item = array[j];
                        if (item != null)
                        {
                            Path unglobbed = path.append(new Part(Integer.toString(j), true, '\0'));
                            glob(item, fullPath, unglobbed, i + 1, paths);
                        }
                    }
                }
                else if (collection instanceof Map)
                {
                    Map<Object, Object> map = Objects.toObjectMap(collection);
                    for (Map.Entry<Object, Object> entry : map.entrySet())
                    {
                        if ((entry.getKey() instanceof String) && entry.getValue() != null)
                        {
                            Path unglobbed = path.append(new Part((String) entry.getKey(), true, '\0'));
                            glob(entry.getValue(), fullPath, unglobbed, i + 1, paths);
                        }
                    }
                }
                break;
            }
        }
        if (i == fullPath.size())
        {
            Path subPath = fullPath.subPath(from, fullPath.size());
            if (new Diffusion(object).get(subPath) != null)
            {
                paths.add(base.appendAll(subPath));
            }
        }
    }
}
