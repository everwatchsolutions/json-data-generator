/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.types;

/**
 *
 * @author andrewserff
 */
public class LastName extends TypeHandler {
    public static final String TYPE_NAME = "lastName";
    public static final String TYPE_DISPLAY_NAME = "Last Name";
    
    private String[] nameList = { "Smith", "Doe", "Grange", "Black", "Baggins", "Loxly" };

    @Override
    public String getNextRandomValue() {
        return nameList[getRand().nextInt(0, nameList.length - 1)];
    }
    
    @Override
    public String getName() {
        return TYPE_NAME;
    }
            
}
