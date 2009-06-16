package com.goodworkalan.infuse;

import java.lang.reflect.Type;


// TODO Document.
public final class DefaultConstructorFactory implements ObjectFactory
{
    // TODO Document.
    public Object create(Type type, Tree tree, Path context) throws FactoryException
    {
        Class<?> cls = Objects.toClass(type);
        if (cls != null && !cls.isInterface())
        {
            try
            {
                return cls.newInstance();
            }
            catch (Exception e)
            {
                throw new FactoryException(112, e).add(cls.getName());
            }
        }
        return null;
    }
}