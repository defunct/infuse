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

import com.goodworkalan.dspl.PropertyPath.Error;

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
    public void factory() throws Exception
    {
        PropertyPath.Factory factory = new PropertyPath.CoreFactory();
        assertEquals(factory.create(SortedMap.class).getClass(), TreeMap.class);
        assertEquals(factory.create(Map.class).getClass(), HashMap.class);
        assertEquals(factory.create(List.class).getClass(), ArrayList.class);
    }

    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void notFound() throws Exception
    {
        PropertyPath.Factory factory = new PropertyPath.CoreFactory();
        factory.create(Runnable.class);
    }

    @Test(expectedExceptions=PropertyPath.Error.class)
    public void noDefaultConstructor() throws Exception
    {
        PropertyPath.Factory factory = new PropertyPath.CoreFactory();
        factory.create(Integer.class);
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
            throw new PropertyPath.Error();
        }
        catch (PropertyPath.Error e)
        {
        }
        try
        {
            throw new PropertyPath.Error(new IOException());
        }
        catch (PropertyPath.Error e)
        {
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
        PropertyPath.newProperty(part);
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void badNumericIndexNonAlphaNum() throws PropertyPath.Error
    {
        String part = "a[ 1i ["; 
        PropertyPath.newProperty(part);
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void badIndexAlphaNum() throws PropertyPath.Error
    {
        String part = "a 1"; 
        PropertyPath.newProperty(part);
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void badIndexNonAlphaNum() throws PropertyPath.Error
    {
        String part = "a]"; 
        PropertyPath.newProperty(part);
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
        PropertyPath.newProperty("a['a'a");
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void stringIndexIncomplete() throws PropertyPath.Error
    {
        PropertyPath.newProperty("a['a");
    }

    @Test(expectedExceptions=PropertyPath.Error.class)
    public void stringIndexMismatchQuotes() throws PropertyPath.Error
    {
        PropertyPath.newProperty("a['a\"]");
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void stringIndexBadEscape() throws PropertyPath.Error
    {
        PropertyPath.newProperty("a['\\a']");
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void stringIndexWithZero() throws PropertyPath.Error
    {
        PropertyPath.newProperty("a['\0']");
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void emptyString() throws PropertyPath.Error
    {
        PropertyPath.newProperty("");
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void nonJavaIdentifierStart() throws PropertyPath.Error
    {
        PropertyPath.newProperty("1");
    }
    
    @Test
    public void setListProperty() throws PropertyPath.Error
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
    public void basListSetType() throws PropertyPath.Error
    {
        PropertyPath path = new PropertyPath("stringListList[0]");
        Widget widget = new Widget();
        path.set(widget, "A", true);
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void cannotConstructListValue() throws Error
    {
        PropertyPath.Factory factory = mock(PropertyPath.Factory.class);
        PropertyPath path = new PropertyPath("stringListList[0][0]");
        Widget widget = new Widget();
        widget.setStringListList(new ArrayList<List<String>>());
        path.set(widget, "foo", factory);
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
        path.set(widget, "A", true);
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void cannotConstructMapValue() throws PropertyPath.Error
    {
        PropertyPath.Factory factory = mock(PropertyPath.Factory.class);
        PropertyPath path = new PropertyPath("stringMapMap['bar']['baz']");
        Widget widget = new Widget();
        widget.setStringMapMap(new HashMap<String, Map<String,String>>());
        path.set(widget, "foo", factory);
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void noSuchSetGetterMethod() throws PropertyPath.Error
    {
        PropertyPath path = new PropertyPath("foo.bar");
        path.set(new Widget(), "foo", false);
        path.set(new Widget(), "foo", true);
    }
    
    @Test(expectedExceptions=PropertyPath.Error.class)
    public void noSuchSetMethod() throws PropertyPath.Error
    {
        PropertyPath path = new PropertyPath("foo");
        path.set(new Widget(), "foo", true);
    }
    
    @Test
    public void mapInsteadOfBean() throws PropertyPath.Error
    {
        Map<Object, Object> root = new HashMap<Object, Object>();
        PropertyPath path = new PropertyPath("foo");
        path.set(root, "bar", true);
        assertEquals(root.get("foo"), "bar");
        assertEquals(path.get(root), "bar");
        
        path = new PropertyPath("bar[0]");
        path.set(root, "bar", true);
        assertEquals(path.get(root), "bar");
    }
}
