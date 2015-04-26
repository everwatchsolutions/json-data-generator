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
public class IntegerType extends TypeHandler {

    public static final String TYPE_NAME = "integer";
    public static final String TYPE_DISPLAY_NAME = "Integer";

    private int min;
    private int max;

    @Override
    public void setLaunchArguments(String[] launchArguments) {
        super.setLaunchArguments(launchArguments);
        if (launchArguments.length == 0) {
            min = 0;
            max = Integer.MAX_VALUE;
        } else if (launchArguments.length == 1) {
            //min only
            min = Integer.parseInt(launchArguments[0]);
            max = Integer.MAX_VALUE;
        } else if (launchArguments.length == 2) {
            min = Integer.parseInt(launchArguments[0]);
            max = Integer.parseInt(launchArguments[1]);
        }
    }

    @Override
    public Integer getNextRandomValue() {
        return getRand().nextInt(min, max);
    }

    @Override
    public String getName() {
        return TYPE_NAME;
    }
}
