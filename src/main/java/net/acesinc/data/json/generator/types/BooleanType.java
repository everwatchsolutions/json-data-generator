/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.types;

import java.util.Random;

/**
 *
 * @author andrewserff
 */
public class BooleanType extends TypeHandler {
    public static final String TYPE_NAME = "boolean";
    public static final String TYPE_DISPLAY_NAME = "Boolean";

    @Override
    public Boolean getNextRandomValue() {
        return new Random().nextBoolean();
    }
    
    @Override
    public String getName() {
        return TYPE_NAME;
    }
            
}
