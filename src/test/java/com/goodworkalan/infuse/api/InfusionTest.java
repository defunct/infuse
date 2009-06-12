package com.goodworkalan.infuse.api;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import com.goodworkalan.infuse.FactoryException;
import com.goodworkalan.infuse.Infusion;
import com.goodworkalan.infuse.NavigateException;
import com.goodworkalan.infuse.ParseException;
import com.goodworkalan.infuse.Tree;
import com.goodworkalan.infuse.Widget;

public class InfusionTest
{
    @Test void flavorsOfThis() throws NavigateException, FactoryException, ParseException
    {
        Widget widget = new Widget();
        
        Infusion infusion = Infusion.getInstance(widget);
        
        infusion.infuse("stringMapMap.this[\"this\"][\"world\"]", "Hello, World!");
        
        assertEquals(widget.getStringMapMap().get("this").get("world"), "Hello, World!");
    }

    @Test
    public void stringMapMap() throws NavigateException, FactoryException, ParseException
    {
        Widget widget = new Widget();
        Infusion infusion = Infusion.getInstance(widget);
        
        infusion.infuse("stringMapMap[\"hello\"][\"world\"]", "Hello, World!");
        
        assertEquals(widget.getStringMapMap().get("hello").get("world"), "Hello, World!");
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void map() throws NavigateException, FactoryException, ParseException
    {
        Widget widget = new Widget();
        Infusion infusion = Infusion.getInstance(widget);
        
        infusion.infuse("map[\"hello\"][\"world\"]", "Hello, World!");
    }
    
    @Test
    public void stringMapMapNull() throws NavigateException, FactoryException, ParseException
    {
        Widget widget = new Widget();
        Infusion infusion = Infusion.getInstance(widget);
        
        infusion.infuse("stringMapMap[\"hello\"][\"world\"]", null);
        
        assertNull(widget.getStringMapMap().get("hello").get("world"));
    }
    
    @Test
    public void widgetListList() throws NavigateException, FactoryException, ParseException
    {
        Widget widget = new Widget();
        Infusion infusion = Infusion.getInstance(widget);
        
        infusion.infuse("widgetListList[0][0].string", "Hello, World!");
        
        assertEquals(widget.getWidgetListList().get(0).get(0).getString(), "Hello, World!");
    }
    
    @Test
    public void widgetListListAppend() throws NavigateException, FactoryException, ParseException
    {
        Widget widget = new Widget();
        Infusion infusion = Infusion.getInstance(widget);
        
        Tree tree = new Tree();
        for (int i = 12; i > 3; i-= 2)
        {
            tree.set("widgetListList[0][" + i + "].string", "Hello, World!");
        }
        infusion.infuse(tree);
        
        for (int i = 0; i < 5; i++)
        {
            assertEquals(widget.getWidgetListList().get(0).get(i).getString(), "Hello, World!");
        }
    }
}
