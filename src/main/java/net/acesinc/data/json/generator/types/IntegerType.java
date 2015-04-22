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

    public IntegerType(String... args) {
        super();
        if (args.length == 0) {
            min = 0;
            max = Integer.MAX_VALUE;
        } else if (args.length == 1) {
            //min only
            min = Integer.parseInt(args[0]);
            max = Integer.MAX_VALUE;
        } else if (args.length == 2) {
            min = Integer.parseInt(args[0]);
            max = Integer.parseInt(args[1]);
        }
    }

    @Override
    public Integer getNextRandomValue() {
        return getRand().nextInt(min, max);
    }

}
