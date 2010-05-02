package com.goodworkalan.infuse;

public class CharacterInfuser implements ObjectInfuser {
    private final Class<?> type;
    
    public CharacterInfuser(Class<?> type) {
        this.type = type;
    }

    public Object infuse(String string) {
        if (string == null) {
            return type.isPrimitive() ? ((char) 0) : null;
        }

        if (string.length() != 1) {
            throw new InfusionException(0, string);
        }
        
        return string.charAt(0);
    }
}
