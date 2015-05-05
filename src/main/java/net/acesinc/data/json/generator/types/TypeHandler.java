/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.types;

import org.apache.commons.math3.random.RandomDataGenerator;

/**
 *
 * @author andrewserff
 */
public abstract class TypeHandler {
    private RandomDataGenerator rand;
    private String[] launchArguments;
    
    public TypeHandler() {
        rand = new RandomDataGenerator();
    }
    
    public abstract Object getNextRandomValue();
    public abstract String getName();
    
    /**
     * @return the rand
     */
    public RandomDataGenerator getRand() {
        return rand;
    }

    /**
     * @param rand the rand to set
     */
    public void setRand(RandomDataGenerator rand) {
        this.rand = rand;
    }

    /**
     * @return the launchArguments
     */
    public String[] getLaunchArguments() {
        return launchArguments;
    }

    /**
     * @param launchArguments the launchArguments to set
     */
    public void setLaunchArguments(String[] launchArguments) {
        this.launchArguments = launchArguments;
    }
    
    public static String stripQuotes(String s) {
        return s.replaceAll("'", "").replaceAll("\"", "").trim();
    }
}
