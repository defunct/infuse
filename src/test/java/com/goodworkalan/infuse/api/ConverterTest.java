package com.goodworkalan.infuse.api;

import static org.testng.Assert.assertEquals;
import static com.goodworkalan.infuse.Converter.box;

import org.testng.annotations.Test;

import com.goodworkalan.infuse.TransmutationException;
import com.goodworkalan.infuse.Converter;

public class ConverterTest {
    @Test
    public void boxes() {
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
    public void transmuteBoolean() throws TransmutationException {
        Converter converter = new Converter();
        assertEquals(converter.fromString(boolean.class, "true"), true);
    }

    @Test
    public void transmuteCharacter() throws TransmutationException {
        Converter converter = new Converter() {
        };
        assertEquals(converter.fromString(char.class, "a"), 'a');
    }

    @Test
    public void transmuteToObject() throws TransmutationException {
        Converter transmutator = new Converter();

        assertEquals(transmutator.fromString(Object.class, "a"), "a");
    }
}
