package com.goodworkalan.infuse;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.testng.annotations.Test;

public class PathTest
{
    @Test public void constructor() throws PathException 
    {
        Infusion.getInstance(new Object());
    }
    
    @Test public void get() throws PathException
    {
        Diffusion diffusion = new Diffusion("string");
        Widget widget = new Widget();
        widget.setString("foo");
        assertEquals(diffusion.get(widget), "foo");
    }
    
    @Test public void set() throws PathException
    {
        Tree tree = new Tree();
        tree.set("string", "foo");
        Widget widget = new Widget();
        Infusion infusion = Infusion.getInstance(widget);
        infusion.infuse(tree);
        assertEquals(widget.getString(), "foo");
    }
    
    @Test
    public void self() throws PathException
    {
        Diffusion diffusion = new Diffusion("this.widget.this.string.this");
        
        Widget widget = new Widget();
        widget.setString("foo");
        Widget parent = new Widget();
        parent.setWidget(widget);
        
        assertEquals(diffusion.get(parent), "foo");
        
    }
    
    @Test public void getChild() throws PathException
    {
        Diffusion diffusion = new Diffusion("widget.string");
        
        Widget widget = new Widget();
        widget.setString("foo");
        Widget parent = new Widget();
        parent.setWidget(widget);
        
        assertEquals(diffusion.get(parent), "foo");
    }
    
    @Test(enabled = false) public void setChild() throws PathException
    {
        Widget widget = new Widget();
        Infusion infusion = Infusion.getInstance(widget);
        
        infusion.infuse(new Tree().add("widget.string", "foo"));
        assertEquals(widget.getWidget().getString(), "foo");
    }

    @Test
    public void factory() throws Exception
    {
        ObjectFactory factory = new BasicObjectFactory();
        assertEquals(factory.create(SortedMap.class, null, null).getClass(), TreeMap.class);
        assertEquals(factory.create(Map.class, null, null).getClass(), LinkedHashMap.class);
        assertEquals(factory.create(List.class, null, null).getClass(), ArrayList.class);
        assertNull(factory.create(new ArrayList<Object>().getClass().getTypeParameters()[0], null, null));
    }

    // TODO Throw a FactoryException.
    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void notFound() throws Exception
    {
        ObjectFactory factory = new BasicObjectFactory();
        factory.create(Runnable.class, null, null);
    }

    @Test(expectedExceptions=FactoryException.class)
    public void noDefaultConstructor() throws Exception
    {
        ObjectFactory factory = new BasicObjectFactory();
        try
        {
            factory.create(Integer.class, null, null);
        }
        catch (FactoryException e)
        {
            assertEquals(e.getMessage(), "Unable to create class of type java.lang.Integer. No default constructor.");
            throw e;
        }
    }

    @Test
    public void pathException()
    {
        try
        {
            throw new PathException(10001);
        }
        catch (PathException e)
        {
        }
        try
        {
            throw new PathException(10001, new IOException());
        }
        catch (PathException e)
        {
        }
        assertEquals(new PathException(999999).getMessage(), "999999");
    }

    @Test(expectedExceptions=Error.class)
    public void errorBadFormat()
    {
        try
        {
            throw new PathException(99999);
        }
        catch (PathException e)
        {
            e.getMessage();
        }
    }
    
    @Test
    public void toClass()
    {
        Type type = null;
        assertNull(Objects.toClass(type));
    }
    
    @Test(expectedExceptions=ParseException.class)
    public void badNumericIndexAlphaNum() throws PathException
    {
        String part = "a[ 1i ] "; 
        try
        {
            new Path(part, false);
        }
        catch (ParseException e)
        {
            assertEquals(e.getMessage(), "Unable to parse path \"a[ 1i ] \". Invalid index specification at index 1.");
            throw e;
        }
    }
    
    @Test(expectedExceptions=ParseException.class)
    public void badNumericIndexNonAlphaNum() throws PathException
    {
        String part = "a[ 1i ["; 
        try
        {
            new Path(part, false);
        }
        catch (ParseException e)
        {
            assertEquals(e.getMessage(), "Unable to parse path \"a[ 1i [\". Invalid index specification at index 1.");
            assertEquals(e.getCode(), 127);
            throw e;
        }
    }
    
    @Test(expectedExceptions=PathException.class)
    public void badIndexAlphaNum() throws PathException
    {
        String part = "a \"";
        try
        {
            new Path(part, false);
        }
        catch (PathException e)
        {
            assertEquals(e.getMessage(), "Unable to parse path \"a \\\"\". Unexpected character '\"' at index 2.");
            throw e;
        }
    }
    
    @Test(expectedExceptions=PathException.class)
    public void badIndexNonAlphaNum() throws PathException
    {
        String part = "a]";
        try
        {
            new Path(part, false);
        }
        catch (PathException e)
        {
            assertEquals(e.getMessage(), "Unable to parse path \"a]\". Unexpected character ']' at index 1.");
            assertEquals(e.getCode(), 129);
            throw e;
        }
    }
    
