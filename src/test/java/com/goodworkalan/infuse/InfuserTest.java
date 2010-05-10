package com.goodworkalan.infuse;
import static org.testng.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.testng.annotations.Test;

/**
 * Unit tests for the {@link Infuser} class.
 *
 * @author Alan Gutierrez
 */
public class InfuserTest {
    /** Test coversion to integer.  */
    @Test
    public void defaults() throws MalformedURLException {
        Infuser infuser = new Infuser();
        assertEquals(infuser.infuse(int.class, "1"), new Integer(1));
        assertEquals(infuser.infuse(int.class, "1"), new Integer(1));
        assertEquals(infuser.infuse(int.class, null), new Integer(0));
        assertEquals(infuser.infuse(char.class, "c"), new Character('c'));
        assertEquals(infuser.infuse(char.class, null), new Character('\0'));
        assertNull(infuser.infuse(Character.class, null));
        assertEquals(infuser.infuse(URL.class, "http://hello.com"), new URL("http://hello.com"));
        assertNull(infuser.infuse(Integer.class, null));
        assertEquals(infuser.infuse(String.class, "1"), "1");
    }
}
