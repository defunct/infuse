package com.goodworkalan.dspl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PropertyList
{
    /** The bean path. */
    protected final List<Property> properties = new ArrayList<Property>();
    
    private final static Pattern IDENTIFIER = Pattern.compile(Patterns.SKIPWHITE + Patterns.IDENTIFIER + Patterns.SKIPWHITE + "([\\[.]?)");

    private final static Pattern INDEX = Pattern.compile("(?:" + Patterns.ANY_INDEX + ")" + Patterns.SKIPWHITE + "([\\[.]?)");
    
    protected PropertyList()
    {
    }
    
    private final static boolean moreIndexes(Matcher matcher)
    {
        return "[".equals(matcher.group(matcher.groupCount()));
    }
    
    private final static boolean moreParts(Matcher matcher)
    {
        return ".".equals(matcher.group(matcher.groupCount()));
    }

    /**
     * Create a bean path from the specified string. The bean path will be
     * checked for syntax. The syntax check will not check the path against a
     * particular bean path, merely that the syntax does not include invalid
     * characters.
     * 
     * @param path
     *            The bean path.
     */
    protected PropertyList(String path, boolean glob) throws PathException
    {
        if (path == null)
        {
            throw new NullPointerException();
        }

        Matcher identifier = IDENTIFIER.matcher(path);
        Matcher index = INDEX.matcher(path);
        
        int identifierStart = 0;
        boolean moreParts = true;
        while (moreParts)
        {
            if (!identifier.find(identifierStart))
            {
                throw new PathException(125).add(Messages.stringEscape(path))
                                            .add(identifierStart);
            }
            if (identifier.start() != identifierStart)
            {
                throw new PathException(126);
            }
            identifierStart = identifier.end();
            
            Property property = new Property(identifier.group(1));
            
            int indexStart = identifier.end() - 1; 
            
            Matcher more = identifier;
            while (moreIndexes(more))
            {
                if (!index.find(indexStart))
                {
                    throw new PathException(127).add(Messages.stringEscape(path))
                                                .add(indexStart);
                }
                
                if (index.group(1) != null)
                {
                    if (!glob)
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
                
                identifierStart = index.end();
                indexStart = index.end() - 1;

                more = index;
            }

            properties.add(property);
            
            moreParts = moreParts(more);
        }
        
        if (identifierStart != path.length())
        {
            throw new PathException(129).add(Messages.stringEscape(path))
                                        .add(Messages.charEscape(path.charAt(identifierStart)))
                                        .add(identifierStart);
        }
    }
    
    void addProperty(Property property)
    {
        properties.add(property);
    }
    
    Property getLastProperty()
    {
        return properties.get(properties.size() - 1);
    }
    
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
