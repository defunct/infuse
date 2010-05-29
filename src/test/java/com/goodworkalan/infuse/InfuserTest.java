package com.goodworkalan.infuse;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.net.MalformedURLException;
import java.net.URL;

import org.testng.annotations.Test;

/**
 * Unit tests for the {@link Infuser} class.
 *
 * @author Alan Gutierrez
 */
public class InfuserTest {
    /** Test conversion to integer.  */
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
    
    /** Cannot convert character */
    @Test(expectedExceptions = InfusionException.class)
    public void badCharacter() {
        exceptional(new Runnable() {
            public void run() {
                new Infuser().infuse(char.class, "00");
            }
        }, "CharacterInfuser/character.length", "The string value [00] is too long to convert to a character.");
    }
    
    /** Cannot zero length character */
    @Test(expectedExceptions = InfusionException.class)
    public void zeroCharacter() {
        exceptional(new Runnable() {
            public void run() {
                new Infuser().infuse(char.class, "");
            }
        }, "CharacterInfuser/character.zero", "Unable to convert zero length string into a character.");
    }
    
    /** Test set infuser. */
    @Test
    public void setInfuser() {
        Infuser infuser = new Infuser();
        infuser.setInfuser(String.class, BogusInfuser.class);
        assertEquals(infuser.infuse(String.class, "a"), new Integer(1));
    }

    /**
     * Run the given runnable and catch an infusion exception, asserting that
     * the exception message key and message are equal to the given message key
     * and given message.
     * 
     * @param runnable
     *            The exception throwing code.
     * @param messageKey
     *            The expected message key.
     * @param message
     *            The expected message.
     */
    public static void exceptional(Runnable runnable, String messageKey, String message) {
        try {
            runnable.run();
        } catch (InfusionException e) {
            assertEquals(e.getMessageKey(), messageKey);
            assertEquals(e.getMessage(), message);
            throw e;
        }
    }
}