    private void assertName(String part, String name) throws PathException
    {
        Path property = new Path(part, false);
        assertEquals(property.get(1).getName(), name);
    }
    
    @Test
    public void stringIndex() throws PathException
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
    
    @Test(expectedExceptions=PathException.class)
    public void stringIndexBadClosingBracket() throws PathException
    {
        try
        {
            new Path("a['a'a", false);
        }
        catch (PathException e)
        {
            assertEquals(e.getMessage(), "Unable to parse path \"a['a'a\". Invalid index specification at index 1.");
            assertEquals(e.getCode(), 127);
            throw e;
        }
    }
    
    @Test(expectedExceptions=PathException.class)
    public void stringIndexIncomplete() throws PathException
    {
        try
        {
            new Path("a['a", true);
        }
        catch (PathException e)
        {
            assertEquals(e.getMessage(), "Unable to parse path \"a['a\". Invalid index specification at index 1.");
            assertEquals(e.getCode(), 127);
            throw e;
        }
    }

    @Test(expectedExceptions=PathException.class)
    public void stringIndexMismatchQuotes() throws PathException
    {
        try
        {
            new Path("a['a\"]", true);
        }
        catch (PathException e)
        {
            assertEquals(e.getMessage(), "Unable to parse path \"a['a\\\"]\". Invalid index specification at index 1.");
            throw e;
        }
    }
    
    @Test(expectedExceptions=PathException.class)
    public void stringIndexBadEscape() throws PathException
    {
        try
        {
            new Path("a['\\a']", true);
        }
        catch (PathException e)
        {
            assertEquals(e.getMessage(), "Unable to parse path \"a['\\a']\". Invalid index specification at index 1.");
            throw e;
        }
    }
    
    @Test(expectedExceptions=PathException.class)
    public void stringIndexWithZero() throws PathException
    {
        try
        {
            new Path("a['\0']", true);
        }
        catch (PathException e)
        {
            assertEquals(e.getMessage(), "Unable to parse path \"a['\\0']\". Invalid character '\\0' in index specification \"'\\0'\" at index 1.");
            throw e;
        }
    }
    
    @Test(expectedExceptions=NullPointerException.class)
    public void nullString() throws PathException
    {
        new Path((String) null, true);
    }

    @Test(expectedExceptions=PathException.class)
    public void emptyString() throws PathException
    {
        try
        {
            new Path("", true);
        }
        catch (PathException e)
        {
            assertEquals(e.getMessage(), "Unable to parse path \"\". Invalid identifier specification at index 0.");
            throw e;
        }
    }
    
    @Test(expectedExceptions=PathException.class)
    public void nonJavaIdentifierStart() throws PathException
    {
        try
        {
            new Path("1", true);
        }
        catch (PathException e)
        {
            assertEquals(e.getMessage(), "Unable to parse path \"1\". Invalid identifier specification at index 0.");
            assertEquals(e.getCode(), 125);
            throw e;
        }
    }
    
    @Test(enabled = false, expectedExceptions=PathException.class)
    public void badListSetType() throws PathException
    {
        Widget widget = new Widget();
        Infusion infusion = Infusion.getInstance(widget);
        try
        {
            infusion.infuse(new Tree().add("stringListList[0]", "A"));
        }
        catch (PathException e)
        {
            assertEquals(e.getMessage(), "Unable to set value of class java.lang.String to list of type java.util.List<java.util.List<java.lang.String>> for path \"stringListList[0]\" in bean of class com.goodworkalan.infuse.Widget.");
            throw e;
        }
    }
    
    @Test(enabled = false, expectedExceptions=PathException.class)
    public void cannotConstructListValue() throws PathException
    {
//        ObjectFactory factory = mock(ObjectFactory.class);
        Widget widget = new Widget();
        Infusion path = Infusion.getInstance(widget);
        widget.setStringListList(new ArrayList<List<String>>());
        try
        {
            path.infuse(new Tree().add("stringListList[0][0]", "foo"));
        }
        catch (PathException e)
        {
            assertEquals(e.getMessage(), "Unable to create path \"stringListList[0][0]\" part \"stringListList[0][0]\" in bean class of com.goodworkalan.infuse.Widget. Unable to create type of java.util.List<java.lang.String> to set list index \"stringListList[0]\".");
            throw e;
        }
    }
    
    @Test(enabled = false, expectedExceptions=PathException.class)
    public void badMapSetType() throws PathException
    {
        Widget widget = new Widget();
        Infusion path = Infusion.getInstance(widget);
        try
        {
            path.infuse(new Tree().add("stringMapMap['bar']", "A"));
        }
        catch (PathException e)
        {
            assertEquals(e.getMessage(), "Unable to set value of class java.lang.String to map of type java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.String>> for path \"stringMapMap['bar']\" in bean of class com.goodworkalan.infuse.Widget.");
            throw e;
        }
    }
    
