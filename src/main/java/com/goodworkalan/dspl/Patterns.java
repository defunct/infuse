package com.goodworkalan.dspl;

import java.util.regex.Pattern;

// TODO This might all become part of PropertyList.
class Patterns
{
    final static String SKIPWHITE = "\\s*";
    
    final static String IDENTIFIER = "([\\w&&[^\\d]][\\w\\d]*)";
    
    final static String LIST_INDEX = "\\[\\s*(\\d+)\\s*\\]";
    
    final static String GLOB_INDEX = "\\[\\s*(\\*)\\s*\\]";
    
    final static String QUOTE_1_INDEX = stringIndex('\'');
    
    final static String QUOTE_2_INDEX = stringIndex('"');
    
    final static String ANY_INDEX =
        GLOB_INDEX + "|" + LIST_INDEX + "|" +
            QUOTE_1_INDEX + "|" + QUOTE_2_INDEX;
    
    final static String PART =
        SKIPWHITE + IDENTIFIER + SKIPWHITE +
           "(?:" +
               "(?:" + ANY_INDEX + ")" +
           SKIPWHITE + ")*";

    final static Pattern GLOB = Pattern.compile(PART + "(?:(\\.)" + PART + ")*");

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

    private final static String stringIndex(char quote)
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
