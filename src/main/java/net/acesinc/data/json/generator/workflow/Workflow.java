/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.acesinc.data.json.generator.workflow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author andrewserff
 */
public class Workflow {
    private List<WorkflowStep> steps;
    /** how often events should be generated.  i.e. time between steps */
    private long eventFrequency;
    private boolean varyEventFrequency;
    private boolean repeatWorkflow;
    private long timeBetweenRepeat;
    private boolean varyRepeatFrequency;
    private String stepRunMode;
    private long iterations = -1;
    
    public Workflow() {
        steps = new ArrayList<>();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Workflow) {
            Workflow w = (Workflow)obj;
            if (w.getEventFrequency() != eventFrequency) { 
                return false;
            } 
            if (w.isVaryEventFrequency() != varyEventFrequency) {
                return false;
            }
            if (!w.getStepRunMode().equals(stepRunMode)) {
                return false;
            }
            if (w.getIterations() != iterations) {
                return false;
            }
            
           
            List<WorkflowStep> compSteps = w.getSteps();
            if (compSteps.size() != steps.size()) {
                return false;
            }
            for (int i = 0; i < compSteps.size(); i++) {
                WorkflowStep s = steps.get(i);
                WorkflowStep compS = compSteps.get(i);
                
                if (s.getDuration() != compS.getDuration()) {
                    return false;
                }
                
                List<Map<String, Object>> configs = s.getConfig();
                List<Map<String, Object>> compConfigs = compS.getConfig();
                
                if (configs.size() != compConfigs.size()) {
                    return false;
                }
                
                for (int j = 0; j < compConfigs.size(); j++) {
                    Map<String, Object> config = configs.get(j);
                    Map<String, Object> compConfig = compConfigs.get(j);
                    
                    if (config.size() != compConfig.size()) {
                        return false;
                    }
                    
                    Set<String> keys1 = new HashSet<>(config.keySet());
                    Set<String> keys2 = new HashSet<>(compConfig.keySet());
                    if (!keys1.equals(keys2)) {
                        return false;
                    }
                    
                    Set<Object> values1 = new HashSet<>(config.values());
                    Set<Object> values2 = new HashSet<>(compConfig.values());
                    if (!values1.equals(values2)) {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }
        return true;
    }
    
    
    
    public void addStep(WorkflowStep step) {
        getSteps().add(step);
    }

    /**
     * @return the steps
     */
    public List<WorkflowStep> getSteps() {
        return steps;
    }

    /**
     * @param steps the steps to set
     */
    public void setSteps(List<WorkflowStep> steps) {
        this.steps = steps;
    }

    /**
     * @return the eventFrequency
     */
    public long getEventFrequency() {
        return eventFrequency;
    }

    /**
     * @param eventFrequency the eventFrequency to set
     */
    public void setEventFrequency(long eventFrequency) {
        this.eventFrequency = eventFrequency;
    }

    /**
     * @return the varyEventFrequency
     */
    public boolean isVaryEventFrequency() {
        return varyEventFrequency;
    }

    /**
     * @param varyEventFrequency the varyEventFrequency to set
     */
    public void setVaryEventFrequency(boolean varyEventFrequency) {
        this.varyEventFrequency = varyEventFrequency;
    }

    /**
     * @return the repeatWorkflow
     */
    public boolean isRepeatWorkflow() {
        return repeatWorkflow;
    }

    /**
     * @param repeatWorkflow the repeatWorkflow to set
     */
    public void setRepeatWorkflow(boolean repeatWorkflow) {
        this.repeatWorkflow = repeatWorkflow;
    }
    
    public boolean shouldRepeat(int currentIteration) {
        return repeatWorkflow && (iterations < 0 || currentIteration < iterations);
    }

    /**
     * @return the timeBetweenRepeat
     */
    public long getTimeBetweenRepeat() {
        return timeBetweenRepeat;
    }

    /**
     * @param timeBetweenRepeat the timeBetweenRepeat to set
     */
    public void setTimeBetweenRepeat(long timeBetweenRepeat) {
        this.timeBetweenRepeat = timeBetweenRepeat;
    }

    /**
     * @return the varyRepeatFrequency
     */
    public boolean isVaryRepeatFrequency() {
        return varyRepeatFrequency;
    }

    /**
     * @param varyRepeatFrequency the varyRepeatFrequency to set
     */
    public void setVaryRepeatFrequency(boolean varyRepeatFrequency) {
        this.varyRepeatFrequency = varyRepeatFrequency;
    }

    /**
     * @return the stepRunMode
     */
    public String getStepRunMode() {
        return stepRunMode;
    }

    /**
     * @param stepRunMode the stepRunMode to set
     */
    public void setStepRunMode(String stepRunMode) {
        this.stepRunMode = stepRunMode;
    }
    
    public long getIterations() {
        return iterations;
    }

    public void setIterations(long runCount) {
        this.iterations = runCount;
    }
    
}
