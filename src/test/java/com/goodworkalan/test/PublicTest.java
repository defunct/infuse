package com.goodworkalan.test;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.goodworkalan.infuse.InfusionBuilder;
import com.goodworkalan.infuse.PathException;
import com.goodworkalan.infuse.PropertyList;

public class PublicTest
{
    @Test
    public void test() throws PathException 
    {
        PropertyList path = new PropertyList("foo[1][2].bar", false);
        assertEquals(path.withoutIndexes(), "foo.bar");
    }
    
    @Test
    public void pathTree() throws PathException
    {
        InfusionBuilder tree = new InfusionBuilder();
        
        tree.set("widget.widget.number", "1");
    }
}
