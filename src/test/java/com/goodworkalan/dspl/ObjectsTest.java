package com.goodworkalan.dspl;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class ObjectsTest
{
    @Test // Solely to satisfy test coverage.
    public void constructor() 
    {
        new Objects();
    }
    
    @Test
    public void isAssignableFrom()
    {
        assertTrue(Objects.isAssignableFrom(long.class, Long.class));
        assertFalse(Objects.isAssignableFrom(long.class, Object.class));
        assertTrue(Objects.isAssignableFrom(int.class, Integer.class));
        assertFalse(Objects.isAssignableFrom(int.class, Object.class));
        assertTrue(Objects.isAssignableFrom(short.class, Short.class));
        assertFalse(Objects.isAssignableFrom(short.class, Object.class));
        assertTrue(Objects.isAssignableFrom(char.class, Character.class));
        assertFalse(Objects.isAssignableFrom(char.class, Object.class));
        assertTrue(Objects.isAssignableFrom(byte.class, Byte.class));
        assertFalse(Objects.isAssignableFrom(byte.class, Object.class));
        assertTrue(Objects.isAssignableFrom(boolean.class, Boolean.class));
        assertFalse(Objects.isAssignableFrom(boolean.class, Object.class));
        assertTrue(Objects.isAssignableFrom(float.class, Float.class));
        assertFalse(Objects.isAssignableFrom(float.class, Object.class));
        assertTrue(Objects.isAssignableFrom(double.class, Double.class));
        assertFalse(Objects.isAssignableFrom(double.class, Object.class));
    }
}
