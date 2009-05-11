package com.goodworkalan.infuse;

import static com.goodworkalan.infuse.Objects.toClass;
import static com.goodworkalan.infuse.Objects.toMap;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

// TODO Document.
final class MapIndex implements Index
{
    // TODO Document.
    final String index;
    
    // TODO Document.
    public MapIndex(String index)
    {
        this.index = index;
    }
    
    // TODO Document.
    public Class<?> getRawType()
    {
        return ObjectMap.class;
    }
    
    // TODO Document.
    public Object getIndex(boolean escape)
    {
        return escape ? escape(index) : index;
    }
    
    // TODO Document.
    public boolean indexedBy(Class<?> cls)
    {
        return String.class.isAssignableFrom(cls);
    }

    // TODO Document.
    public Type typeOf(Type type) throws PathException
    {
        if (Map.class.isAssignableFrom(toClass(type)))
        {
            return ((ParameterizedType) type).getActualTypeArguments()[1];
        }
        return null;
    }
    
    // TODO Document.
    public Object get(Type type, Object container, ObjectFactory factory) throws PathException
    {
        Map<Object, Object> map = toMap(container);
        Object got = map.get(index);
        if (got == null && factory != null)
        {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            try
            {
                got = factory.create(types[1]);
            }
            catch (FactoryException e)
            {
                throw new PathException(132, e);
            }
            if (got == null)
            {
                throw new PathException(119).add(types[1]);
            }
            map.put(index, got);
        }
        return got;
    }
    
    // TODO Document.
    public void set(Type type, Object container, Object value) throws PathException
    {
        Type[] types = ((ParameterizedType) type).getActualTypeArguments();
        if (value == null || toClass(types[1]).isAssignableFrom(value.getClass()))
        {
            toMap(container).put(index, value);
        }
        else
        {
            throw new PathException(120).add(type);
        }
    }

    // TODO Document.
    public Index duplicate()
    {
        return new MapIndex(index);
    }

    // TODO Document.
    public void glob(Object bean, PropertyPath path, List<PropertyPath> glob) throws PathException
    {
        Map<Object, Object> map = toMap(path.get(bean));
        if (map.get(index) != null)
        {
            path.getLastProperty().addIndex(new MapIndex(index));
            glob.add(path);
        }
    }
    
    // TODO Document.
    @Override
    public String toString()
    {
        return "[" + escape(index) + "]";
    }
    
    // TODO Document.
    final static String escape(String index)
    {
        return "'" + index.replaceAll("['\t\b\r\n\f]", "\\($1)") + "'";
    }
 
    // TODO Document.
    final static String unescape(String key) throws PathException
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
                throw new PathException(108).add(Messages.stringEscape(key));
            case '\'':
                // This noop is only to get 100% Corbertura coverage, sorry.
                key.length();
            case '"':
                if (ch == quote)
                {
                    break KEY;
                }
                throw new PathException(109).add(Messages.stringEscape(key))
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
                    throw new PathException(110).add(Messages.stringEscape(key))
                                        .add("'\\" + ch + "'");
                }
                break;
            default:
                newKey.append(ch);
            }
        }
        return newKey.toString();
    }
}