package com.goodworkalan.dspl;

public class Department
{
    private String name;
    
    private Phone phone;
    
    public Department()
    {
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setPhone(Phone phone)
    {
        this.phone = phone;
    }
    
    public Phone getPhone()
    {
        return phone;
    }
}
