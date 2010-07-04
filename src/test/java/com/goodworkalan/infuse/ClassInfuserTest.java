package com.goodworkalan.infuse;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

/**
 * Unit tests for the {@link ClassInfuser} class.
 *
 * @author Alan Gutierrez
 */
public class ClassInfuserTest {
    /** Test loading a class. */
    @Test
    public void load() {
        Class<?> stringClass = (Class<?>) new ClassInfuser(null).infuse("java.lang.String");
        assertEquals(stringClass, String.class);
    }
    
    /** Test failure to load a class. */
    @Test(expectedExceptions = InfusionException.class)
    public void notFound() {
        InfuserTest.exceptional(new Runnable() {
            public void run() {
                new ClassInfuser(null).infuse("this.class.cannot.Exist");
            }
        }, ClassInfuser.class, "classNotFound", "Cannot find class [this.class.cannot.Exist].");
    }
}
