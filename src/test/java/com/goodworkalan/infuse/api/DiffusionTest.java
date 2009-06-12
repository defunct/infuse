package com.goodworkalan.infuse.api;

import org.testng.annotations.Test;

import com.goodworkalan.infuse.Diffusion;
import com.goodworkalan.infuse.ParseException;

public class DiffusionTest
{
    @Test(expectedExceptions = NullPointerException.class)
    public void npeString() throws ParseException
    {
        new Diffusion(null);
    }
}
