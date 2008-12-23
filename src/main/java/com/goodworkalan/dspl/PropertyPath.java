package com.goodworkalan.dspl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * A dirt simple bean property path navigator that gets and sets bean properties
 * within object graphs.
 * </p>
 * <p>
 * Is is not entirely possible to check against an potential property path using
 * a class. The <code>PropertyPath</code> class can navigate maps, lists, and
 * arrays. Although type information is available through generic types, it may
 * be the case that a generic container is declared with an Object as type
 * property. We won't know the real properties until we navigate an actual
 * object graph.
 * </p>
 * TODO Consider carefully whether you want to break this up
 *      into separate classes. PropertyPath.Factory is not shorter than
 *      PropertyFactory (or ObjectFactory).
 * 
 * @author Alan Gutierrez
 */
public class PropertyPath
{
    /** The bean path. */
    private final Property[] properties;
    
    /**
     * Create a bean path from the specified string. The bean path will be
     * checked for syntax. The syntax check will not check the path against a
     * particular bean path, merely that the syntax does not include invalid
     * characters.
     * 
     * @param path
     *            The bean path.
     */
    public PropertyPath(String path) throws PathException
    {
        if (path == null)
        {
            throw new IllegalArgumentException();
        }

        String[] parts = path.split("\\.");
        Property[] properties = new Property[parts.length];
        for (int i = 0; i < parts.length; i++)
        {
            try
            {
                properties[i] = newProperty(parts[i]);
            }
            catch (PathException e)
            {
                throw e.add(stringEscape(path));
            }
        }
        this.properties = properties;
    }

    /**
     * Get the bean property matching this bean path.
     * 
     * @param bean
     *            The root bean of an object graph.
     * @throws PathException
     *             If the path does not exist or if an error occurs in
     *             reflection.
     */
    public Object get(Object bean) throws PathException
    {
        if (bean == null)
        {
            throw new IllegalArgumentException();
        }

        for (int i = 0; bean != null && i < properties.length - 1; i++)
        {
            bean = properties[i].get(bean, null);
        }

        if (bean != null)
        {
            return properties[properties.length - 1].get(bean, null);
        }
        
        throw new PathException(101);
    }
    
    public Type typeOf(Object bean, ObjectFactory factory) throws PathException
    {
        if (bean == null)
        {
            throw new IllegalArgumentException();
        }

        try
        {
            for (int i = 0; bean != null && i < properties.length - 1; i++)
            {
                bean = properties[i].get(bean, factory);
            }
    
            Type type = null;
            if (bean != null)
            {
                type = properties[properties.length - 1].typeOf(bean);
            }
    
            if (type == null && factory != null)
            {
                throw new PathException(102);
            }

            return type;
        }
        catch (PathException e)
        {
            e.add(stringEscape(toString())).add(bean.getClass().getName());
            throw e;
        }
    }
    
    public Type typeOf(Object bean, boolean create) throws PathException
    {
        return typeOf(bean, create ? new CoreObjectFactory() : null);
    }

    /**
     * Set the bean property matching this bean path to the specified value.
     * 
     * @param bean
     *            The root bean of an object graph.
     * @param value
     *            The value to set.
     * @throws PathException
     *             If the path does not exist, if the value is of the incorrect
     *             type, or if an error occurs in reflection.
     */
    public void set(Object bean, Object value, ObjectFactory factory) throws PathException
    {
        if (bean == null)
        {
            throw new IllegalArgumentException();
        }

        try
        {
            Object object = bean;
            for (int i = 0; object != null && i < properties.length - 1; i++)
            {
                object = properties[i].get(object, factory);
            }
            if (object != null)
            {
                properties[properties.length - 1].set(object, value, factory);
            }
            else if (factory != null)
            {
                throw new PathException(103);
            }
        }
        catch (PathException e)
        {
            e.add(stringEscape(toString()))
             .add(bean.getClass().getName())
             .add(value == null ? value : value.getClass().getName());
            throw e;
        }
    }
    
    public void set(Object bean, Object value, boolean create) throws PathException
    {
        set(bean, value, create ? new CoreObjectFactory() : null);
    }
    
    @Override
    public String toString()
    {
        StringBuilder newString = new StringBuilder();
        String separator = "";
        for (int i = 0; i < properties.length; i++)
        {
            newString.append(separator);
            newString.append(properties[i].toString());
            separator = ".";
        }
        return newString.toString();
    }

