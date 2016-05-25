/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.types;

import java.util.Arrays;

/**
 *
 * @author andrewserff
 */
public class StringMergeType extends TypeHandler {

    public static final String TYPE_NAME = "stringMerge";
    public static final String TYPE_DISPLAY_NAME = "String Merge";

    private String delimiter;
    private String[] stringsToMerge;

    @Override
    public void setLaunchArguments(String[] launchArguments) {
        delimiter = launchArguments[0];
        stringsToMerge = Arrays.copyOfRange(launchArguments, 1, launchArguments.length);
    }

    @Override
    public Object getNextRandomValue() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stringsToMerge.length; i++) {
            String s = stringsToMerge[i];
            sb.append(s);
            if (i < stringsToMerge.length - 1) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    @Override
    public String getName() {
        return TYPE_NAME;
    }

}
