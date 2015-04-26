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
public class LongType extends TypeHandler {

    public static final String TYPE_NAME = "long";
    public static final String TYPE_DISPLAY_NAME = "Long";

    private long min;
    private long max;

    public LongType() {
    }
    
    @Override
    public void setLaunchArguments(String[] launchArguments) {
        super.setLaunchArguments(launchArguments);
        if (launchArguments.length == 0) {
            min = 0;
            max = Long.MAX_VALUE;
        } else if (launchArguments.length == 1) {
            //min only
            min = Long.parseLong(launchArguments[0]);
            max = Long.MAX_VALUE;
        } else if (launchArguments.length == 2) {
            min = Long.parseLong(launchArguments[0]);
            max = Long.parseLong(launchArguments[1]);
        }
    }

    @Override
    public Long getNextRandomValue() {
        return getRand().nextLong(min, max);
    }
    
    @Override
    public String getName() {
        return TYPE_NAME;
    }

}
