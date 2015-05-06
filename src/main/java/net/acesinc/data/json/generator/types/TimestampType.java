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
public class TimestampType extends BaseDateType {

    public static final String TYPE_NAME = "timestamp";
    public static final String TYPE_DISPLAY_NAME = "Timestamp";

    public TimestampType() {
    }
    
    @Override
    public Long getNextRandomValue() {
        return getRandomDate().getTime();
    }
    
    @Override
    public String getName() {
        return TYPE_NAME;
    }
}
