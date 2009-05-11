package com.goodworkalan.infuse;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.regex.Pattern;

import org.testng.annotations.Test;

import com.goodworkalan.infuse.PathException;
import com.goodworkalan.infuse.Patterns;
import com.goodworkalan.infuse.PropertyGlob;
import com.goodworkalan.infuse.PropertyPath;

public class PropertyGlobTest
{
    @Test
    public void regex()
    {
        assertTrue("a".matches(Patterns.identifier(false)));
        assertFalse("1".matches(Patterns.identifier(false)));
        assertTrue("['foo']".matches(Patterns.stringIndex('\'', false)));
        assertTrue("['\\'']".matches(Patterns.stringIndex('\'', false)));
        assertTrue("['\\b']".matches(Patterns.stringIndex('\'', false)));
        assertFalse("[''']".matches(Patterns.stringIndex('\'', false)));
        assertTrue(Pattern.compile(Patterns.glob()).matcher("a[1]['\\''].b.c[ 12 ]").matches());
    }
    
    @Test
    public void constructor() throws PathException
    {
        new PropertyGlob("foo[*].bar");
    }
    
    @Test(expectedExceptions=NullPointerException.class)
    public void nullGlob() throws PathException
    {
        new PropertyGlob(null);
    }
    
    @Test(expectedExceptions=PathException.class)
    public void invalidGlob() throws PathException
    {
        try
        {
            new PropertyGlob("!");
        }
        catch (PathException e)
        {
            assertEquals(e.getMessage(), "Unable to parse path \"!\". Invalid identifier specification at index 0.");
            throw e;
        }
    }
    
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void nullAll() throws PathException
    {
        new PropertyGlob("a").all(null);
    }
    
    @Test
    public void all() throws PathException
    {
        Widget widget = new Widget();
        new PropertyPath("widget.widgetListList[0][0].widget.number").set(widget, 1, true);
        new PropertyPath("widget.widgetListList[1][0].widget.number").set(widget, 1, true);
        new PropertyPath("widget.widgetListList[1][1].widget.number").set(widget, 1, true);
        new PropertyPath("widget.widgetListList[1][3].widget.number").set(widget, 1, true);

        PropertyGlob glob = new PropertyGlob("widget.widgetListList[0][*].widget.number");
        List<PropertyPath> expansions = glob.all(widget);
        assertEquals(expansions.size(), 1);
        
        for (PropertyPath expansion : expansions)
        {
            assertEquals(expansion.get(widget), 1);
        }
 
        glob = new PropertyGlob("widget.widgetListList[1][*].widget.number");
        expansions = glob.all(widget);
        assertEquals(expansions.size(), 3);
        
        for (PropertyPath expansion : expansions)
        {
            assertEquals(expansion.get(widget), 1);
        }
        
        glob = new PropertyGlob("widget.widgetListList[*][0].widget.number");
        expansions = glob.all(widget);
        assertEquals(expansions.size(), 2);
        
        for (PropertyPath expansion : expansions)
        {
            assertEquals(expansion.get(widget), 1);
        }
        
        glob = new PropertyGlob("widget.widgetListList[*][*].widget.number");
        expansions = glob.all(widget);
        assertEquals(expansions.size(), 4);
        
        for (PropertyPath expansion : expansions)
        {
            assertEquals(expansion.get(widget), 1);
        }
        
        new PropertyPath("widget.widgetMapMap['foo']['foo'].widget.number").set(widget, 1, true);
        new PropertyPath("widget.widgetMapMap['foo']['bar']").set(widget, null, true);
        new PropertyPath("widget.widgetMapMap['bar']['foo'].widget.number").set(widget, 1, true);
        new PropertyPath("widget.widgetMapMap['bar']['bar'].widget.number").set(widget, 1, true);
        new PropertyPath("widget.widgetMapMap['bar']['baz'].widget.number").set(widget, 1, true);

        glob = new PropertyGlob("widget.widgetMapMap['foo'][*].widget.number");
        expansions = glob.all(widget);
        assertEquals(expansions.size(), 1);
        
        for (PropertyPath expansion : expansions)
        {
            assertEquals(expansion.get(widget), 1);
        }
 
        glob = new PropertyGlob("widget.widgetMapMap['bar'][*].widget.number");
        expansions = glob.all(widget);
        assertEquals(expansions.size(), 3);
        
        for (PropertyPath expansion : expansions)
        {
            assertEquals(expansion.get(widget), 1);
        }

        glob = new PropertyGlob("widget.widgetMapMap[*]['foo'].widget.number");
        expansions = glob.all(widget);
        assertEquals(expansions.size(), 2);
        
        for (PropertyPath expansion : expansions)
        {
            assertEquals(expansion.get(widget), 1);
        }

        glob = new PropertyGlob("widget.widgetMapMap[*][*].widget.number");
        expansions = glob.all(widget);
        assertEquals(expansions.size(), 4);
        
        for (PropertyPath expansion : expansions)
        {
            assertEquals(expansion.get(widget), 1);
        }
        
        glob = new PropertyGlob("widget.number[*]");
        expansions = glob.all(widget);
        assertEquals(expansions.size(), 0);
    }
}
