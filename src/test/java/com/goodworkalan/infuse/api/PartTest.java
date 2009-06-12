package com.goodworkalan.infuse.api;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.Test;

import com.goodworkalan.infuse.Part;

public class PartTest
{
    @Test
    public void constructor() 
    {
        Part part = new Part("name");
        assertEquals(part.getName(), "name");
        assertEquals(part.getQuote(), '\0');
        assertFalse(part.isIndex());
        
    }
    
    @Test
    public void isGlob()
    {
        assertTrue(new Part("*", true, '\0').isGlob());
        assertFalse(new Part("name").isGlob());
        assertFalse(new Part("*", false, '\0').isGlob());
        assertFalse(new Part("*", true, '"').isGlob());
    }
}
