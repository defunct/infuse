package com.goodworkalan.dspl;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.testng.annotations.Test;

public class PropertyPathTest
{
    @Test public void constructor() throws PropertyPath.Error 
    {
        new PropertyPath("name");
    }
    
    @Test public void get() throws PropertyPath.Error
    {
        PropertyPath path = new PropertyPath("string");
        Widget widget = new Widget();
        widget.setString("foo");
        assertEquals(path.get(widget), "foo");
    }
    
    @Test public void set() throws PropertyPath.Error
    {
        PropertyPath path = new PropertyPath("string");
        Widget widget = new Widget();
        path.set(widget, "foo", true);
        assertEquals(widget.getString(), "foo");
    }
    
    @Test public void getChild() throws PropertyPath.Error
    {
        PropertyPath path = new PropertyPath("widget.string");
        
        Widget widget = new Widget();
        widget.setString("foo");
        Widget parent = new Widget();
        parent.setWidget(widget);
        
        assertEquals(path.get(parent), "foo");
    }
    
    @Test public void setChild() throws PropertyPath.Error
    {
        PropertyPath path = new PropertyPath("widget.string");
        
        Widget widget = new Widget();
        
        path.set(widget, "foo", true);
        assertEquals(widget.getWidget().getString(), "foo");
    }
    
