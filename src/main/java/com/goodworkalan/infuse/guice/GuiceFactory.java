package com.goodworkalan.infuse.guice;

import java.lang.reflect.Type;

import com.goodworkalan.infuse.FactoryException;
import com.goodworkalan.infuse.InfusionBuilder;
import com.goodworkalan.infuse.ObjectFactory;
import com.goodworkalan.infuse.Path;
import com.google.inject.Injector;
import com.google.inject.Key;

public class GuiceFactory implements ObjectFactory
{
    private final Injector injector;
    
    public GuiceFactory(Injector injector)
    {
        this.injector = injector;
    }

    public Object create(InfusionBuilder builder, Type type, Path context)
            throws FactoryException
    {
        return injector.getInstance(Key.get(type));
    }
}
