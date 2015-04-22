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

    public LongType(String... args) {
        super();
        if (args.length == 0) {
            min = 0;
            max = Long.MAX_VALUE;
        } else if (args.length == 1) {
            //min only
            min = Long.parseLong(args[0]);
            max = Long.MAX_VALUE;
        } else if (args.length == 2) {
            min = Long.parseLong(args[0]);
            max = Long.parseLong(args[1]);
        }
    }

    @Override
    public Long getNextRandomValue() {
        return getRand().nextLong(min, max);
    }

}
