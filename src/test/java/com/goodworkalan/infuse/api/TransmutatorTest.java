package com.goodworkalan.infuse.api;

import static org.testng.Assert.assertEquals;
import static com.goodworkalan.infuse.Transmutator.box;

import org.testng.annotations.Test;

import com.goodworkalan.infuse.TransmutationException;
import com.goodworkalan.infuse.Transmutator;

public class TransmutatorTest
{
    @Test
    public void boxes()
    {
        assertEquals(box(long.class), Long.class);
        assertEquals(box(int.class), Integer.class);
        assertEquals(box(short.class), Short.class);
        assertEquals(box(char.class), Character.class);
        assertEquals(box(byte.class), Byte.class);
        assertEquals(box(boolean.class), Boolean.class);
        assertEquals(box(float.class), Float.class);
        assertEquals(box(double.class), Double.class);
        assertEquals(box(Object.class), Object.class);
    }
    
    @Test
    public void transmuteBoolean() throws TransmutationException
    {
        Transmutator transmutator = new Transmutator();
        
        assertEquals((Boolean) transmutator.transmute(boolean.class, "true"), (Boolean) true);
    }
    
    @Test
    public void transmuteCharacter() throws TransmutationException
    {
        Transmutator transmutator = new Transmutator();
        
        assertEquals((Character) transmutator.transmute(char.class, "a"), (Character) 'a');
    }
    
    @Test
    public void transmuteToObject() throws TransmutationException
    {
        Transmutator transmutator = new Transmutator();
        
        assertEquals(transmutator.transmute(Object.class, "a"), "a");
    }
}
