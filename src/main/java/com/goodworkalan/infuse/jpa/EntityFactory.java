package com.goodworkalan.infuse.jpa;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;

import com.goodworkalan.infuse.FactoryException;
import com.goodworkalan.infuse.ObjectFactory;
import com.goodworkalan.infuse.Part;
import com.goodworkalan.infuse.Path;
import com.goodworkalan.infuse.PropertyInfo;
import com.goodworkalan.infuse.TransmutationException;
import com.goodworkalan.infuse.Converter;
import com.goodworkalan.infuse.Tree;

/**
 * An infusion factory that will create objects by looking them up in a JPA data
 * source and returning an existing object if one does indeed exist.
 * 
 * @author Alan Gutierrez
 */
public class EntityFactory implements ObjectFactory
{
    private final EntityManager em;
    
    public EntityFactory(EntityManager em)
    {
        this.em = em;
    }

    public Object create(Type type, Tree tree, Path context) throws FactoryException
    {
        Class<?> clazz = null;
        if (type instanceof ParameterizedType)
        {
            clazz = (Class<?>) ((ParameterizedType) type).getRawType();
        }
        else if (type instanceof Class<?>)
        {
            clazz = (Class<?>) type;
        }
        else
        {
            return null;
        }
        if (clazz.getAnnotation(Entity.class) != null)
        {
            for (Method method : clazz.getMethods())
            {
                if (method.getAnnotation(Id.class) != null)
                {
                    PropertyInfo propertyInfo = new PropertyInfo(clazz, method);
                    String value = (String) tree.get(context.append(new Part(propertyInfo.getName())));
                    if (value == null)
                    {
                        return null;
                    }
                    try
                    {
                        return em.find(clazz, new Converter().fromString(propertyInfo.getPropertyType(), value));
                    }
                    catch (TransmutationException e)
                    {
                        return null;
                    }
                }
            }
        }
        return null;
    }
}
