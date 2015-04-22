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
public class RandomType extends TypeHandler{

    public static final String TYPE_NAME = "random";
    public static final String TYPE_DISPLAY_NAME = "Random";
    
    private String[] values;
    
    public RandomType(String... args) {
        super();
        this.values = args;
    }

    
    @Override
    public String getNextRandomValue() {
        return values[getRand().nextInt(0, values.length - 1)];
    }
    
}
