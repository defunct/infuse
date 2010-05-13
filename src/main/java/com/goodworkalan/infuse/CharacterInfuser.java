package com.goodworkalan.infuse;

/**
 * Converts a string containing a single character into the single character.
 * 
 * @author Alan Gutierrez
 */
public class CharacterInfuser implements ObjectInfuser {
    /** The character class. */
    private final Class<?> type;

    /**
     * Create a character infuser with the given character type. The character
     * infuser created for the primitive character class is different from the
     * character infuser created for the object based character class in that
     * the different classes have different ways of handling null values.
     * 
     * @param type
     *            The character class.
     */
    public CharacterInfuser(Class<?> type) {
        this.type = type;
    }

    /**
     * Convert the given string containing a single character into a character.
     * The given string must have a length of 1.
     * 
     * @param string
     *            The string to convert.
     * @return A character.
     * @throws InfusionException
     *             If the length of the given string is not 1.
     */
    public Object infuse(String string) {
        if (string == null) {
            return type.isPrimitive() ? ((char) 0) : null;
        }

        if (string.length() == 0) {
            throw new InfusionException(CharacterInfuser.class, "character.zero");
        }
        
        if (string.length() != 1) {
            throw new InfusionException(CharacterInfuser.class, "character.length", string);
        }
        
        return string.charAt(0);
    }
}
