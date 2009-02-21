package com.goodworkalan.dspl;

import java.util.LinkedList;

// TODO Document.
public class PropertyGlob extends PropertyList
{
    // TODO Document.
    public PropertyGlob(String path) throws PathException
    {
        super(path, true);
    }
    
    // TODO Document.
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