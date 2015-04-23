/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.types;

import java.util.Date;

/**
 *
 * @author andrewserff
 */
public class NowTimestampType extends TypeHandler {
    public static final String TYPE_NAME = "nowTimestamp";
    public static final String TYPE_DISPLAY_NAME = "Now Timestamp";

    @Override
    public Long getNextRandomValue() {
        return new Date().getTime();
    }
            
}
