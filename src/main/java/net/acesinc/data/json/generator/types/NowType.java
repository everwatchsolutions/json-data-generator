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
public class NowType extends TypeHandler {
    public static final String TYPE_NAME = "now";
    public static final String TYPE_DISPLAY_NAME = "Now";

    @Override
    public Date getNextRandomValue() {
        return new Date();
    }
            
}
