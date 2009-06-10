package com.goodworkalan.infuse.api;

import static org.testng.Assert.assertEquals;

import java.util.Collections;

import org.testng.annotations.Test;

import com.goodworkalan.infuse.BasicObjectFactory;
import com.goodworkalan.infuse.FactoryException;
import com.goodworkalan.infuse.Infusion;
import com.goodworkalan.infuse.InfusionBuilder;
import com.goodworkalan.infuse.NavigateException;
import com.goodworkalan.infuse.ObjectFactory;
import com.goodworkalan.infuse.ParseException;
import com.goodworkalan.infuse.Widget;

public class InfusionBuilderTest
{
    @Test
    public void addFactories() throws NavigateException, FactoryException, ParseException
    {
        InfusionBuilder builder = new InfusionBuilder();
        
        builder.addFactories(Collections.<ObjectFactory>singleton(new BasicObjectFactory()));
        
        Widget widget = new Widget();
        
        Infusion infusion = builder.getInstance(widget);
        infusion.infuse("widget.string", "Hello, World!");
        
        assertEquals(widget.getWidget().getString(), "Hello, World!");
    }
    
    @Test
    public void addFactory() throws NavigateException, FactoryException, ParseException
    {
        InfusionBuilder builder = new InfusionBuilder();
        
        builder.addFactory(new BasicObjectFactory());
        
        Widget widget = new Widget();
        
        Infusion infusion = builder.getInstance(widget);
        infusion.infuse("widget.string", "Hello, World!");
        
        assertEquals(widget.getWidget().getString(), "Hello, World!");
    }
}