    @Test(enabled = false, expectedExceptions=PathException.class)
    public void cannotConstructMapValue() throws PathException
    {
//        ObjectFactory factory = mock(ObjectFactory.class);
        Widget widget = new Widget();
        Infusion path = Infusion.getInstance(widget);
        widget.setStringMapMap(new HashMap<String, Map<String,String>>());
        try
        {
            path.infuse(new Tree().add("stringMapMap['bar']['baz']", "foo"));
        }
        catch (PathException e)
        {
            assertEquals(e.getMessage(), "Unable to create map property of type java.util.Map<java.lang.String, java.lang.String>.");
            throw e;
        }
    }
    
    @Test(enabled = false, expectedExceptions=IllegalArgumentException.class)
    public void nullGet() throws PathException
    {
        new Diffusion("foo").get(null);
    }
    
    @Test(expectedExceptions=NullPointerException.class)
    public void nullSet() throws PathException
    {
        Infusion.getInstance(null);
    }
    
    @Test(enabled = false, expectedExceptions=PathException.class)
    public void noSuchSetMethod() throws PathException
    {
        Infusion infusion = Infusion.getInstance(new Widget());
        try
        {
            infusion.infuse(new Tree().add("foo", "foo"));
        }
        catch (PathException e)
        {
            assertEquals(e.getMessage(), "Unable to navigate path \"foo\" in bean of class com.goodworkalan.infuse.Widget in order to set value of class java.lang.String.");
            throw e;
        }
    }
    
//    @Test
//    public void mapPropertyCreate() throws PathException
//    {
//        Map<Object, Object> root = new HashMap<Object, Object>();
//        PropertyPath path = new PropertyPath("foo");
//        path.set(root, "bar", true);
//        assertEquals(root.get("foo"), "bar");
//        assertEquals(path.get(root), "bar");
//    }
//    
//    @Test
//    public void mapListCreate() throws PathException
//    {
//        Map<Object, Object> root = new HashMap<Object, Object>();
//        PropertyPath path = new PropertyPath("bar[0]");
//        path.set(root, "bar", true);
//        assertEquals(path.get(root), "bar");
//    }
//    
//    @Test
//    public void mapMapCreate() throws PathException
//    {
//        Map<Object, Object> root = new HashMap<Object, Object>();
//        PropertyPath path = new PropertyPath("bar['baz']");
//        path.set(root, "foo", true);
//        assertEquals(path.get(root), "foo");
//        
//        path = new PropertyPath("baz['bar'].foo");
//        path.set(root, "foo", true);
//
//        path = new PropertyPath("baz['bar'].baz['foo']");
//        path.set(root, "foo", true);
//        path.set(root, "foo", true);
//    }

//    @Test
//    public void mapBeanCreate() throws PathException
//    {
//        Map<Object, Object> root = new HashMap<Object, Object>();
//        PropertyPath path = new PropertyPath("bar.baz");
//        path.set(root, "foo", true);
//        assertEquals(path.get(root), "foo");
//    }
    
    @Test
    public void stringEscape()
    {
        assertEquals(Messages.stringEscape("\b\f\n\r\t\0\1\2\3\4\5\6\7\""), "\"\\b\\f\\n\\r\\t\\0\\1\\2\\3\\4\\5\\6\\7\\\"\"");
    }
    
    @Test
    public void charEscape()
    {
        assertEquals(Messages.charEscape('\''), "'\\''");
        assertEquals(Messages.charEscape('\\'), "'\\\\'");
    }
    
//    @Test
//    public void dotInPath() throws PathException
//    {
//        PropertyList path = new PropertyList("widgetMapMap['.']['.'].number");
//        Widget widget = new Widget();
//        path.set(widget, 1, true);
//        assertEquals(path.get(widget), 1);
//    }

    @Test
    public void stripIndexes() throws PathException
    {
        String path = " foo . bar [1] [   'Hello, World!\\n' ] [1] . baz [100] [  11 ]  ";
        assertEquals(new Path(path, false).withoutIndexes(), "foo.bar.baz");
    }
    
//    @Test(expectedExceptions=PathException.class)
//    public void listIndexFactoryException() throws PathException, FactoryException
//    {
//        PropertyPath path = new PropertyPath("widgetListList[0][0].number");
//        Widget widget = new Widget();
//        path.set(widget, 1, true);
//        ObjectFactory factory = mock(ObjectFactory.class);
//        when(factory.create((Type) anyObject())).thenThrow(new FactoryException(0, null));
//        path = new PropertyPath("widgetListList[1][1].number");
//        try
//        {
//            path.set(widget, 1, factory);
//        }
//        catch (PathException e)
//        {
//            assertEquals(e.getMessage(), "Unable to create path \"widgetListList[1][1].number\" part \"widgetListList[1][1]\" in bean class of com.goodworkalan.infuse.Widget. Unable to create type of java.util.List<com.goodworkalan.infuse.Widget> to set list index \"widgetListList[1]\".");
//            throw e;
//        }
//    }
}
