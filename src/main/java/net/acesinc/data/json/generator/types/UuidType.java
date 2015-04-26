/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.types;

import java.util.UUID;

/**
 *
 * @author andrewserff
 */
public class UuidType extends TypeHandler {
    public static final String TYPE_NAME = "uuid";
    public static final String TYPE_DISPLAY_NAME = "UUID";

    @Override
    public String getNextRandomValue() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public String getName() {
        return TYPE_NAME;
    }
            
}
