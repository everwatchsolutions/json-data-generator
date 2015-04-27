/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.acesinc.data.json.generator.config;

import java.util.List;
import java.util.Map;

/**
 *
 * @author andrewserff
 */
public class SimulationConfig {
    private List<WorkflowConfig> workflowList;
    private List<Map<String, Object>> producers;
    
    /**
     * @return the workflowList
     */
    public List<WorkflowConfig> getWorkflowList() {
        return workflowList;
    }

    /**
     * @param workflowList the workflowList to set
     */
    public void setWorkflowList(List<WorkflowConfig> workflowList) {
        this.workflowList = workflowList;
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
