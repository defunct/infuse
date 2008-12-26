package com.goodworkalan.dspl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PropertyList
{
    final static String SKIPWHITE = "\\s*";
    
    final static String IDENTIFIER = "([\\w&&[^\\d]][\\w\\d]*)";
    
    final static String LIST_INDEX = "\\[\\s*(\\d+)\\s*\\]";
    
    final static String GLOB_INDEX = "\\[\\s*(\\*)\\s*\\]";
    
    final static String QUOTE_1_INDEX = stringIndex('\'');
    
    final static String QUOTE_2_INDEX = stringIndex('"');
    
    final static String ANY_INDEX =
        GLOB_INDEX + "|" + LIST_INDEX + "|" +
            QUOTE_1_INDEX + "|" + QUOTE_2_INDEX;
    
    final static String PART =
        SKIPWHITE + IDENTIFIER + SKIPWHITE +
           "(?:" +
               "(?:" + ANY_INDEX + ")" +
           SKIPWHITE + ")*";

    final static Pattern GLOB = Pattern.compile(PART + "(?:(\\.)" + PART + ")*");

    private final static String escaped(String...characters)
    {
        StringBuffer newString = new StringBuffer();
        String separator = "";
        for (int i = 0; i < characters.length; i++)
        {
            newString.append(separator)
                     .append("\\\\")
                     .append(characters[i]);
            separator = "|";
        }
        return newString.toString();
    }

    private final static String stringIndex(char quote)
    {
        String escaped = escaped("b", "f", "n", "r", "t");
        return "\\[" + SKIPWHITE
                     + "("
                     + quote
                         + "(?:"
                             + "[^" + quote + "\\\\]"
                             + "|" 
                             + "(?:"
                                 + "\\\\\\\\"
                                 + "|"
                                 + "\\\\" + quote
                                 + "|"
                                 + "\\\\u[A-Fa-f0-9]{4}"
                                 + "|"
                                 + "\\\\x[A-Fa-f0-9]{2}"
                                 + "|"
                                 + escaped
                             + ")"
                         + ")*"
                     + quote
                     + ")"
                     + SKIPWHITE + "\\]"; 
    }

    /** The bean path. */
    protected final List<Property> properties = new ArrayList<Property>();
    
    private final static Pattern NAME = Pattern.compile(SKIPWHITE + IDENTIFIER + SKIPWHITE + "([\\[.]?)");

    private final static Pattern INDEX = Pattern.compile("(?:" + ANY_INDEX + ")" + SKIPWHITE + "([\\[.]?)");
    
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
