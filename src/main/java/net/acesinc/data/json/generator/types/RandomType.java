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
    
    @Override
    public void setLaunchArguments(String[] launchArguments) {
        super.setLaunchArguments(launchArguments);
        this.values = launchArguments;
    }
    
    @Override
    public String getNextRandomValue() {
        return values[getRand().nextInt(0, values.length - 1)];
    }
    
    @Override
    public String getName() {
        return TYPE_NAME;
    }
}
