package com.goodworkalan.dspl;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class BeanPathTest
{
    @Test public void constructor() throws BeanPath.Error 
    {
        new BeanPath("name");
    }
    
    @Test public void get() throws BeanPath.Error
    {
        BeanPath path = new BeanPath("name");
        Department department = new Department();
        department.setName("Accounting");
        assertEquals(path.get(department), "Accounting");
    }
    
    @Test public void set() throws BeanPath.Error
    {
        BeanPath path = new BeanPath("name");
        Department department = new Department();
        path.set(department, "Accounting");
        assertEquals(department.getName(), "Accounting");
    }
    
    @Test public void getChild() throws BeanPath.Error
    {
        BeanPath path = new BeanPath("phone.number");
        
        Department department = new Department();
        Phone phone = new Phone();
        phone.setNumber("504.717.1428");
        department.setPhone(phone);
        
        assertEquals(path.get(department), "504.717.1428");
    }
    
    @Test public void setChild() throws BeanPath.Error
    {
        BeanPath path = new BeanPath("phone.number");
        
        Department department = new Department();
        Phone phone = new Phone();
        department.setPhone(phone);

        path.set(department, "504.717.1428");
        assertEquals(department.getPhone().getNumber(), "504.717.1428");
    }
}
