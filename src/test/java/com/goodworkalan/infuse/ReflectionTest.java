package com.goodworkalan.infuse;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

public class ReflectionTest
{
    @Test
    public void hasTypes() throws SecurityException, NoSuchMethodException
    {
        Class<?> type = HasTypes.class;
        Method method = type.getMethod("getStringList");
        System.out.println(method.getGenericReturnType());
    }
}
