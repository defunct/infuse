package com.goodworkalan.infuse;

/**
 * The regular expressions that define DSPL.
 * <p>
 * These regular expressions are all that DSPL really is.
 * 
 * @author Alan Gutierrez
 */
public class Patterns
{
    /** A regular expression to skip whitespace. */ 
    final static String SKIPWHITE = "\\s*";

    /**
     * Creates a regular expression that matches a DSPL/Java identifier. If
     * capture is true, the regular expression surrounds the identifier with
     * parenthesis to capture the identifier.
     * 
     * @param capture
     *            Capture the identifier if true.
     * @return A regular expression that matches a DSPL/Java identifier.
     */
    public static String identifier(boolean capture) 
    {
        return
            (capture ? "(" : "") +  
                "[\\w&&[^\\d]][\\w\\d]*" +
            (capture ? ")" : "");
    }

    /**
     * Create a regular expression that matches a DSPL/Java numeric index.If
     * capture is true, the regular expression surrounds the value of the index
     * with parenthesis to capture the index value.
     * 
     * @param capture
     *            Capture the index value if true.
     * @return A regular expression that matches a DSPL/Java numeric index.
     */
    public static String listIndex(boolean capture)
    {
        return
            "\\[\\s*" +
                (capture ? "(" : "") +
                    "\\d+" +
                (capture ? ")" : "") +
            "\\s*\\]";
    }

    /**
     * Create a regular expression that matches a DSPL/Java numeric index.If
     * capture is true, the regular expression surrounds the value of the index
     * with parenthesis to capture the index value.
     * 
     * @param capture
     *            Capture the index value if true.
     * @return A regular expression that matches a DSPL/Java numeric index.
     */
    public static String globIndex(boolean capture)
    {
        return
            "\\[\\s*" +
                (capture ? "(" : "") +
                    "\\*" +
                (capture ? ")": "") +
            "\\s*\\]";
    
    }
    
    // TODO Document.
    public static String anyIndex(boolean capture)
    {
        return
            globIndex(capture) + "|" + listIndex(capture) + "|" +
                "\\[\\s*" + identifier(capture) + "\\s*\\]|" +
                stringIndex('\'', capture) + "|" + stringIndex('"', capture);
    }
    
    // TODO Document.
    public static String part(boolean capture)
    {
        return
            identifier(capture) +
                "(?:\\s*" +
                    "(?:" + anyIndex(capture) + ")" +
                ")*";
    }

    // TODO Document.
    public static String glob()
    {
        return part(false) + "(?:\\s*\\.\\s*" + part(false) + ")*";  
    }

    // TODO Document.
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

    // FIXME Add valid Java identifier, unquoted as a valid type.
    // TODO Document.
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
