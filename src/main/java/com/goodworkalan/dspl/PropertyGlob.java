package com.goodworkalan.dspl;

import java.util.LinkedList;

public class PropertyGlob extends PropertyList
{
    public PropertyGlob(String path) throws PathException
    {
        super(path, true);
    }
    
    public LinkedList<PropertyPath> all(Object bean) throws PathException
    {
        LinkedList<PropertyPath> paths = new LinkedList<PropertyPath>();
        paths.add(new PropertyPath());
        for (Property property : properties)
        {
            property.glob(bean, paths);
        }
        return paths;
    }
}