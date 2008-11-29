package com.goodworkalan.dspl;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class BeanPathTest
{
    @Test public void constructor() 
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
}