    @Test
    public void typeOf() throws PropertyPath.Error
    {    
        PropertyPath path = new PropertyPath("widget.string");
        
        Widget widget = new Widget();
        assertNull(path.typeOf(widget, false));
        assertEquals(path.typeOf(widget, true), String.class);
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void typeOfBadPath() throws PropertyPath.Error
    {    
        PropertyPath path = new PropertyPath("widget.foo");
        Widget widget = new Widget();
        try
        {
            path.typeOf(widget, true);
        }
        catch (PropertyPath.Error e)
        {
            assertEquals(e.getMessage(), "Unable to navigate path \"widget.foo\" in bean of class com.goodworkalan.dspl.Widget in order to determine type.");
            throw e;
        }
    }

    @Test
    public void factory() throws Exception
    {
        PropertyPath.Factory factory = new PropertyPath.CoreFactory();
        assertEquals(factory.create(SortedMap.class).getClass(), TreeMap.class);
        assertEquals(factory.create(Map.class).getClass(), HashMap.class);
        assertEquals(factory.create(List.class).getClass(), ArrayList.class);
        assertNull(factory.create(new ArrayList<Object>().getClass().getTypeParameters()[0]));
    }

    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void notFound() throws Exception
    {
        PropertyPath.Factory factory = new PropertyPath.CoreFactory();
        try
        {
            factory.create(Runnable.class);
        }
        catch (PropertyPath.Error e)
        {
            assertEquals(e.getMessage(), "foo");
            throw e;
        }
    }

    @Test(expectedExceptions=PropertyPath.Error.class)
    public void noDefaultConstructor() throws Exception
    {
        PropertyPath.Factory factory = new PropertyPath.CoreFactory();
        try
        {
            factory.create(Integer.class);
        }
        catch (PropertyPath.Error e)
        {
            // TODO Maybe there are two 
            assertEquals(e.getMessage(), "Unable to create class of type java.lang.Integer. No default constructor.");
            throw e;
        }
    }

    @Test
    public void listIndex() throws Exception
    {
        PropertyPath.Index index = new PropertyPath.ListIndex(0);
        assertNull(index.typeOf(Object.class));
        Widget bean = new Widget();
        PropertyPath.Property property = new PropertyPath.Property("stringMapMap");
        assertNull(index.typeOf(property.typeOf(bean)));
        property = new PropertyPath.Property("stringListList");
        Type type = property.typeOf(bean);
        type = index.typeOf(type);
        type = index.typeOf(type);
        assertEquals(String.class, type);
        
        bean.setStringListList(new ArrayList<List<String>>());

        PropertyPath.Factory factory = new PropertyPath.CoreFactory();
        type = property.typeOf(bean);
        Object list = property.get(bean, null);
        type = index.typeOf(type);
        list = index.get(type, list, factory);
        type = index.typeOf(type);
        assertEquals(index.get(type, list, factory), "");
        assertEquals(((Widget) bean).getStringListList().get(0).get(0), "");
    }
    
    @Test
    public void mapIndex() throws Exception
    {
        PropertyPath.Index index = new PropertyPath.MapIndex("foo");
        assertNull(index.typeOf(Object.class));
        Object bean = new Widget();
        PropertyPath.Property property = new PropertyPath.Property("stringListList");
        assertNull(index.typeOf(property.typeOf(bean)));
        property = new PropertyPath.Property("stringMapMap");
        Type type = property.typeOf(bean);
        type = index.typeOf(type);
        type = index.typeOf(type);
        assertEquals(String.class, type);
    }
    
    @Test
    public void error()
    {
        try
        {
            throw new PropertyPath.Error(10001);
        }
        catch (PropertyPath.Error e)
        {
        }
        try
        {
            throw new PropertyPath.Error(10001, new IOException());
        }
        catch (PropertyPath.Error e)
        {
        }
    }

    @Test(expectedExceptions=Error.class)
    public void errorBadFormat()
    {
        try
        {
            throw new PropertyPath.Error(99999);
        }
        catch (PropertyPath.Error e)
        {
            e.getMessage();
        }
    }
    
    @Test
    public void toClass()
    {
        Type type = null;
        assertNull(PropertyPath.toClass(type));
    }
    
    @Test
    public void eatWhite()
    {
        String part = "   ab";
        int i = 0;
        i = PropertyPath.eatWhite(part, i);
        assertEquals(i, 3);
        i = PropertyPath.eatWhite(part, ++i);
        assertEquals(i, 4);
        i = PropertyPath.eatWhite(part, ++i);
        assertEquals(i, 5);
    }
    
    @Test
    public void numericIndex() throws PropertyPath.Error
    {
        List<PropertyPath.Index> indexes = new ArrayList<PropertyPath.Index>();

        String part = "[ 1 ] "; 
        int i = PropertyPath.newIndex(part, 0, indexes);
        
        assertEquals(indexes.get(0).getClass(), PropertyPath.ListIndex.class);
        assertEquals(part.length(), i);
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void badNumericIndexAlphaNum() throws PropertyPath.Error
    {
        String part = "a[ 1i ] "; 
        try
        {
            new PropertyPath(part);
        }
        catch (PropertyPath.Error e)
        {
            assertEquals(e.getMessage(), "Unable to parse path \"a[ 1i ] \" part \"a[ 1i ] \". List index is not an integer.");
            throw e;
        }
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void badNumericIndexNonAlphaNum() throws PropertyPath.Error
    {
        String part = "a[ 1i ["; 
        try
        {
            new PropertyPath(part);
        }
        catch (PropertyPath.Error e)
        {
            assertEquals(e.getMessage(), "Unable to parse path \"a[ 1i [\" part \"a[ 1i [\". List index is not an integer.");
            throw e;
        }
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void badIndexAlphaNum() throws PropertyPath.Error
    {
        String part = "a \"";
        try
        {
            new PropertyPath(part);
        }
        catch (PropertyPath.Error e)
        {
            assertEquals(e.getMessage(), "Unable to parse path \"a \\\"\" part \"a \\\"\". Expecting '[' but got '\"'.");
            throw e;
        }
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void badIndexNonAlphaNum() throws PropertyPath.Error
    {
        String part = "a]";
        try
        {
            new PropertyPath(part);
        }
        catch (PropertyPath.Error e)
        {
            assertEquals(e.getMessage(), "Unable to parse path \"a]\" part \"a]\". Expecting '[' but got ']'.");
            throw e;
        }
    }
    
    private void assertName(String part, String name) throws PropertyPath.Error
    {
        PropertyPath.Property property = PropertyPath.newProperty(part);
        PropertyPath.Index index = property.indexes[0];
        assertEquals(((PropertyPath.MapIndex) index).index, name);
    }
    
    @Test
    public void stringIndex() throws PropertyPath.Error
    {
        assertName("a ['a']", "a");
        assertName("a ['abcdef']", "abcdef");
        assertName("a ['\\'']", "'");
        assertName("a [\"\\\"\"]", "\"");
        assertName("a ['\\b']", "\b");
        assertName("a ['\\t']", "\t");
        assertName("a ['\\f']", "\f");
        assertName("a ['\\r']", "\r");
        assertName("a ['\\n']", "\n");
        assertName("a ['\\u0041']", "A");
        assertName("a ['\\x41']", "A");
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void stringIndexBadClosingBracket() throws PropertyPath.Error
    {
        try
        {
            new PropertyPath("a['a'a");
        }
        catch (PropertyPath.Error e)
        {
            assertEquals(e.getMessage(), "Unable to parse path \"a['a'a\" part \"a['a'a\". Expecting ']' but got 'a'.");
            throw e;
        }
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void stringIndexIncomplete() throws PropertyPath.Error
    {
        try
        {
            new PropertyPath("a['a");
        }
        catch (PropertyPath.Error e)
        {
            assertEquals(e.getMessage(), "Unable to parse path \"a['a\" part \"a['a\". Unexpected end of string.");
            throw e;
        }
    }

    @Test(expectedExceptions=PropertyPath.Error.class)
    public void stringIndexMismatchQuotes() throws PropertyPath.Error
    {
        try
        {
            new PropertyPath("a['a\"]");
        }
        catch (PropertyPath.Error e)
        {
            assertEquals(e.getMessage(), "Unable to parse path \"a['a\\\"]\" part \"a['a\\\"]\". Unnecessary escape of quote character '\"' in string quoted with '\\''.");
            throw e;
        }
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void stringIndexBadEscape() throws PropertyPath.Error
    {
        try
        {
            new PropertyPath("a['\\a']");
        }
        catch (PropertyPath.Error e)
        {
            assertEquals(e.getMessage(), "Unable to parse path \"a['\\a']\" part \"a['\\a']\". Invalid character escape sequence '\\a'.");
            throw e;
        }
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void stringIndexWithZero() throws PropertyPath.Error
    {
        try
        {
            new PropertyPath("a['\0']");
        }
        catch (PropertyPath.Error e)
        {
            assertEquals(e.getMessage(), "Unable to parse path \"a['\\0']\" part \"a['\\0']\". Invalid character '\\0'.");
            throw e;
        }
    }
    
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void nullString() throws PropertyPath.Error
    {
        new PropertyPath(null);
    }

    @Test(expectedExceptions=PropertyPath.Error.class)
    public void emptyString() throws PropertyPath.Error
    {
        try
        {
            new PropertyPath("");
        }
        catch (PropertyPath.Error e)
        {
            assertEquals(e.getMessage(), "Unable to parse empty string.");
            throw e;
        }
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void nonJavaIdentifierStart() throws PropertyPath.Error
    {
        try
        {
            new PropertyPath("1");
        }
        catch (PropertyPath.Error e)
        {
            assertEquals(e.getMessage(), "Unable to parse path \"1\" part \"1\". First character '1' is not a valid Java start identifier character.");
            throw e;
        }
    }
    
    @Test
    public void listProperty() throws PropertyPath.Error
    {
        PropertyPath path = new PropertyPath("stringListList[0]");
     
        Widget widget = new Widget();
        assertNull(path.get(widget));

        Type type = path.typeOf(widget, false);
        assertEquals(((ParameterizedType) type).getRawType(), List.class);
    
        widget.setStringListList(new ArrayList<List<String>>());
        
        Object list = new ArrayList<String>();
        path.set(widget, list, false);
        assertSame(path.get(widget), list);

        path.set(widget, null, false);
        assertNull(path.get(widget));
        
        widget = new Widget();
        path.set(widget, list, true);
        assertSame(path.get(widget), list);
        
        widget = new Widget();
        path = new PropertyPath("stringListList[0][0]");
        path.set(widget, "foo", true);
        assertEquals(path.get(widget), "foo");
        
        path = new PropertyPath("string[0][0]");
        assertEquals(path.get(widget), "foo");
        path.set(widget, "oof", true);
        assertEquals(path.get(widget), "oof");
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void badListSetType() throws PropertyPath.Error
    {
        PropertyPath path = new PropertyPath("stringListList[0]");
        Widget widget = new Widget();
        try
        {
            path.set(widget, "A", true);
        }
        catch (PropertyPath.Error e)
        {
            assertEquals(e.getMessage(), "Unable to set value of class java.lang.String to list of type java.util.List<java.util.List<java.lang.String>> for path \"stringListList[0]\" in bean of class com.goodworkalan.dspl.Widget.");
            throw e;
        }
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void cannotConstructListValue() throws PropertyPath.Error
    {
        PropertyPath.Factory factory = mock(PropertyPath.Factory.class);
        PropertyPath path = new PropertyPath("stringListList[0][0]");
        Widget widget = new Widget();
        widget.setStringListList(new ArrayList<List<String>>());
        try
        {
            path.set(widget, "foo", factory);
        }
        catch (PropertyPath.Error e)
        {
            assertEquals(e.getMessage(), "Unable to create path \"stringListList[0][0]\" part \"stringListList[0][0]\" in bean java.util.List<java.util.List<java.lang.String>>. Unable to set part \"stringListList\" with type of java.util.List<java.util.List<java.lang.String>> with value of type java.lang.String.");
            throw e;
        }
    }
    
    @Test
    public void mapProperty() throws PropertyPath.Error
    {
        PropertyPath path = new PropertyPath("stringMapMap['bar']");
        
        Widget widget = new Widget();
        assertNull(path.get(widget));

        Type type = path.typeOf(widget, false);
        assertEquals(((ParameterizedType) type).getRawType(), Map.class);
        
        widget.setStringMapMap(new HashMap<String, Map<String,String>>());
        
        Object map = new HashMap<String, String>();
        path.set(widget, map, false);
        assertSame(path.get(widget), map);

        path.set(widget, null, false);
        assertNull(path.get(widget));
        
        widget = new Widget();
        path.set(widget, map, true);
        assertSame(path.get(widget), map);
     
        widget = new Widget();
        path = new PropertyPath("stringMapMap['bar']['baz']");
        path.set(widget, "foo", true);
        assertEquals(path.get(widget), "foo");
        
        path = new PropertyPath("string['bar']['baz']");
        assertEquals(path.get(widget), "foo");
        path.set(widget, "oof", true);
        assertEquals(path.get(widget), "oof");
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void badMapSetType() throws PropertyPath.Error
    {
        PropertyPath path = new PropertyPath("stringMapMap['bar']");
        Widget widget = new Widget();
        try
        {
            path.set(widget, "A", true);
        }
        catch (PropertyPath.Error e)
        {
            assertEquals(e.getMessage(), "Unable to set value of class java.lang.String to map of type java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.String>> for path \"stringMapMap[bar]\" in bean of class com.goodworkalan.dspl.Widget.");
            throw e;
        }
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void cannotConstructMapValue() throws PropertyPath.Error
    {
        PropertyPath.Factory factory = mock(PropertyPath.Factory.class);
        PropertyPath path = new PropertyPath("stringMapMap['bar']['baz']");
        Widget widget = new Widget();
        widget.setStringMapMap(new HashMap<String, Map<String,String>>());
        try
        {
            path.set(widget, "foo", factory);
        }
        catch (PropertyPath.Error e)
        {
            assertEquals(e.getMessage(), "Unable to create map property of type java.util.Map<java.lang.String, java.lang.String>.");
            throw e;
        }
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void noSuchSetGetterMethod() throws PropertyPath.Error
    {
        PropertyPath path = new PropertyPath("foo.bar");
        path.set(new Widget(), "foo", false);
        try
        {
            path.set(new Widget(), "foo", true);
        }
        catch (PropertyPath.Error e)
        {
            assertEquals(e.getMessage(), "Unable to navigate path \"foo.bar\" in bean of class com.goodworkalan.dspl.Widget  in order to set value of class java.lang.String.");
            throw e;
        }
    }
    
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void nullGet() throws PropertyPath.Error
    {
        new PropertyPath("foo").get(null);
    }
    
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void nullSet() throws PropertyPath.Error
    {
        new PropertyPath("foo").set(null, null, false);
    }
    
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void nullTypeOf() throws PropertyPath.Error
    {
        new PropertyPath("foo").typeOf(null, false);
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void noSuchSetMethod() throws PropertyPath.Error
    {
        PropertyPath path = new PropertyPath("foo");
        try
        {
            path.set(new Widget(), "foo", true);
        }
        catch (PropertyPath.Error e)
        {
            assertEquals(e.getMessage(), "Unable to navigate path \"foo\" in bean of class com.goodworkalan.dspl.Widget in order to set value of class java.lang.String.");
            throw e;
        }
    }
    
    @Test
    public void mapPropertyCreate() throws PropertyPath.Error
    {
        Map<Object, Object> root = new HashMap<Object, Object>();
        PropertyPath path = new PropertyPath("foo");
        path.set(root, "bar", true);
        assertEquals(root.get("foo"), "bar");
        assertEquals(path.get(root), "bar");
    }
    
    @Test
    public void mapListCreate() throws PropertyPath.Error
    {
        Map<Object, Object> root = new HashMap<Object, Object>();
        PropertyPath path = new PropertyPath("bar[0]");
        path.set(root, "bar", true);
        assertEquals(path.get(root), "bar");
    }
    
    @Test
    public void mapMapCreate() throws PropertyPath.Error
    {
        Map<Object, Object> root = new HashMap<Object, Object>();
        PropertyPath path = new PropertyPath("bar['baz']");
        path.set(root, "foo", true);
        assertEquals(path.get(root), "foo");
    }

    @Test
    public void mapBeanCreate() throws PropertyPath.Error
    {
        Map<Object, Object> root = new HashMap<Object, Object>();
        PropertyPath path = new PropertyPath("bar.baz");
        path.set(root, "foo", true);
        assertEquals(path.get(root), "foo");
    }
}
