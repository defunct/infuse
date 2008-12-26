package com.goodworkalan.dspl;

public class Patterns
{
    final static String SKIPWHITE = "\\s*";
    
    public static String identifier(boolean capture) 
    {
        return
            (capture ? "(" : "") +  
                "[\\w&&[^\\d]][\\w\\d]*" +
            (capture ? ")" : "");
    }
    
    public static String listIndex(boolean capture)
    {
        return
            "\\[\\s*" +
                (capture ? "(" : "") +
                    "\\d+" +
                (capture ? ")" : "") +
            "\\s*\\]";
    }
    
    public static String globIndex(boolean capture)
    {
        return
            "\\[\\s*" +
                (capture ? "(" : "") +
                    "\\*" +
                (capture ? ")": "") +
            "\\s*\\]";
    
    }
    
    public static String anyIndex(boolean capture)
    {
        return
            globIndex(capture) + "|" + listIndex(capture) + "|" +
                stringIndex('\'', capture) + "|" + stringIndex('"', capture);
    }
    
    public static String part(boolean capture)
    {
        return
            identifier(capture) +
                "(?:\\s*" +
                    "(?:" + anyIndex(capture) + ")" +
                ")*";
    }

    public static String glob()
    {
        return part(false) + "(?:\\s*\\.\\s*" + part(false) + ")*";  
    }

    private final static String escaped(String...characters)
    {
        StringBuffer newString = new StringBuffer();
        String separator = "";
        for (int i = 0; i < characters.length; i++)
        {
            newString.append(separator)
                     .append("\\\\")
                     .append(characters[i]);
            separator = "|";
        }
        return newString.toString();
    }

    public static String stringIndex(char quote, boolean capture)
    {
        String escaped = escaped("b", "f", "n", "r", "t");
        return "\\[" + SKIPWHITE
                     + (capture ? "(" : "")
                     + quote
                         + "(?:"
                             + "[^" + quote + "\\\\]"
                             + "|" 
                             + "(?:"
                                 + "\\\\\\\\"
                                 + "|"
                                 + "\\\\" + quote
                                 + "|"
                                 + "\\\\u[A-Fa-f0-9]{4}"
                                 + "|"
                                 + "\\\\x[A-Fa-f0-9]{2}"
                                 + "|"
                                 + escaped
                             + ")"
                         + ")*"
                     + quote
                     + (capture ? ")" : "")
                     + SKIPWHITE + "\\]"; 
    }
}
