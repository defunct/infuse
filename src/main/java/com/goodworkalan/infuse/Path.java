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
public final class Path extends AbstractList<Part> implements RandomAccess
{
    /** The bean path. */
    protected final List<Part> parts;
    
    /** Matches a part identifier in a path. */
    private final static Pattern NAME = Pattern.compile("\\s*" + identifier(true) + "\\s*([\\[.]?)");

    /** Matches an index in a path. */
    private final static Pattern INDEX = Pattern.compile("(?:" + anyIndex(true) + ")\\s*([\\[.]?)");

    /**
     * Create an empty path.
     */
    public Path()
    {
        this(new ArrayList<Part>());
    }

    /**
     * Create a property path with the given property list. Used by the sub
     * property list construction method {@link #subPath(int, int)
     * subPropertyList}.
     * 
     * @param parts The parts of the path.
     */
    private Path(List<Part> parts)
    {
        this.parts = parts;
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
        List<Part> parts = new ArrayList<Part>();
        
        if (path == null)
        {
            throw new NullPointerException();
        }
    
        Matcher name = NAME.matcher(path);
        Matcher index = INDEX.matcher(path);
        
        boolean appendIndex = false;
        int nameStart = 0;
        boolean moreParts = true;
        while (moreParts)
        {
            if (appendIndex)
            {
                throw new ParseException(0);
            }

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
            
            parts.add(new Part(name.group(1), false, '\0'));
            
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
                    parts.add(new Part("*", true, '\0'));
                }
                else if (index.group(2) != null)
                {
                    parts.add(new Part(index.group(2), true, '\0'));
                }
                else if (index.group(3) != null)
                {
                    parts.add(new Part(index.group(3).trim(), true, '\0'));
                    appendIndex = true;
                }
                else
                {
                    char quote = '\0';
                    String key = index.group(4);
                    if (key == null)
                    {
                        quote = '\'';
                        key = index.group(5);
                        if (key == null)
                        {
                            key = index.group(6);
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
                        parts.add(new Part(key, true, quote));
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
        
        this.parts = parts;
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
     * Create a path from the sub bath defined by the given from index to the
     * given to index.
     * 
     * @param fromIndex
     *            The start index of the sub path inclusive.
     * @param toIndex
     *            The end index of the sub path exclusive.
     * @return A sub path.
     */
    public Path subPath(int fromIndex, int toIndex)
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
        return parts.size();
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
    public Part get(int index)
    {
        return parts.get(index);
    }
    
    /**
     * Return true if the property path is a globbing property path.
     * 
     * @return True if the property path is a glob.
     */
    public boolean isGlob()
    {
        for (Part property : parts)
        {
            if (property.isGlob())
            {
                return true;
            }
        }
        return false;
    }
    
    // TODO Document.
    public Path append(Part property)
    {
        List<Part> newProperties = new ArrayList<Part>(parts);
        newProperties.add(property);
        return new Path(newProperties);
    }

    // TODO Document.
    public Path appendAll(List<Part> append)
    {
        List<Part> newProperties = new ArrayList<Part>(parts);
        newProperties.addAll(append);
        return new Path(newProperties);
    }

    // TODO Document.
    final static String escape(String index, char quote)
    {
        return quote + index.replaceAll("[" + quote + "\t\b\r\n\f]", "\\($1)") + quote;
    }
    
    // TODO Document.
    @Override
    public String toString()
    {
        StringBuilder newString = new StringBuilder();
        String separator = "";
        for (Part part : parts)
        {
            if (part.isIndex())
            {
                if (part.getQuote() != '\0')
                {
                    newString.append("[").append(part.getName()).append("]");
                }
                else
                {
                    newString.append("[").append(part.getQuote())
                             .append(part.getName())
                             .append(part.getQuote()).append("]");
                }
            }
            else
            {
                newString.append(separator);
                newString.append(part.getName());
                separator = ".";
            }
        }
        return newString.toString();
    }
    
    // TODO Document.
    public String withoutIndexes()
    {
        StringBuilder newString = new StringBuilder();
        String separator = "";
        for (Part property : parts)
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
    
    // TODO Document.
    public int arityAtIndex(int index)
    {
        if (index >= parts.size())
        {
            throw new IndexOutOfBoundsException();
        }
        int arity = 0;
        for (int i = index + 1; i < parts.size(); i++)
        {
            if (parts.get(i).isIndex())
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
