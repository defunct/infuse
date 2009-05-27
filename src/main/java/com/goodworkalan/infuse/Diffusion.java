package com.goodworkalan.infuse;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


// TODO Document.
public class Diffusion
{
    // TODO Document.
    private final PropertyList properties;
    
    // TODO Document.
    public Diffusion(String path) throws ParseException
    {
        if (path == null)
        {
            throw new NullPointerException();
        }
        this.properties = new PropertyList(path, true);
    }

    // TODO Document.
    public Diffusion(PropertyList properties)
    {
        if (properties == null)
        {
            throw new NullPointerException();
        }
        this.properties = properties;
    }
    
    // TODO Document.
    public String withoutIndexes()
    {
        return properties.withoutIndexes();
    }

    // TODO Document.
    public Object get(Object object) throws NavigateException
    {
        return get(object, properties, 0);
    }
    
    // TODO Document.
    private Object get(Object object, PropertyList properties, int index) throws NavigateException
    {
        Property property = properties.get(index);
        if (property.getName().equals("this") && !property.isIndex())
        {
            if (index + 1 == properties.size())
            {
                return object;
            }
            return get(object, properties, index + 1);
        }
        if (object instanceof Map)
        {
            Map<String, Object> map = Casts.toStringToObject(object);
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
            if (child == null)
            {
                continue ARITY;
            }
            if (index + i == properties.size() - 1)
            {
                return child;
            }
            else
            {
                return get(child, properties, index + i + 1);
            }
        }
        return child;
    }
    
    public List<Diffusion> all(Object object) throws NavigateException
    {
        List<Diffusion> diffusions = new ArrayList<Diffusion>();
        glob(object, new PropertyList(), 0, diffusions);
        return diffusions;
    }
    
    public void glob(Object object, PropertyList base, int from, List<Diffusion> diffusions) throws NavigateException
    {
        int i;
        for (i = from; i < properties.size(); i++)
        {
            Property property = properties.get(i);
            if (property.isGlob())
            {
                PropertyList subPath = properties.subPropertyList(from, i);
                Object collection = new Diffusion(subPath).get(object);
                PropertyList path = base.append(subPath);
                if (collection instanceof List)
                {
                    List<Object> list = Casts.toObjectList(collection);
                    for (int j = 0; j < list.size(); j++)
                    {
                        Object item = list.get(j);
                        if (item != null)
                        {
                            PropertyList unglobbed = path.append(new Property(Integer.toString(j), true, '\0'));
                            glob(item, unglobbed, i + 1, diffusions);
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
                            PropertyList unglobbed = path.append(new Property(Integer.toString(j), true, '\0'));
                            glob(item, unglobbed, i + 1, diffusions);
                        }
                    }
                }
                else if (collection instanceof Map)
                {
                    Map<Object, Object> map = Casts.toObjectMap(collection);
                    for (Map.Entry<Object, Object> entry : map.entrySet())
                    {
                        if ((entry.getKey() instanceof String) && entry.getValue() != null)
                        {
                            PropertyList unglobbed = path.append(new Property((String) entry.getKey(), true, '\0'));
                            glob(entry.getValue(), unglobbed, i + 1, diffusions);
                        }
                    }
                }
                break;
            }
        }
        if (i == properties.size())
        {
            PropertyList subPath = properties.subPropertyList(from, properties.size());
            if (new Diffusion(subPath).get(object) != null)
            {
                diffusions.add(new Diffusion(base.append(subPath)));
            }
        }
    }
}
