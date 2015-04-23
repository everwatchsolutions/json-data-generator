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
public class DoubleType extends TypeHandler {

    public static final String TYPE_NAME = "double";
    public static final String TYPE_DISPLAY_NAME = "Double";

    private double min;
    private double max;
    private Random rand;
    private int decimalPlaces = 4;

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
        rand = new Random();

    }

    @Override
    public Double getNextRandomValue() {
//        return RandomUtils.nextDouble(min, max);

        double range = max - min;
        double scaled = rand.nextDouble() * range;
        double shifted = scaled + min;
//        return shifted; // == (rand.nextDouble() * (max-min)) + min;
        return Double.parseDouble(String.format("%." + decimalPlaces + "f", shifted));

    }

}
