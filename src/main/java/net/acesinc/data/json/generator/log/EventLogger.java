/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.acesinc.data.json.generator.log;


/**
 *
 * @author andrewserff
 */
public interface EventLogger {
    public void logEvent(String event);
    public void shutdown();
}
