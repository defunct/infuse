package com.goodworkalan.infuse;

// TODO Document.
public final class Part implements Comparable<Part>
{
    /** The property name. */
    private final String name;
    
    /** If true, the part was declared as an index. */
    private final boolean index;
    
    /** The character used to quote the index value, or '\0' if not quoted. */
    private final char quote;

    /**
     * Create a non-index part with the given name.
     * 
     * @param name
     *            The part name.
     */
    public Part(String name)
    {
        this.name = name;
        this.index = false;
        this.quote = '\0';
    }

    /**
     * Create a new property with the given name and the given indexes.
     * 
     * @param name
     *            The property name.
     * @param indexes
     *            The property indexes.
     */
    public Part(String name, boolean index, char quote)
    {
        this.name = name;
        this.index = index;
        this.quote = quote;
    }

    /**
     * Return the property name value which can be any string value.
     * 
     * @return The property name.
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Return true if the property was declared as an index.
     * 
     * @return True if the property was declared as an index.
     */
    public boolean isIndex()
    {
        return index;
    }

    /**
     * Return true if this part is an empty bracket set, the terminal part of a
     * PHP array parameter path. This is from the PHP array construct
     * "parameter[]=value".
     * 
     * @return True if this is an empty bracket set, indicating append the value
     *         to an array.
     */
    public boolean isAppend()
    {
        return name.equals("");
    }
    
    /**
     * Return the quote character used to quote the index value or '\0' if the
     * index is unquoted.
     * 
     * @return The quote character.
     */
    public char getQuote()
    {
        return quote;
    }

    /**
     * Return true if the property is a value glob.
     * 
     * @return True if the proeprty is a glob.
     */
    public boolean isGlob()
    {
        return quote == '\0' && index && name.equals("*");
    }

    /**
     * A property is equal to another property object with the same name,
     * quote that has the same index flag.
     * 
      * @param object
     *            An object to which to compare this property.
     * @return True if the given object is equal to this property.
     */
    @Override
    public boolean equals(Object object)
    {
        if (object == this)
        {
            return true;
        }
        if (object instanceof Part)
        {
            Part property = (Part) object;
            return name.equals(property.name) 
                && index == property.index
                && quote == property.quote;
        }
        return false;
    }

    /**
     * Return a hash code that combines the hash code of the name property, with
     * a hash code generated from the index and quote properties.
     * 
     * @return The hash code.
     */
    @Override
    public int hashCode()
    {
        int hash = 694847539;
        hash = hash * 37 + name.hashCode();
        hash = hash * 37 + (index ? 533000401 : 553105243);
        hash = hash * 37 + (int) quote;
        return hash;
    }

    /**
     * Compare this part to another part, first comparing the name, then
     * comparing the index property, where an index declared part is less than a
     * bean property part, then comparing the quote characters.
     * 
     * @param part
     *            The part to compare.
     * @return A negative integer, zero, or a positive integer as this part is
     *         less than, equal to, or greater than the given part.
     */
    public int compareTo(Part part)
    {
        int compare = name.compareTo(part.name);
        if (compare == 0)
        {
            compare = index == part.index ? 0 : index ? -1 : 1;
            if (compare == 0)
            {
                compare = (int) quote - (int) part.quote;
            }
        }
        return compare;
    }
}