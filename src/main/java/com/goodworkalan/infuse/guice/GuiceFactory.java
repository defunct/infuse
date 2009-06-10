package com.goodworkalan.infuse.guice;

import java.lang.reflect.Type;

import com.goodworkalan.infuse.FactoryException;
import com.goodworkalan.infuse.ObjectFactory;
import com.goodworkalan.infuse.Path;
import com.goodworkalan.infuse.Tree;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;

public class GuiceFactory implements ObjectFactory
{
    private final Injector injector;
    
    @Inject
    public GuiceFactory(Injector injector)
    {
        this.injector = injector;
    }

    public Object create(Type type, Tree tree, Path context) throws FactoryException
    {
        return injector.getInstance(Key.get(type));
    }
}
