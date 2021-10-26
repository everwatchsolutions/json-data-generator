/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;
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

    private SimulationConfig config;
    private List<EventGenerator> eventGenerators;
    private List<Thread> eventGenThreads;
    private List<EventLogger> eventLoggers;

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

        eventGenThreads.parallelStream().forEach(Thread::start);
    }

    public void stopSimulation() {
        log.info("Dumping " + metrics.getMetrics().size() + " metrics");
        metrics.getMetrics().forEach((key, metric) -> {
            if (metric instanceof Timer) {
                log.info("timer: " + key);
                Timer timer = (Timer) metric;
                log.info("count: " + timer.getCount());
                log.info("\tmean rate: " + timer.getMeanRate());
                log.info("\tone minute rate: " + timer.getOneMinuteRate());
                log.info("\tfive minute rate: " + timer.getFiveMinuteRate());
                log.info("\tfifteen minute rate: " + timer.getFifteenMinuteRate());
            } else if (metric instanceof Gauge) {
                log.info("gauge: " + key);
                log.info("\tvalue: " + ((Gauge<?>) metric).getValue());
            } else if (metric instanceof Counter) {
                log.info("counter: " + key);
                log.info("\tvalue: " + ((Counter) metric).getCount());
            }
            log.info("----------------------------------------");
        });

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
