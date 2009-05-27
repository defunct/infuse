package com.goodworkalan.infuse;

import org.testng.annotations.Test;

public class PartTest
{
    @Test
    public void constructor()
    {
        new Part("a", false, '\0');
    }
}
