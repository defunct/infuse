package com.goodworkalan.infuse;

import static org.testng.Assert.assertEquals;

import java.net.URI;

import org.testng.annotations.Test;

public class InfuserTest {
    @Test
    public void infuse() {
        Infuser infuser = new Infuser();
        infuser.setInfuser(Character.class, CharacterInfuser.class);
        assertEquals(1, infuser.infuse(int.class, "1"));
        assertEquals('c', infuser.infuse(char.class, "c"));
        assertEquals(1, infuser.infuse(Integer.class, "1"));
        assertEquals('c', infuser.infuse(Character.class, "c"));
        assertEquals(null, infuser.infuse(Character.class, null));
        assertEquals('\0', infuser.infuse(char.class, null));
        assertEquals(null, infuser.infuse(Integer.class, null));
        assertEquals(0, infuser.infuse(int.class, null));
        assertEquals(null, infuser.infuse(Boolean.class, null));
        assertEquals(false, infuser.infuse(boolean.class, null));
        assertEquals(infuser.infuse(String.class, "a"), "a");
        assertEquals(URI.create("http://blogometer.com"), infuser.infuse(URI.class, "http://blogometer.com"));
    }
}
