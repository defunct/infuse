package com.goodworkalan.infuse;

import java.util.List;
import java.util.Map;

public class Widget
{
    private String string;
    
    private int number;
    
    private Widget widget;

    private List<List<String>> stringListList;
    
    private Map<String, Map<String, String>> stringMapMap;

    private List<List<Widget>> widgetListList;
    
    private Map<String, Map<String, Widget>> widgetMapMap;

    public Widget()
    {
    }

    public void setNumber(int number)
    {
        this.number = number;
    }
    
    public int getNumber()
    {
        return number;
    }

    public void setString(String string)
    {
        this.string = string;
    }
    
    public String getString()
    {
        return string;
    }

    public void setWidget(Widget widget)
    {
        this.widget = widget;
    }
    
    public Widget isWidget()
    {
        throw new UnsupportedOperationException();
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
    
    public void setWidgetListList(List<List<Widget>> widgetListList)
    {
        this.widgetListList = widgetListList;
    }
    
    public List<List<Widget>> getWidgetListList()
    {
        return widgetListList;
    }
    
    public void setStringMapMap(Map<String, Map<String, String>> stringMapMap)
    {
        this.stringMapMap = stringMapMap;
    }
    
    public Map<String, Map<String, String>> getStringMapMap()
    {
        return stringMapMap;
    }
    
    public void setWidgetMapMap(Map<String, Map<String, Widget>> widgetMapMap)
    {
        this.widgetMapMap = widgetMapMap;
    }
    
    public Map<String, Map<String, Widget>> getWidgetMapMap()
    {
        return widgetMapMap;
    }
    
    public void setString(String a, String b, String value)
    {
        stringMapMap.get(a).put(b, value);
    }
    
    public String getString(String a, String b)
    {
        return stringMapMap.get(a).get(b);
    }
    
    public void setString(int a, int b, String value)
    {
        stringListList.get(a).add(b, value);
    }
    
    public String getString(int a, Integer b)
    {
        return stringListList.get(a).get(b);
    }
}
