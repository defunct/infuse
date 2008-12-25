package com.goodworkalan.dspl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import org.testng.annotations.Test;

public class PropertyGlobTest
{
    @Test
    public void regex()
    {
        assertTrue("".matches(Patterns.SKIPWHITE));
        assertFalse(" a".matches(Patterns.SKIPWHITE));
        assertTrue("a".matches(Patterns.IDENTIFIER));
        assertFalse("1".matches(Patterns.IDENTIFIER));
        assertTrue("['foo']".matches(Patterns.QUOTE_1_INDEX));
        assertTrue("['\\'']".matches(Patterns.QUOTE_1_INDEX));
        assertTrue("['\\b']".matches(Patterns.QUOTE_1_INDEX));
        assertFalse("[''']".matches(Patterns.QUOTE_1_INDEX));
        assertTrue(Patterns.GLOB.matcher("a[1]['\\''].b.c[ 12 ]").matches());
    }
    
    @Test
    public void constructor() throws PathException
    {
        new PropertyGlob("foo[*].bar");
    }
    
    @Test(expectedExceptions=IllegalArgumentException.class)
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
            assertEquals(e.getMessage(), "Invalid glob pattern \"!\".");
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
        Set<String> expansions = glob.all(widget);
        assertEquals(expansions.size(), 1);
        
        for (String expansion : expansions)
        {
            assertEquals(new PropertyPath(expansion).get(widget), 1);
        }
 
        glob = new PropertyGlob("widget.widgetListList[1][*].widget.number");
        expansions = glob.all(widget);
        assertEquals(expansions.size(), 3);
        
        for (String expansion : expansions)
        {
            assertEquals(new PropertyPath(expansion).get(widget), 1);
        }
        
        glob = new PropertyGlob("widget.widgetListList[*][0].widget.number");
        expansions = glob.all(widget);
        assertEquals(expansions.size(), 2);
        
        for (String expansion : expansions)
        {
            assertEquals(new PropertyPath(expansion).get(widget), 1);
        }
        
        glob = new PropertyGlob("widget.widgetListList[*][*].widget.number");
        expansions = glob.all(widget);
        assertEquals(expansions.size(), 4);
        
        for (String expansion : expansions)
        {
            assertEquals(new PropertyPath(expansion).get(widget), 1);
        }
        
        new PropertyPath("widget.widgetMapMap['foo']['foo'].widget.number").set(widget, 1, true);
        new PropertyPath("widget.widgetMapMap['foo']['bar']").set(widget, null, true);
        new PropertyPath("widget.widgetMapMap['bar']['foo'].widget.number").set(widget, 1, true);
        new PropertyPath("widget.widgetMapMap['bar']['bar'].widget.number").set(widget, 1, true);
        new PropertyPath("widget.widgetMapMap['bar']['baz'].widget.number").set(widget, 1, true);

        glob = new PropertyGlob("widget.widgetMapMap['foo'][*].widget.number");
        expansions = glob.all(widget);
        assertEquals(expansions.size(), 1);
        
        for (String expansion : expansions)
        {
            assertEquals(new PropertyPath(expansion).get(widget), 1);
        }
 
        glob = new PropertyGlob("widget.widgetMapMap['bar'][*].widget.number");
        expansions = glob.all(widget);
        assertEquals(expansions.size(), 3);
        
        for (String expansion : expansions)
        {
            assertEquals(new PropertyPath(expansion).get(widget), 1);
        }

        glob = new PropertyGlob("widget.widgetMapMap[*]['foo'].widget.number");
        expansions = glob.all(widget);
        assertEquals(expansions.size(), 2);
        
        for (String expansion : expansions)
        {
            assertEquals(new PropertyPath(expansion).get(widget), 1);
        }

        glob = new PropertyGlob("widget.widgetMapMap[*][*].widget.number");
        expansions = glob.all(widget);
        assertEquals(expansions.size(), 4);
        
        for (String expansion : expansions)
        {
            assertEquals(new PropertyPath(expansion).get(widget), 1);
        }
        
        glob = new PropertyGlob("widget.number[*]");
        expansions = glob.all(widget);
        assertEquals(expansions.size(), 0);
    }
}
