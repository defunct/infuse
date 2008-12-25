package com.goodworkalan.dspl;

import static com.goodworkalan.dspl.Objects.toList;
import static com.goodworkalan.dspl.Objects.toMap;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertyGlob
{
    private final String glob;
    
    public PropertyGlob(String path) throws PathException
    {
        if (path == null)
        {
            throw new IllegalArgumentException();
        }
        if (!Patterns.GLOB.matcher(path).matches())
        {
            throw new PathException(122).add(Messages.stringEscape(path));
        }
        this.glob = path;
    }
    
    public Set<String> outgoingAll(Object bean)
    {
        try
        {
            if (bean == null)
            {
                throw new IllegalArgumentException();
            }
     
            Set<String> stems = Collections.singleton(""); 
            
            StringBuilder newPath = new StringBuilder();
            Matcher matcher = Pattern.compile(Patterns.ANY_INDEX).matcher(glob);
            int start = 0;
            while (matcher.find())
            {
                String skipped = glob.substring(start, matcher.start());
                newPath.append(skipped);
                String index = glob.substring(matcher.start(), matcher.end());
                
                if (index.matches(Patterns.GLOB_INDEX))
                {
                    Set<String> expanded = new HashSet<String>();
    
                    for (String stem : stems)
                    {
                        PropertyPath path = new PropertyPath(stem + newPath);
                        Object object = path.get(bean);
                        if (object instanceof List)
                        {
                            List<Object> list = toList(object);
                            for (int i = 0; i < list.size(); i++)
                            {
                                if (list.get(i) != null)
                                {
                                    expanded.add(stem + newPath + "[" + i + "]");
                                }
                            }
                        }
                        else if (object instanceof Map)
                        {
                            Map<Object, Object> map = toMap(object);
                            for (Map.Entry<Object, Object> entry : map.entrySet())
                            {
                                if (entry.getValue() != null)
                                {
                                    expanded.add(stem + newPath + "[" + MapIndex.escape(entry.getKey().toString()) + "]");
                                }
                            }
                        }
                    }
                    
                    newPath.setLength(0);
                    stems = expanded;
                }
                else
                {
                    newPath.append(index);
                }
                
                start = matcher.end();
            }
            
            newPath.append(glob.substring(start));
            
            Set<String> paths = new HashSet<String>();
            for (String stem : stems)
            {
                paths.add(new PropertyPath(stem + newPath).toString());
            }
    
            return paths;
        }
        catch (PathException e)
        {
            throw new Error(e);
        }
    }
}