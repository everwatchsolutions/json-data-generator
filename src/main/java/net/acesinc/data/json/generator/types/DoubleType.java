/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.types;

import org.apache.commons.lang3.RandomUtils;

/**
 *
 * @author andrewserff
 */
public class DoubleType extends TypeHandler {

    public static final String TYPE_NAME = "double";
    public static final String TYPE_DISPLAY_NAME = "Double";

    private double min;
    private double max;

    public DoubleType(String... args) {
        super();
        if (args.length == 0) {
            min = 0;
            max = Double.MAX_VALUE;
        } else if (args.length == 1) {
            //min only
            min = Double.parseDouble(args[0]);
            max = Double.MAX_VALUE;
        } else if (args.length == 2) {
            min = Double.parseDouble(args[0]);
            max = Double.parseDouble(args[1]);
        }
    }

    @Override
    public Double getNextRandomValue() {
        return RandomUtils.nextDouble(min, max);
    }

}
