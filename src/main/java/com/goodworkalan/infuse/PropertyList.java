package com.goodworkalan.infuse;

import static com.goodworkalan.infuse.Patterns.anyIndex;
import static com.goodworkalan.infuse.Patterns.identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO Document.
class PropertyList
{
    /** The bean path. */
    protected final List<Property> properties = new ArrayList<Property>();
    
    // TODO Document.
    private final static Pattern NAME = Pattern.compile("\\s*" + identifier(true) + "\\s*([\\[.]?)");

    // TODO Document.
    private final static Pattern INDEX = Pattern.compile("(?:" + anyIndex(true) + ")\\s*([\\[.]?)");
    
    // TODO Document.
    protected PropertyList()
    {
    }
    
    // TODO Document.
    private final static boolean moreIndexes(Matcher matcher)
    {
        return "[".equals(matcher.group(matcher.groupCount()));
    }
    
    // TODO Document.
    private final static boolean moreParts(Matcher matcher)
    {
        return ".".equals(matcher.group(matcher.groupCount()));
    }

    /**
     * Create a bean path from the given path. If the given is glob parameter is
     * true, the property list will permit glob wildcards.
     * <p>
     * The bean path is checked for syntax. The syntax check will not check the
     * path against a particular bean path, merely that the syntax does not
     * include invalid characters.
     * 
     * @param path
     *            The bean path.
     */
    protected PropertyList(String path, boolean isGlob) throws PathException
    {
        if (path == null)
        {
            throw new NullPointerException();
        }

        Matcher name = NAME.matcher(path);
        Matcher index = INDEX.matcher(path);
        
        int nameStart = 0;
        boolean moreParts = true;
        while (moreParts)
        {
            if (!name.find(nameStart))
            {
                throw new PathException(125).add(Messages.stringEscape(path))
                                            .add(nameStart);
            }
            if (name.start() != nameStart)
            {
                throw new PathException(126);
            }
            nameStart = name.end();
            
            Property property = new Property(name.group(1));
            
            int indexStart = name.end() - 1; 
            
            Matcher more = name;
            while (moreIndexes(more))
            {
                if (!index.find(indexStart))
                {
                    throw new PathException(127).add(Messages.stringEscape(path))
                                                .add(indexStart);
                }
                
                if (index.group(1) != null)
                {
                    if (!isGlob)
                    {
                        throw new PathException(128);
                    }
                    property.addIndex(new GlobIndex());
                }
                else if (index.group(2) != null)
                {
                    property.addIndex(new ListIndex(Integer.parseInt(index.group(2))));
                }
                else
                {
                    String key = index.group(3);
                    if (key == null)
                    {
                        key = index.group(4);
                    }
                    if (key != null)
                    {
                        try
                        {
                            property.addIndex(new MapIndex(MapIndex.unescape(key)));
                        }
                        catch (PathException e)
                        {
                            e.add(Messages.stringEscape(path)).add(indexStart);
                            throw e;
                        }
                    }
                }
                
                nameStart = index.end();
                indexStart = index.end() - 1;

                more = index;
            }

            properties.add(property);
            
            moreParts = moreParts(more);
        }
        
        if (nameStart != path.length())
        {
            throw new PathException(129).add(Messages.stringEscape(path))
                                        .add(Messages.charEscape(path.charAt(nameStart)))
                                        .add(nameStart);
        }
    }
    
    // TODO Document.
    void addProperty(Property property)
    {
        properties.add(property);
    }
    
    // TODO Document.
    Property getLastProperty()
    {
        return properties.get(properties.size() - 1);
    }
    
    // TODO Document.
    @Override
    public String toString()
    {
        StringBuilder newString = new StringBuilder();
        String separator = "";
        for (Property property : properties)
        {
            newString.append(separator);
            newString.append(property.toString());
            separator = ".";
        }
        return newString.toString();
    }
    
    // TODO Document.
    public String withoutIndexes()
    {
        StringBuilder newString = new StringBuilder();
        String separator = "";
        for (Property property : properties)
        {
            newString.append(separator);
            newString.append(property.name);
            separator = ".";
        }
        return newString.toString();
    }
    
    // TODO Document.
    public List<String> toList(boolean escape)
    {
        List<String> list = new ArrayList<String>();
        for (Property property : properties)
        {
            property.toList(list, escape);
       }
        return list;
    }
}
