/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.types;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author andrewserff
 */
public class RandomType extends TypeHandler {

    public static final String TYPE_NAME = "random";
    public static final String TYPE_DISPLAY_NAME = "Random";

    private List<Object> typedValues;

    @Override
    public void setLaunchArguments(String[] launchArguments) {
        super.setLaunchArguments(launchArguments);
        typedValues = new ArrayList<>();
        for (String s : launchArguments) {
            try {
                if (s.contains("\"") || s.contains("'")) {
                    typedValues.add(stripQuotes(s));
                } else {
                    if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false")) {
                        typedValues.add(Boolean.parseBoolean(s));
                    } else if (s.contains(".")) {
                        typedValues.add(Double.parseDouble(s));
                    } else {
                        typedValues.add(Long.parseLong(s));
                    }
                }
            } catch (Throwable t) {
                //error parsing, just assume string then
                typedValues.add(stripQuotes(s));
            }
        }
    }

    @Override
    public Object getNextRandomValue() {
        return typedValues.get(getRand().nextInt(0, typedValues.size() - 1));
    }

    @Override
    public String getName() {
        return TYPE_NAME;
    }

    
}
