package com.goodworkalan.infuse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Tree implements Iterable<Path>
{
    private final Map<String, Object> tree;
    
    private final List<Path> paths; 
    
    public Tree()
    {
        this.tree = new LinkedHashMap<String, Object>();
        this.paths = new ArrayList<Path>();
    }
    
    public Iterator<Path> iterator()
    {
        return paths.iterator();
    }

    public boolean set(String path, Object value) throws ParseException
    {
        Path properties = new Path(path, false);
        if (set(properties, tree, value, 0))
        {
            paths.add(properties);
            return true;
        }
        return false;
    }
    
    public Tree add(String path, Object value) throws ParseException
    {
        set(path, value);
        return this;
    }

    // FIXME Put in subclass.
    public Map<String, Object> getMap(Path path) throws PathException
    {
        Object object = get(path);
        if (object instanceof ImmutableMap)
        {
            return (ImmutableMap) object;
        }
        return null;
    }
    
    public Map<String, Object> getMap(String path) throws PathException
    {
        Object object = get(path);
        if (object instanceof ImmutableMap)
        {
            return (ImmutableMap) object;
        }
        return null;
    }
    
    public Object get(String path) throws ParseException
    {
        return get(new Path(path, false), tree, 0);
    }
    
    public Object get(Path path)
    {
        return get(path, tree, 0);
    }
    
    private boolean set(Path properties, Map<String, Object> map, Object value, int index)
    {
        Part property = properties.get(index);
        if (index == properties.size() - 1)
        {
            if (!map.containsKey(property.getName()))
            {
                map.put(property.getName(), value);
                return true;
            }
            return false;
        }
        Object current = map.get(property.getName());
        if (current == null)
        {
            current = new ImmutableMap();
            map.put(property.getName(), current);
        }
        if (current instanceof String)
        {
            return false;
        }
        return set(properties, Objects.toStringToObject(current), value, index + 1);
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
        return get(properties, Objects.toStringToObject(current), index + 1);
    }
}
