package com.goodworkalan.dspl;

import static com.goodworkalan.dspl.Objects.toMap;
import static com.goodworkalan.dspl.Objects.toClass;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

final class MapIndex implements Index
{
    final String index;
    
    public MapIndex(String index)
    {
        this.index = index;
    }
    
    public Class<?> getRawType()
    {
        return ObjectMap.class;
    }
    
    public Object getIndex(boolean escape)
    {
        return escape ? escape(index) : index;
    }
    
    public boolean indexedBy(Class<?> cls)
    {
        return String.class.isAssignableFrom(cls);
    }

    public Type typeOf(Type type) throws PathException
    {
        if (Map.class.isAssignableFrom(toClass(type)))
        {
            return ((ParameterizedType) type).getActualTypeArguments()[1];
        }
        return null;
    }
    
    public Object get(Type type, Object container, ObjectFactory factory) throws PathException
    {
        Map<Object, Object> map = toMap(container);
        Object got = map.get(index);
        if (got == null && factory != null)
        {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            got = factory.create(types[1]);
            if (got == null)
            {
                throw new PathException(119).add(types[1]);
            }
            map.put(index, got);
        }
        return got;
    }
    
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
    
    @Override
    public String toString()
    {
        return "[" + escape(index) + "]";
    }
    
    final static String escape(String index)
    {
        return "'" + index.replaceAll("['\t\b\r\n\f]", "\\($1)") + "'";
    }
 
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