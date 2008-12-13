package com.goodworkalan.dspl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.testng.annotations.Test;

public class PropertyPathTest
{
    @Test public void constructor() throws PropertyPath.Error 
    {
        new PropertyPath("name");
    }
    
    @Test public void get() throws PropertyPath.Error
    {
        PropertyPath path = new PropertyPath("name");
        Department department = new Department();
        department.setName("Accounting");
        assertEquals(path.get(department), "Accounting");
    }
    
    @Test public void set() throws PropertyPath.Error
    {
        PropertyPath path = new PropertyPath("name");
        Department department = new Department();
        path.set(department, "Accounting", new PropertyPath.Factory());
        assertEquals(department.getName(), "Accounting");
    }
    
    @Test public void getChild() throws PropertyPath.Error
    {
        PropertyPath path = new PropertyPath("phone.number");
        
        Department department = new Department();
        Phone phone = new Phone();
        phone.setNumber("504.717.1428");
        department.setPhone(phone);
        
        assertEquals(path.get(department), "504.717.1428");
    }
    
    @Test public void setChild() throws PropertyPath.Error
    {
        PropertyPath path = new PropertyPath("phone.number");
        
        Department department = new Department();
        Phone phone = new Phone();
        department.setPhone(phone);

        path.set(department, "504.717.1428", new PropertyPath.Factory());
        assertEquals(department.getPhone().getNumber(), "504.717.1428");
    }
    
    @Test
    public void factory() throws Exception
    {
        PropertyPath.Factory factory = new PropertyPath.Factory();
        assertEquals(factory.create(SortedMap.class).getClass(), TreeMap.class);
        assertEquals(factory.create(Map.class).getClass(), HashMap.class);
        assertEquals(factory.create(List.class).getClass(), ArrayList.class);
    }

    @Test
    public void listIndex() throws Exception
    {
        PropertyPath.Index index = new PropertyPath.ListIndex(0);
        assertNull(index.typeOf(Object.class));
        Object bean = new Widget();
        PropertyPath.Property property = new PropertyPath.Property("stringMapMap");
        assertNull(index.typeOf(property.typeOf(bean)));
        property = new PropertyPath.Property("stringListList");
        Type type = property.typeOf(bean);
        type = index.typeOf(type);
        type = index.typeOf(type);
        assertEquals(String.class, type);

        PropertyPath.Factory factory = new PropertyPath.Factory();
        type = property.typeOf(bean);
        Object list = property.get(bean, null);
        type = index.typeOf(type);
        list = index.get(type, list, factory);
        type = index.typeOf(type);
        assertEquals(index.get(type, list, factory), "");
        assertEquals(((Widget) bean).getStringListList().get(0).get(0), "");
    }
    
    @Test
    public void mapIndex() throws Exception
    {
        PropertyPath.Index index = new PropertyPath.MapIndex("foo");
        assertNull(index.typeOf(Object.class));
        Object bean = new Widget();
        PropertyPath.Property property = new PropertyPath.Property("stringListList");
        assertNull(index.typeOf(property.typeOf(bean)));
        property = new PropertyPath.Property("stringMapMap");
        Type type = property.typeOf(bean);
        type = index.typeOf(type);
        type = index.typeOf(type);
        assertEquals(String.class, type);
    }
    
    @Test
    public void error()
    {
        try
        {
            throw new PropertyPath.Error();
        }
        catch (PropertyPath.Error e)
        {
        }
        try
        {
            throw new PropertyPath.Error(new IOException());
        }
        catch (PropertyPath.Error e)
        {
        }
    }
    
    @Test
    public void eatWhite()
    {
        String part = "   ab";
        int i = 0;
        i = PropertyPath.eatWhite(part, i);
        assertEquals(i, 3);
        i = PropertyPath.eatWhite(part, ++i);
        assertEquals(i, 4);
        i = PropertyPath.eatWhite(part, ++i);
        assertEquals(i, 5);
    }
    
    @Test
    public void newIndex() throws PropertyPath.Error
    {
        List<PropertyPath.Index> indexes = new ArrayList<PropertyPath.Index>();

        String part = "[ 1 ] "; 
        int i = PropertyPath.newIndex(part, 0, indexes);
        
        assertEquals(indexes.get(0).getClass(), PropertyPath.ListIndex.class);
        assertEquals(part.length(), i);
    }
}
