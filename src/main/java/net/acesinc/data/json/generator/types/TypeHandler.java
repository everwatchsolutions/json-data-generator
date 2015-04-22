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
    
    public TypeHandler() {
        rand = new RandomDataGenerator();
    }
    
    public abstract Object getNextRandomValue();

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
}
