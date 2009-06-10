package com.goodworkalan.infuse.api;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import com.goodworkalan.infuse.FactoryException;
import com.goodworkalan.infuse.Infusion;
import com.goodworkalan.infuse.NavigateException;
import com.goodworkalan.infuse.ParseException;
import com.goodworkalan.infuse.Widget;

public class InfusionTest
{
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
}
