package com.goodworkalan.dspl;

import java.util.regex.Pattern;

class Patterns
{
    final static String SKIPWHITE = "\\s*";
    
    final static String IDENTIFIER = "([\\w&&[^\\d]][\\w\\d]*)";
    
    final static String LIST_INDEX = "\\[\\s*(\\d+)\\s*\\]";
    
    final static String GLOB_INDEX = "\\[\\s*(\\*)\\s*\\]";
    
    final static String ANY_INDEX =
        GLOB_INDEX + "|" + LIST_INDEX + "|" +
            stringIndex('\'') + "|" + stringIndex('"');
    
    final static String PART =
        SKIPWHITE + IDENTIFIER + SKIPWHITE +
           "(?:" +
               "(?:" + ANY_INDEX + ")" +
           SKIPWHITE + ")*";

    final static Pattern GLOB = Pattern.compile(PART + "(?:(\\.)" + PART + ")*");

    final static String escaped(String...characters)
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

    final static String stringIndex(char quote)
    {
        String escaped = escaped("b", "f", "n", "r", "t");
        return "\\[" + SKIPWHITE
                     + "("
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
                     + ")"
                     + SKIPWHITE + "\\]"; 
    }
}