    final static Class<?> toClass(Type type)
    {
        if (type instanceof Class)
        {
            return (Class<?>) type;
        }
        else if (type instanceof ParameterizedType)
        {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    static Map<Object, Object> toMap(Object object)
    {
        return (Map) object;
    }

    /**
     * Skip whitespace between property names, array brackets, and property
     * indices.
     * 
     * @param part
     *            A part of a property path.
     * @param i
     *            The current index.
     * @return The index of the first non-whitespace character, current index
     *         included.
     */
    static int eatWhite(String part, int i)
    {
        for (; i < part.length() && Character.isWhitespace(part.charAt(i)); i++)
        {
        }
        return i;
    }

    /**
     * Read a Java identifier from the start of a part of a property path.
     * 
     * @param part
     *            A part of a property path.
     * @param identifier
     *            A string builder to collect the identifier.
     * @return The index of the first character that is not a part of a Java
     *         identifier.
     */
    static int getIdentifier(String part, StringBuilder identifier, int i)
    {
        identifier.append(part.charAt(i++));
        for (; i < part.length() && Character.isJavaIdentifierPart(part.charAt(i)); i++)
        {
            identifier.append(part.charAt(i));
        }
        return i;
    }
    
    static Property newProperty(String part) throws PathException
    {
        int i = eatWhite(part, 0);
        
        if (i == part.length())
        {
            throw new PathException(104);
        }

        if (!Character.isJavaIdentifierStart(part.charAt(i)))
        {
            throw new PathException(121).add(stringEscape(part))
                                .add(charEscape(part.charAt(i)));
        }
    
        // Read the Java bean identifier.
        StringBuilder newIdentifier = new StringBuilder();
        i = getIdentifier(part, newIdentifier, i);
        String identifier = newIdentifier.toString();
        
        // Skip an whitespace.
        i = eatWhite(part, i);
        
        List<Index> indices = new ArrayList<Index>();
    
        // Check for an optional indexed parameter.
        while (i != part.length())
        {
            try
            {
                i = newIndex(part, i, indices);
            }
            catch (StringIndexOutOfBoundsException e)
            {
                throw new PathException(105, e).add(stringEscape(part));
            }
            catch (NumberFormatException e)
            {
                throw new PathException(106, e).add(stringEscape(part));
            }
        }
        
        return new Property(identifier, indices.toArray(new Index[indices.size()]));
    }

    static int newIndex(String part, int i, List<Index> indexes)
        throws PathException
    {
        if (part.charAt(i++) != '[')
        {
            throw new PathException(107).add(stringEscape(part))
                                .add(charEscape(part.charAt(i - 1)));
        }
    
        i = eatWhite(part, i);
        
        if ("\"'".indexOf(part.charAt(i)) != -1)
        {
            StringBuilder newKey = new StringBuilder();
            char quote = part.charAt(i++);
            KEY: for (;;)
            {
                char ch = part.charAt(i++);
                switch (ch)
                {
                case 0:
                    throw new PathException(108).add(stringEscape(part));
                case '\'':
                    // This noop is only to get 100% Corbertura coverage, sorry.
                    part.length();
                case '"':
                    if (ch == quote)
                    {
                        break KEY;
                    }
                    throw new PathException(109).add(stringEscape(part))
                                        .add(charEscape(ch))
                                        .add(charEscape(quote));
                case '\\':
                    ch = part.charAt(i++);
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
                        newKey.append((char) Integer.parseInt(part.substring(i, i += 4), 16));
                        break;
                    case 'x':
                        newKey.append((char) Integer.parseInt(part.substring(i, i += 2), 16));
                        break;
                    case '\'':
                        newKey.append('\'');
                        break;
                    case '"':
                        newKey.append('"');
                        break;
                    default:
                        throw new PathException(110).add(stringEscape(part))
                                            .add("'\\" + ch + "'");
                    }
                    break;
                default:
                    newKey.append(ch);
                }
            }
            i = eatWhite(part, i);
            if (part.charAt(i++) != ']')
            {
                throw new PathException(111).add(stringEscape(part))
                                    .add(charEscape(part.charAt(i - 1)));
            }
    
            indexes.add( new MapIndex(newKey.toString()));
            
            return eatWhite(part, i);
        }
    
        int index = 0;
        do
        {
            int n = Integer.parseInt(new Character(part.charAt(i++)).toString(), 10);
            index = index * 10 + n;
        }
        while ("] ".indexOf(part.charAt(i)) == -1);
        
        i = eatWhite(part, i);
        
        indexes.add(new ListIndex(index));
        
        return eatWhite(part, i + 1);
    }
    
    final static String charEscape(char ch)
    {
        return "'" + (ch == '\'' || ch == '\\' ? "\\" + ch : ch) + "'"; 
    }
    
    final static String stringEscape(String string)
    {
        Pattern pattern = Pattern.compile("[\\\"\b\r\n\f\t\0\1\2\3\4\5\6\7]");
        Matcher matcher = pattern.matcher(string);
        StringBuffer newString = new StringBuffer();
        while (matcher.find())
        {
            char ch = string.charAt(matcher.start());
            String replacement;
            if (ch < 8)
            {
                replacement = "\\\\" + (int) ch;
            }
            else
            {
                switch (ch)
                {
                case '\b':
                    replacement = "\\\\b";
                case '\f':
                    replacement = "\\\\f";
                case '\n':
                    replacement = "\\\\n";
                case '\r':
                    replacement = "\\\\r";
                case '\t':
                    replacement = "\\\\t";
                default:
                    replacement = "\\\\" + ch;
                }
            }
            matcher.appendReplacement(newString, replacement);
        }
        matcher.appendTail(newString);
        return "\"" + newString.toString() + "\"";
    }
}