package com.goodworkalan.dspl;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.Test;

public class PropertyTest
{
    @Test
    public void isAssignableFrom()
    {
        assertTrue(Property.isAssignableFrom(long.class, Long.class));
        assertFalse(Property.isAssignableFrom(long.class, Object.class));
        assertTrue(Property.isAssignableFrom(int.class, Integer.class));
        assertFalse(Property.isAssignableFrom(int.class, Object.class));
        assertTrue(Property.isAssignableFrom(short.class, Short.class));
        assertFalse(Property.isAssignableFrom(short.class, Object.class));
        assertTrue(Property.isAssignableFrom(char.class, Character.class));
        assertFalse(Property.isAssignableFrom(char.class, Object.class));
        assertTrue(Property.isAssignableFrom(byte.class, Byte.class));
        assertFalse(Property.isAssignableFrom(byte.class, Object.class));
        assertTrue(Property.isAssignableFrom(boolean.class, Boolean.class));
        assertFalse(Property.isAssignableFrom(boolean.class, Object.class));
        assertTrue(Property.isAssignableFrom(float.class, Float.class));
        assertFalse(Property.isAssignableFrom(float.class, Object.class));
        assertTrue(Property.isAssignableFrom(double.class, Double.class));
        assertFalse(Property.isAssignableFrom(double.class, Object.class));
    }
}
