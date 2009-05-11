package com.goodworkalan.infuse;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.goodworkalan.infuse.GlobIndex;
import com.goodworkalan.infuse.PathException;

public class GlobIndexTest
{
    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void getRawType()
    {
        new GlobIndex().getRawType();
    }
    
    @Test
    public void getIndex()
    {
        assertEquals(new GlobIndex().getIndex(false), "*");
    }
    
    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void indexedBy()
    {
        new GlobIndex().indexedBy(null);
    }
    
    @Test
    public void duplicate()
    {
        assertEquals(new GlobIndex().duplicate().getClass(), GlobIndex.class);
    }
    
    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void typeOf() throws PathException
    {
        new GlobIndex().typeOf(null);
    }

    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void get() throws PathException
    {
        new GlobIndex().get(null, null, null);
    }

    
    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void set() throws PathException
    {
        new GlobIndex().set(null, null, null);
    }

 }
