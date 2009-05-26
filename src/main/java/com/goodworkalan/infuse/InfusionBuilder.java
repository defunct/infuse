package com.goodworkalan.infuse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InfusionBuilder
{
    private final Map<String, Object> tree;
    
    private final List<PropertyList> paths; 
    
    public InfusionBuilder()
    {
        this.tree = new LinkedHashMap<String, Object>();
        this.paths = new ArrayList<PropertyList>();
    }
    
    Map<String, Object> getTree()
    {
        return tree;
    }
    
    List<PropertyList> getPaths()
    {
        return paths;
    }
    
    public boolean set(String path, String value) throws PathException
    {
        PropertyList properties = new PropertyList(path, false);
        if (set(properties, tree, value, 0))
        {
            paths.add(properties);
            return true;
        }
        return false;
    }
    
    public String get(String path) throws PathException
    {
        return get(new PropertyList(path, false), tree, 0);
    }
    
    public Infusion getInstnace()
    {
        return new Infusion(tree, paths);
    }
    
    private boolean set(PropertyList properties, Map<String, Object> map, String value, int index)
    {
        Property property = properties.get(index);
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
            current = new LinkedHashMap<String, Object>();
            map.put(property.getName(), current);
        }
        if (current instanceof String)
        {
            return false;
        }
        return set(properties, Casts.toStringToObject(current), value, index + 1);
    }
    
    private String get(PropertyList properties, Map<String, Object> map, int index)
    {
        Property property = properties.get(index);
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