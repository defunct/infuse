package com.goodworkalan.dspl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Widget
{
    private List<List<String>> stringListList;
    
    private Map<String, Map<String, String>> stringMapMap;

    public Widget()
    {
        stringListList = new ArrayList<List<String>>();
        stringListList.add(new ArrayList<String>());
        stringMapMap = new HashMap<String, Map<String,String>>();
    }
    
    public List<List<String>> getStringListList()
    {
        return stringListList;
    }
    
    public Map<String, Map<String, String>> getStringMapMap()
    {
        return stringMapMap;
    }
}
