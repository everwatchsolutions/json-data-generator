/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.acesinc.data.json.generator.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author andrewserff
 */
public class WorkflowStep {
    private List<Map<String, Object>> config;
    private Map<String, Object> producerConfig;
    private long duration = 0;
    private long iterations = -1;

    public WorkflowStep() {
        config = new ArrayList<Map<String, Object>>();
        producerConfig = new HashMap<>() ;
    }

    /**
     * @return the duration
     */
    public long getDuration() {
        return duration;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * @return the config
     */
    public List<Map<String, Object>> getConfig() {
        return config;
    }

    /**
     * @param config the config to set
     */
    public void setConfig(List<Map<String, Object>> config) {
        this.config = config;
    }

        /**
     * @return the producerConfig
     */
    public Map<String, Object> getProducerConfig() {
        return producerConfig;
    }

    /**
     * @param producerConfig the producerConfig to set
     */
    public void setProducerConfig(Map<String, Object> producerConfig) {
        this.producerConfig = producerConfig;
    }

    public long getIterations() {
        return iterations;
    }

    public void setIterations(long iterations) {
        this.iterations = iterations;
    }

}
