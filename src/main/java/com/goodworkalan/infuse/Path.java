package com.goodworkalan.infuse;

import static com.goodworkalan.infuse.Patterns.anyIndex;
import static com.goodworkalan.infuse.Patterns.identifier;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO Document.
public class Path extends AbstractList<Property> implements RandomAccess
{
    /** The bean path. */
    protected final List<Property> properties;
    
    // TODO Document.
    private final static Pattern NAME = Pattern.compile("\\s*" + identifier(true) + "\\s*([\\[.]?)");

    // TODO Document.
    private final static Pattern INDEX = Pattern.compile("(?:" + anyIndex(true) + ")\\s*([\\[.]?)");

    protected Path()
    {
        this(new ArrayList<Property>());
    }

    /**
     * Create a property path with the given property list. Used by the sub
     * property list construction method {@link #subPropertyList(int, int)
     * subPropertyList}.
     * 
     * @param properties
     */
    protected Path(List<Property> properties)
    {
        this.properties = properties;
    }
    
    public Path subPropertyList(int fromIndex, int toIndex)
    {
        return new Path(subList(fromIndex, toIndex));
    }

    /**
     * Return the size of the property path. The size of the property path
     * includes the count of property names and indexes.
     * 
     * @return The property path size.
     */
    @Override
    public int size()
    {
        return properties.size();
    }

    /**
     * Returns the element at the specified position in this list.
     * 
     * @param index
     *            The index of element to return.
     * 
     * @return The element at the specified position in this list.
     * @throws IndexOutOfBoundsException
     *             if the given index is out of range.
     */
    @Override
    public Property get(int index)
    {
        return properties.get(index);
    }
    
    /**
     * Return true if the property path is a globbing property path.
     * 
     * @return True if the property path is a glob.
     */
    public boolean isGlob()
    {
        for (Property property : properties)
        {
            if (property.isGlob())
            {
                return true;
            }
        }
        return false;
    }
    
    public Path append(Property property)
    {
        List<Property> newProperties = new ArrayList<Property>(properties);
        newProperties.add(property);
        return new Path(newProperties);
    }

    public Path append(List<Property> append)
    {
        List<Property> newProperties = new ArrayList<Property>(properties);
        newProperties.addAll(append);
        return new Path(newProperties);
    }

    /**
     * Returns true if the last capture in the pattern has matched an open
     * bracket indicating that there are more indices to process.
     * 
     * @param matcher
     *            The index or name matcher.
     * @return True if there are more indexes to match.
     */
    private final static boolean moreIndexes(Matcher matcher)
    {
        return "[".equals(matcher.group(matcher.groupCount()));
    }

    /**
     * Return true if the last capture in the name pattern has matched a dot
     * indicating that there are more property path parts.
     * 
     * @param matcher
     *            The index or name matcher.
     * @return True if there are more parts to match.
     */
    private final static boolean moreParts(Matcher matcher)
    {
        return ".".equals(matcher.group(matcher.groupCount()));
    }
    
    // TODO Document.
    final static String escape(String index)
    {
        return "'" + index.replaceAll("['\t\b\r\n\f]", "\\($1)") + "'";
    }

    /**
     * Unescape a quoted hash key.
     * 
     * @param key
     *            The hash key.
     * @return The hash key with escape sequences converted into characters.
     * @throws ParseException
     *             If the key is not a valid Java string literal.
     */
    final static String unescape(String key) throws ParseException
    {
        StringBuilder newKey = new StringBuilder();
        int i = 0;
        char quote = key.charAt(i++);
        KEY: for (;;)
        {
            char ch = key.charAt(i++);
            switch (ch)
            {
            case 0:
                throw new ParseException(108).add(Messages.stringEscape(key));
            case '\'':
                // This noop is only to get 100% Corbertura coverage, sorry.
                key.length();
            case '"':
                if (ch == quote)
                {
                    break KEY;
                }
                throw new ParseException(109).add(Messages.stringEscape(key))
                                             .add(Messages.charEscape(ch))
                                             .add(Messages.charEscape(quote));
            case '\\':
                ch = key.charAt(i++);
                switch (ch)
                {
                case 'b':
                    newKey.append('\b');
                    break;
                case 't':
                    newKey.append('\t');
                    break;
                case 'n':
                    newKey.append('\n');
                    break;
                case 'f':
                    newKey.append('\f');
                    break;
                case 'r':
                    newKey.append('\r');
                    break;
                case 'u':
                    newKey.append((char) Integer.parseInt(key.substring(i, i += 4), 16));
                    break;
                case 'x':
                    newKey.append((char) Integer.parseInt(key.substring(i, i += 2), 16));
                    break;
                case '\'':
                    newKey.append('\'');
                    break;
                case '"':
                    newKey.append('"');
                    break;
                default:
                    throw new ParseException(110).add(Messages.stringEscape(key))
                                                 .add("'\\" + ch + "'");
                }
                break;
            default:
                newKey.append(ch);
            }
        }
        return newKey.toString();
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
    public Path(String path, boolean isGlob) throws ParseException
    {
        List<Property> properties = new ArrayList<Property>();
        
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
                throw new ParseException(125).add(Messages.stringEscape(path))
                                             .add(nameStart);
            }
            if (name.start() != nameStart)
            {
                throw new ParseException(126);
            }
            nameStart = name.end();
            
            properties.add(new Property(name.group(1), false, '\0'));
            
            int indexStart = name.end() - 1; 
            
            Matcher more = name;
            while (moreIndexes(more))
            {
                if (!index.find(indexStart))
                {
                    throw new ParseException(127).add(Messages.stringEscape(path))
                                                 .add(indexStart);
                }
                
                if (index.group(1) != null)
                {
                    if (!isGlob)
                    {
                        throw new ParseException(128);
                    }
                    properties.add(new Property("*", true, '\0'));
                }
                else if (index.group(2) != null)
                {
                    properties.add(new Property(index.group(2), true, '\0'));
                }
                else
                {
                    char quote = '\0';
                    String key = index.group(3);
                    if (key == null)
                    {
                        quote = '\'';
                        key = index.group(4);
                        if (key == null)
                        {
                            key = index.group(5);
                            quote = '"';
                        }
                    }
                    if (key != null)
                    {
                        try
                        {
                            key = quote == '\0' ? key : unescape(key);
                        }
                        catch (ParseException e)
                        {
                            e.add(Messages.stringEscape(path)).add(indexStart);
                            throw e;
                        }
                        properties.add(new Property(key, true, quote));
                    }
                }
                
                nameStart = index.end();
                indexStart = index.end() - 1;

                more = index;
            }
            
            moreParts = moreParts(more);
        }
        
        if (nameStart != path.length())
        {
            throw new ParseException(129).add(Messages.stringEscape(path))
                                         .add(Messages.charEscape(path.charAt(nameStart)))
                                         .add(nameStart);
        }
        
        this.properties = properties;
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
            if (!property.isIndex())
            {
                newString.append(separator);
                newString.append(property.getName());
                separator = ".";
            }
        }
        return newString.toString();
    }
    
    public int arityAtIndex(int index)
    {
        if (index >= properties.size())
        {
            throw new IndexOutOfBoundsException();
        }
        if (properties.get(index).isIndex())
        {
            throw new IllegalArgumentException();
        }
        int arity = 0;
        for (int i = index + 1; i < properties.size(); i++)
        {
            if (properties.get(i).isIndex())
            {
                arity++;
            }
            else
            {
                break;
            }
        }
        return arity;
    }
}
