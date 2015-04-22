/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.types;

import org.apache.commons.lang3.RandomStringUtils;

/**
 *
 * @author andrewserff
 */
public class AlphaType extends TypeHandler {
    public static final String TYPE_NAME = "alpha";
    public static final String TYPE_DISPLAY_NAME = "Alphabetic";
    
    private int length;

    public AlphaType(String... args) {
        super();
        if (args.length != 1) {
            throw new IllegalArgumentException("You must specifc a length for Alpha types");
        }
        length = Integer.parseInt(args[0]);
    }
    
    

    
    @Override
    public String getNextRandomValue() {
        return RandomStringUtils.randomAlphabetic(length);
    }
            
}
