package com.goodworkalan.infuse;

import java.net.URI;

import org.testng.annotations.Test;

import com.goodworkalan.reflective.Constructor;
import com.goodworkalan.reflective.ReflectiveException;
import com.goodworkalan.reflective.ReflectiveFactory;

import static com.goodworkalan.infuse.InfuserTest.*;
/**
 * Unit tests for the {@link StringConstructorInfuser} class.
 *
 * @author Alan Gutierez
 */
public class StringConstructorInfuserTest {
    /** Test missing string constructor. */
    @Test(expectedExceptions = InfusionException.class)
    public void cannotGetConstructor() {
        exceptional(new Runnable() {
            public void run() {
                new StringConstructorInfuser(new ReflectiveFactory() {
                    public <T> Constructor<T> getConstructor(Class<T> type, Class<?>... initargs)
                    throws ReflectiveException {
                        try {
                            throw new NoSuchMethodException();
                        } catch (NoSuchMethodException e) { 
                            throw new ReflectiveException(ReflectiveException.NO_SUCH_METHOD, e);
                        }
                    };
                }, URI.class);
            }
        }, "StringConstructorInfuser/get.constructor", "Unable to find a string constructor for type [java.net.URI].");
    }

    /** Test failed string constructor. */
    @Test(expectedExceptions = InfusionException.class)
    public void cannotCreateNewInstance() {
        exceptional(new Runnable() {
            public void run() {
                new StringConstructorInfuser(new ReflectiveFactory() {
                    public <T> Constructor<T> getConstructor(Class<T> type, Class<?>... initargs)
                    throws ReflectiveException {
                        ReflectiveFactory reflective = new ReflectiveFactory();
                        return new Constructor<T>(reflective.getConstructor(type, String.class).getNative()) {
                            @Override
                            public T newInstance(Object... initargs)
                            throws ReflectiveException {
                                try {
                                    throw new IllegalAccessException();
                                } catch (IllegalAccessException e) {
                                    throw new ReflectiveException(ReflectiveException.ILLEGAL_ACCESS, e);
                                }
                            }
                        };
                    };
                }, URI.class).infuse("a");
            }
        }, "StringConstructorInfuser/new.instance", "Unable to create a new instance of type [java.net.URI] using the string [a].");
    }
}
