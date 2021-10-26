/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.acesinc.data.json.generator.config.SimulationConfig;
import net.acesinc.data.json.generator.config.WorkflowConfig;
import net.acesinc.data.json.generator.config.JSONConfigReader;
import net.acesinc.data.json.generator.log.EventLogger;
import net.acesinc.data.json.generator.workflow.Workflow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author andrewserff
 */
public class SimulationRunner {

    private static final Logger log = LogManager.getLogger(SimulationRunner.class);

    public static final MetricRegistry metrics = new MetricRegistry();
    private static final ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .convertRatesTo(TimeUnit.SECONDS)
        .build();


    private SimulationConfig config;
    private List<EventGenerator> eventGenerators;
    private List<Thread> eventGenThreads;
    private List<EventLogger> eventLoggers;
    private final Timer simDurationTimer = metrics.timer(MetricRegistry.name(SimulationRunner.class, "duration", "ms"));

    private Context durationContext;

    public SimulationRunner(SimulationConfig config, List<EventLogger> loggers) {
        this.config = config;
        this.eventLoggers = loggers;
        eventGenerators = new ArrayList<EventGenerator>();
        eventGenThreads = new ArrayList<Thread>();

        setupSimulation();
    }

    private void setupSimulation() {
        for (WorkflowConfig workflowConfig : config.getWorkflows()) {
            try {
                Workflow w = JSONConfigReader.readConfig(this.getClass().getClassLoader().getResourceAsStream(workflowConfig.getWorkflowFilename()), Workflow.class);
                final EventGenerator gen = new EventGenerator(w, workflowConfig, eventLoggers);
                log.info("Adding EventGenerator for [ " + workflowConfig.getWorkflowName()+ "," + workflowConfig.getWorkflowFilename()+ " ]");
                eventGenerators.add(gen);
                eventGenThreads.add(new Thread(gen));
            } catch (IOException ex) {
                log.error("Error reading config: " + workflowConfig.getWorkflowName(), ex);
            }
        }
    }

    public void startSimulation() {
        log.info("Starting Simulation");
        durationContext = this.simDurationTimer.time();

        eventGenThreads.parallelStream().forEach(Thread::start);
    }

    public void stopSimulation() {
        if (durationContext != null) {
            durationContext.stop();
        }

        log.info("Reporting  metrics if there are any");
        reporter.report();

        log.info("Stopping Simulation");
        for (Thread t : eventGenThreads) {
            t.interrupt();
        }
        for (EventLogger l : eventLoggers) {
            l.shutdown();
        }
    }

    public boolean isRunning() {
        return eventGenThreads.parallelStream().anyMatch(Thread::isAlive);
    }

}
