/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.acesinc.data.json.generator.config;

import java.util.List;

/**
 *
 * @author andrewserff
 */
public class SimulationConfig {
    private List<WorkflowConfig> workflowList;

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
}
