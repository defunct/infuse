package com.goodworkalan.dspl;

import java.util.Map;

public class Phone
{
    private String type;
    
    private String number;
    
    private Map<String, Department> foo;
    
    public void setType(String type)
    {
        this.type = type;
    }
    
    public String getType()
    {
        return type;
    }
    
    public void setNumber(String number)
    {
        this.number = number;
    }
    
    public String getNumber()
    {
        return number;
    }
    
    public Map<String, Department> getDepartment()
    {
        return foo;
    }
    
    public void setDepartment(Map<String, Department> foo)
    {
        this.foo = foo;
    }
}
