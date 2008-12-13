package com.goodworkalan.dspl;

import java.util.List;
import java.util.Map;

public class Widget
{
    private Widget widget;

    private List<List<String>> stringListList;
    
    private Map<String, Map<String, String>> stringMapMap;

    public Widget()
    {
    }

    public void setWidget(Widget widget)
    {
        this.widget = widget;
    }

    public Widget getWidget()
    {
        return widget;
    }
    
    public void setStringListList(List<List<String>> stringListList)
    {
        this.stringListList = stringListList;
    }
    
    public List<List<String>> getStringListList()
    {
        return stringListList;
    }
    
    public void setStringMapMap(Map<String, Map<String, String>> stringMapMap)
    {
        this.stringMapMap = stringMapMap;
    }
    
    public Map<String, Map<String, String>> getStringMapMap()
    {
        return stringMapMap;
    }
}
