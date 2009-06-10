package com.goodworkalan.infuse;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class InfusionBuilder
{
    private final Set<ObjectFactory> factories;
    
    public InfusionBuilder()
    {
        this.factories = new LinkedHashSet<ObjectFactory>();
    }
    
    public InfusionBuilder addFactory(ObjectFactory factory)
    {
        factories.add(factory);
        return this;
    }
    
    public InfusionBuilder addFactories(Collection<ObjectFactory> collection)
    {
        factories.addAll(collection);
        return this;
    }
    
    public Infusion getInstance(Object root)
    {
        return new Infusion(factories, root);
    }
}
