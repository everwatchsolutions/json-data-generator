/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.types;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ygalblum
 */
public class RandomIncrementLongType extends TypeHandler {
    public static final String TYPE_NAME = "randomIncrementLong";
    public static final String TYPE_DISPLAY_NAME = "Random Increment Long";

    private class IncrementParameters {
        public long nextValue;
        public final long minStep;
        public final long maxStep;

        public IncrementParameters(long nextValue, long minStep, long maxStep) {
            this.nextValue = nextValue;
            this.minStep = minStep;
            this.maxStep = maxStep;
        }
    };

    private String currentRandomIncrementLongName;
    private final Map<String, IncrementParameters> namedRandomIncrementLongMap;

    public RandomIncrementLongType(){
        namedRandomIncrementLongMap = new HashMap<>();
    }
    
    @Override
    public void setLaunchArguments(String[] launchArguments) {
        if (launchArguments.length < 1) {
            throw new IllegalArgumentException("Arguments list is too short");
        }
        currentRandomIncrementLongName = launchArguments[0];
        if (namedRandomIncrementLongMap.get(currentRandomIncrementLongName) != null) {
            return;
        }

        long nextValue;
        long minStep;
        long maxStep;

        super.setLaunchArguments(launchArguments);
        switch (launchArguments.length) {
            case 1:
                nextValue = 0;
                minStep = 0;
                maxStep = Long.MAX_VALUE;
                break;
            case 2:
                nextValue = Long.parseLong(launchArguments[1]);
                minStep = 0;
                maxStep = Long.MAX_VALUE;
                break;
            case 3:
                nextValue = Long.parseLong(launchArguments[1]);
                minStep = Long.parseLong(launchArguments[2]);
                maxStep = Long.MAX_VALUE;
                break;
            case 4:
                nextValue = Long.parseLong(launchArguments[1]);
                minStep = Long.parseLong(launchArguments[2]);
                maxStep = Long.parseLong(launchArguments[3]);
                break;
            default:
                return;
        }

        namedRandomIncrementLongMap.put(currentRandomIncrementLongName, new IncrementParameters(nextValue, minStep, maxStep));
    }
    
    @Override
    public Object getNextRandomValue() {
        IncrementParameters incrementParameters = namedRandomIncrementLongMap.get(currentRandomIncrementLongName);
        long value = incrementParameters.nextValue;
        incrementParameters.nextValue += getRand().nextLong(incrementParameters.minStep, incrementParameters.maxStep);
        return value;
    }

    @Override
    public String getName() {
        return TYPE_NAME;
    } 
}
