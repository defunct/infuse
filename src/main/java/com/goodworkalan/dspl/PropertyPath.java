package com.goodworkalan.dspl;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
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
    public PropertyPath(String path) throws Error
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
            catch (Error e)
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
     * @throws Error
     *             If the path does not exist or if an error occurs in
     *             reflection.
     */
    public Object get(Object bean) throws Error
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
        
        throw new Error(101);
    }
    
    public Type typeOf(Object bean, Factory factory) throws Error
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
                throw new Error(102);
            }

            return type;
        }
        catch (PropertyPath.Error e)
        {
            e.add(stringEscape(toString())).add(bean.getClass().getName());
            throw e;
        }
    }
    
    public Type typeOf(Object bean, boolean create) throws Error
    {
        return typeOf(bean, create ? new CoreFactory() : null);
    }

    /**
     * Set the bean property matching this bean path to the specified value.
     * 
     * @param bean
     *            The root bean of an object graph.
     * @param value
     *            The value to set.
     * @throws Error
     *             If the path does not exist, if the value is of the incorrect
     *             type, or if an error occurs in reflection.
     */
    public void set(Object bean, Object value, Factory factory) throws Error
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
                throw new Error(103);
            }
        }
        catch (PropertyPath.Error e)
        {
            e.add(stringEscape(toString()))
             .add(bean.getClass().getName())
             .add(value == null ? value : value.getClass().getName());
            throw e;
        }
    }
    
    public void set(Object bean, Object value, boolean create) throws Error
    {
        set(bean, value, create ? new CoreFactory() : null);
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
    
    static Property newProperty(String part) throws Error
    {
        int i = eatWhite(part, 0);
        
        if (i == part.length())
        {
            throw new Error(104);
        }

        if (!Character.isJavaIdentifierStart(part.charAt(i)))
        {
            throw new Error(121).add(stringEscape(part))
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
                throw new Error(105, e).add(stringEscape(part));
            }
            catch (NumberFormatException e)
            {
                throw new Error(106, e).add(stringEscape(part));
            }
        }
        
        return new Property(identifier, indices.toArray(new Index[indices.size()]));
    }

    static int newIndex(String part, int i, List<Index> indexes)
        throws Error
    {
        if (part.charAt(i++) != '[')
        {
            throw new Error(107).add(stringEscape(part))
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
                    throw new Error(108).add(stringEscape(part));
                case '\'':
                    // This noop is only to get 100% Corbertura coverage, sorry.
                    part.length();
                case '"':
                    if (ch == quote)
                    {
                        break KEY;
                    }
                    throw new Error(109).add(stringEscape(part))
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
                        throw new Error(110).add(stringEscape(part))
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
                throw new Error(111).add(stringEscape(part))
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

    public final static class Error extends Exception
    {
        private final static long serialVersionUID = 1L;
        
        private final List<Object> listOfArguments = new ArrayList<Object>(); 
        
        private final int code;
        
        public Error(int code)
        {
            this.code = code;
        }
        
        public String getKey()
        {
            return Integer.toString(code);
        }
        
        public Error(int code, Throwable cause)
        {
            super(cause);
            this.code = code;
        }
        
        public Error add(Object argument)
        {
            listOfArguments.add(argument);
            return this;
        }
        
        @Override
        public String getMessage()
        {
            String key = getKey();
            ResourceBundle exceptions = ResourceBundle.getBundle("com.goodworkalan.dspl.exceptions");
            String format;
            try
            {
                format = exceptions.getString(key);
            }
            catch (MissingResourceException e)
            {
                return key;
            }
            try
            {
                return String.format(format, listOfArguments.toArray());
            }
            catch (Throwable e)
            {
                throw new java.lang.Error(key, e);
            }
        }
    }

    public interface Factory
    {
        public Object create(Type type) throws Error;
        
        public Object newBean();
    }
    
    final static class CoreFactory implements Factory
    {
        public Object create(Type type) throws Error
        {
            Object created = null;
            if (type instanceof ParameterizedType)
            {
                created = create((Class<?>) ((ParameterizedType) type).getRawType());
            }
            else if (type instanceof Class)
            {
                created = create((Class<?>) type);
            }
            return created;
        }
        
        public Object create(Class<?> cls) throws Error
        {
            if (!cls.isInterface())
            {
                try
                {
                    return cls.newInstance();
                }
                catch (Exception e)
                {
                    throw new Error(112, e).add(cls.getName());
                }
            }
            else if (SortedMap.class.isAssignableFrom(cls))
            {
                return new TreeMap<Object, Object>();
            }
            else if (Map.class.isAssignableFrom(cls))
            {
                return new HashMap<Object, Object>();
            }
            else if (List.class.isAssignableFrom(cls))
            {
                return new ArrayList<Object>();
            }
            throw new UnsupportedOperationException();
        }
        
        public Object newBean()
        {
            return new ObjectMap();
        }
    }

    final static class Property
    {
        final String name;
        
        final Index[] indexes;
        
        public Property(String name, Index...indexes)
        {
            this.name = name;
            this.indexes = indexes;
        }
        
        public String methodName()
        {
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
    
        public Map<Class<?>, String> readMethodNames()
        {
            Map<Class<?>, String> map = new HashMap<Class<?>, String>();
            map.put(Boolean.class, "is" + methodName());
            map.put(boolean.class, "is" + methodName());
            map.put(Object.class, "get" + methodName());
            return map;
        }
    
        public Set<Method> readMethods(Object bean, int indexesLength)
        {
            Map<Class<?>, String> readerNames = readMethodNames();
            Set<Method> readers = new HashSet<Method>();
            for (Method method : bean.getClass().getMethods())
            {
                if (readerNames.values().contains(method.getName()))
                {
                    for (Class<?> result : readerNames.keySet())
                    {
                        if (result.isAssignableFrom(toClass(method.getGenericReturnType()))
                                && readerNames.get(result).equals(method.getName()))
                        {
                            readers.add(method);
                        }
                    }
                }
            }
            Map<Integer, Method> priority = new TreeMap<Integer, Method>(Collections.reverseOrder());
            METHODS: for (Method method : readers)
            {
                Type[] types = method.getGenericParameterTypes();
                if (types.length <= indexesLength)
                {
                    for (int i = 0; i < types.length; i++)
                    {
                        Class<?> cls = toClass(types[i]);
                        if (!indexes[i].indexedBy(cls))
                        {
                            continue METHODS;
                        }
                    }
                    priority.put(types.length, method);
                }
            }
            return new LinkedHashSet<Method>(priority.values());
        }
    
        boolean create(Object bean, Type type, int indexLength, Factory factory) throws Error
        {
            Method method = explicitSet(bean, type, indexLength);
            if (method != null)
            {
                Object value = factory.create(type);
                if (value == null)
                {
                    throw new Error(113);
                }
                set(method, bean, value);
                return true;
            }
            return false;
        }
        
        public Object get(Object bean, Factory factory) throws Error
        {
            return get(bean, indexes.length, factory);
        }
    
        public Object get(Object bean, int indexesLength, Factory factory) throws Error
        {
            if (bean instanceof Map)
            {
                Map<Object, Object> map = toMap(bean);
                if (indexes.length == 0)
                {
                    Object value = map.get(name);
                    if (value == null && factory != null)
                    {
                        value = factory.newBean();
                        map.put(name, value);
                    }
                    return value;
                }
                Object container = null;
                Object object = map.get(name);
                for (int i = 0; i < indexes.length; i++)
                {
                    if (object == null)
                    {
                        object = factory.create(indexes[i].getRawType());
                        if (i == 0)
                        {
                            toMap(bean).put(name, object);
                        }
                        else
                        {
                            indexes[i - 1].set(indexes[i - 1].getRawType(), container, object);
                        }
                    }
                    container = object;
                }
                return indexes[indexes.length - 1].get(container.getClass().getGenericSuperclass(), container, factory);
            }

            Set<Method> readers = readMethods(bean, indexesLength);
            Object object = null;
            Iterator<Method> methods = readers.iterator();
            while (object == null && methods.hasNext())
            {
                Method method = methods.next();
                Object[] args = new Object[method.getParameterTypes().length];
                for (int i = 0; i < args.length; i++)
                {
                    args[i] = indexes[i].getIndex();
                }
                try
                {
                    object = method.invoke(bean, args);
                }
                catch (Exception e)
                {
                    throw new Error(114, e);
                }
                if (object == null && factory != null && create(bean, method.getGenericReturnType(), args.length, factory))
                {
                    object = get(bean, args.length, factory);
                }
                Type type = method.getGenericReturnType();
                for (int i = args.length; object != null && i < indexesLength; i++)
                {
                    try
                    {
                        object = indexes[i].get(type, object, factory);
                        type = indexes[i].typeOf(type);
                    }
                    catch (PropertyPath.Error e)
                    {
                        e.add(stringEscape(toString()))
                         .add(stringEscape(toString(i)));
                        throw e;
                    }
                }
            }
            return object;
        }
        
        public Type typeOf(Object bean) throws Error
        {
            return typeOf(bean, indexes.length);
        }

        public Type typeOf(Object bean, int indexesLength) throws Error
        {
            Set<Method> readers = readMethods(bean, indexesLength);
            Type type = null;
            Iterator<Method> methods = readers.iterator();
            while (type == null && methods.hasNext())
            {
                Method method = methods.next();
                int args = method.getParameterTypes().length;
                type = method.getGenericReturnType();
                for (int i = args; i < indexesLength; i++)
                {
                    type = indexes[i].typeOf(type);
                }
            }
            return type;
        }
        
        public void set(Object bean, Object value, Factory factory) throws Error
        {
            try
            {
                if (bean instanceof Map)
                {
                    if (indexes.length == 0)
                    {
                        toMap(bean).put(name, value);
                    }
                    else
                    {
                        Object container = null;
                        Object object = toMap(bean).get(name);
                        for (int i = 0; i < indexes.length; i++)
                        {
                            if (object == null)
                            {
                                object = factory.create(indexes[i].getRawType());
                                if (i == 0)
                                {
                                    toMap(bean).put(name, object);
                                }
                                else
                                {
                                    indexes[i - 1].set(indexes[i - 1].getRawType(), container, value);
                                }
                            }
                            container = object;
                        }
                        indexes[indexes.length - 1].set(container.getClass().getGenericSuperclass(), container, value);
                    }
                }
                else
                {
                    Method method = explicitSet(bean, value == null ? null : value.getClass(), indexes.length);
                    if (method == null && indexes.length != 0)
                    {
                        Object object = get(bean, indexes.length - 1, factory);
                        if (object != null)
                        {
                            Type type = typeOf(bean, indexes.length - 1);
                            indexes[indexes.length - 1].set(type, object, value);
                        }
                    }
                    else if (method == null)
                    {
                        throw new Error(115);
                    }
                    else
                    {
                        set(method, bean, value);
                    }
                }
            }
            catch (PropertyPath.Error e)
            {
                e.add(stringEscape(toString()))
                 .add(bean.getClass().getName())
                 .add(value == null ? value : value.getClass().getName());
                throw e;
            }
        }
    
        @Override
        public String toString()
        {
            return toString(indexes.length);
        }
        
        public String toString(int indexCount)
        {
            StringBuilder newString = new StringBuilder();
            newString.append(name);
            for (int i = 0; i < indexCount; i++)
            {
                newString.append(indexes[i].toString());
            }
            return newString.toString();
        }

        void set(Method method, Object bean, Object value) throws Error
        {
            Object[] args = new Object[method.getParameterTypes().length];
            for (int i = 0; i < args.length - 1; i++)
            {
                args[i] = indexes[i].getIndex();
            }
            args[args.length - 1] = value;
            try
            {
                method.invoke(bean, args);
            }
            catch (Exception e)
            {
                throw new Error(116, e);
            }
        }
        
        Method explicitSet(Object bean, Type type, int indexLength) throws Error
        {
            Class<?> cls = toClass(type);
            Set<Method> writers = new HashSet<Method>();
            String methodName = "set" + methodName();
            METHOD: for (Method method : bean.getClass().getMethods())
            {
                Type[] types = method.getGenericParameterTypes();
                if (method.getName().equals(methodName)
                    && types.length == indexLength + 1
                    && (cls == null || cls.isAssignableFrom(toClass(types[indexLength]))))
                {
                    for (int i = 0; i < indexLength; i++)
                    {
                        if (!indexes[i].indexedBy(toClass(types[i])))
                        {
                            continue METHOD;
                        }
                    }
                    writers.add(method);
                }
            }
            if (writers.size() == 1)
            {
                return writers.iterator().next();
            }
            return null;
        }
    }

    interface Index
    {
        public Class<?> getRawType();

        public boolean indexedBy(Class<?> cls);
        
        public Object getIndex();

        public Type typeOf(Type type) throws Error;
        
        public Object get(Type type, Object container, Factory factory) throws Error;
        
        public void set(Type type, Object container, Object value) throws Error;
    }
    
    final static class ObjectList extends ArrayList<Object>
    {
        private static final long serialVersionUID = 1L;
    }

    final static class ListIndex implements Index
    {
        private final int index;
        
        public ListIndex(int index)
        {
            this.index = index;
        }
        
        public Class<?> getRawType()
        {
            return ObjectList.class;
        }

        public Object getIndex()
        {
            return index;
        }
        
        public boolean indexedBy(Class<?> cls)
        {
            return int.class.isAssignableFrom(cls) || Integer.class.isAssignableFrom(cls);
        }

        public Type typeOf(Type type) throws Error
        {
            if (type instanceof ParameterizedType)
            {
                ParameterizedType parameterized = (ParameterizedType) type;
                if (((Class<?>) parameterized.getRawType()).isAssignableFrom(List.class))
                {
                    return parameterized.getActualTypeArguments()[0];
                }
            }
            return null;
        }
        
        @SuppressWarnings("unchecked")
        private List<Object> toList(Object object)
        {
            return (List) object;
        }

        // TODO Why does get take the parameter type, but set takes the list type?
        public Object get(Type type, Object container, Factory factory) throws Error
        {
            Object got = null;
            List<Object> list = toList(container);
            if (index < list.size())
            {
                got = list.get(index);
            }
            if (got == null && factory != null)
            {
                got = factory.create(type);
                if (got == null)
                {
                    throw new Error(117).add(type);
                }
                list.add(index, got);
            }
            return got;
        }
        
        public void set(Type type, Object container, Object value) throws Error
        {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            if (value == null || toClass(types[0]).isAssignableFrom(value.getClass()))
            {
                toList(container).add(index, value);
            }
            else
            {
                throw new Error(118).add(type);
            }
        }
        
        @Override
        public String toString()
        {
            return "[" + index + "]";
        }
    }
    
    final static class ObjectMap extends HashMap<Object, Object>
    {
        private static final long serialVersionUID = 1L;
    }
    
    final static class MapIndex implements Index
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
        
        public Object getIndex()
        {
            return index;
        }
        
        public boolean indexedBy(Class<?> cls)
        {
            return String.class.isAssignableFrom(cls);
        }

        public Type typeOf(Type type) throws Error
        {
            if (Map.class.isAssignableFrom(toClass(type)))
            {
                return ((ParameterizedType) type).getActualTypeArguments()[1];
            }
            return null;
        }
        
        public Object get(Type type, Object container, Factory factory) throws Error
        {
            Map<Object, Object> map = toMap(container);
            Object got = map.get(index);
            if (got == null && factory != null)
            {
                Type[] types = ((ParameterizedType) type).getActualTypeArguments();
                got = factory.create(types[1]);
                if (got == null)
                {
                    throw new Error(119).add(types[1]);
                }
                map.put(index, got);
            }
            return got;
        }
        
        public void set(Type type, Object container, Object value) throws Error
        {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            if (value == null || toClass(types[1]).isAssignableFrom(value.getClass()))
            {
                toMap(container).put(index, value);
            }
            else
            {
                throw new Error(120).add(type);
            }
        }
        
        @Override
        public String toString()
        {
            return "[" + index.replaceAll("['\t\b\r\n\f]", "\\($1)") + "]";
        }
    }
}