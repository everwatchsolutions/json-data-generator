/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.acesinc.data.json.generator.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author andrewserff
 */
public class SimulationConfig {
    private List<WorkflowConfig> workflows;
    private List<Map<String, Object>> producers;
    
    /**
     * @return the workflows
     */
    public List<WorkflowConfig> getWorkflows() {
        List<WorkflowConfig> allWorkflows = new ArrayList<>();
        for (WorkflowConfig workflowConfig: workflows) {
            int instanceCounter = workflowConfig.getInstances();
            if(instanceCounter > 0) {
                for(int i = 0 ; i<instanceCounter ; i++) {
                    allWorkflows.add(workflowConfig);
                }
            }
        }
        return allWorkflows;
    }

    /**
     * @param workflows the workflows to set
     */
    public void setWorkflows(List<WorkflowConfig> workflows) {
        this.workflows = workflows;
    }

    /**
     * @return the producers
     */
    public List<Map<String, Object>> getProducers() {
        return producers;
    }

    /**
     * @param producers the producers to set
     */
    public void setProducers(List<Map<String, Object>> producers) {
        this.producers = producers;
    }
}
