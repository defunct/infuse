package com.goodworkalan.dspl;

import static org.testng.Assert.assertEquals;

import java.lang.reflect.Type;

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
        path.set(department, "Accounting");
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

        path.set(department, "504.717.1428");
        assertEquals(department.getPhone().getNumber(), "504.717.1428");
    }
    
    @Test
    public void listIndex() throws Exception
    {
        PropertyPath.Index index = new PropertyPath.ListIndex();
        PropertyPath.BeanProperty property = new PropertyPath.BeanProperty("stringListList");
        Object bean = new Widget();
        Type type = property.typeOf(bean);
        type = index.typeOf(type);
        type = index.typeOf(type);
        System.out.println(type);
    }
    
    @Test
    public void mapIndex() throws Exception
    {
        PropertyPath.Index index = new PropertyPath.MapIndex();
        PropertyPath.BeanProperty property = new PropertyPath.BeanProperty("stringMapMap");
        Object bean = new Widget();
        Type type = property.typeOf(bean);
        type = index.typeOf(type);
        type = index.typeOf(type);
        System.out.println(type);
    }
}
