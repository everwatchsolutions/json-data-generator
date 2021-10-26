/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator;

import com.codahale.metrics.MetricRegistry;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.acesinc.data.json.generator.config.WorkflowConfig;
import net.acesinc.data.json.generator.log.EventLogger;
import net.acesinc.data.json.generator.workflow.Workflow;
import net.acesinc.data.json.generator.workflow.WorkflowStep;

/**
 *
 * @author andrewserff
 */
public class EventGenerator implements Runnable {

    private static final Logger log = LogManager.getLogger(EventGenerator.class);

    private Workflow workflow;
    private String generatorName;
    private boolean running;
    private List<EventLogger> eventLoggers;
    private long startTime;
    private long generatedEvents = 0;
    private RandomJsonGenerator generator;
    private WorkflowConfig workflowConfig;

    public EventGenerator(Workflow workflow, WorkflowConfig workflowConfig, List<EventLogger> loggers) {
        this.workflow = workflow;
        this.workflowConfig = workflowConfig;
        this.generatorName = workflowConfig.getWorkflowName();
        this.eventLoggers = loggers;
        this.generator = new RandomJsonGenerator(workflowConfig);
    }

    public void runWorkflow() {
        String runMode = "sequential";
        if (workflow.getStepRunMode() != null) {
            runMode = workflow.getStepRunMode();
        }

        startTime = System.currentTimeMillis();
        switch (runMode) {
            case "sequential":
                runSequential();
                break;
            case "random":
                runRandom();
                break;
            case "random-pick-one":
                runRandomPickOne();
                break;
            default:
                runSequential();
                break;
        }
    }

    protected void runSequential() {
        Iterator<WorkflowStep> it = workflow.getSteps().iterator();
        int i = 1;
        while (running && it.hasNext()) {
            WorkflowStep step = it.next();
            executeStep(step);

            if (!it.hasNext() && workflow.shouldRepeat(i++)) {
                it = workflow.getSteps().iterator();
                try {
                    performWorkflowSleep(workflow);
                } catch (InterruptedException ie) {
                    //wake up!
                    running = false;
                    break;
                }
            }

        }
    }

    protected void runRandom() {
        List<WorkflowStep> stepsCopy = new ArrayList<>(workflow.getSteps());
        Collections.shuffle(stepsCopy, new Random(System.currentTimeMillis()));

        Iterator<WorkflowStep> it = stepsCopy.iterator();
        int i = 1;
        while (running && it.hasNext()) {
            WorkflowStep step = it.next();
            executeStep(step);

            if (!it.hasNext() && workflow.shouldRepeat(i++)) {
                Collections.shuffle(stepsCopy, new Random(System.currentTimeMillis()));
                it = stepsCopy.iterator();
                try {
                    performWorkflowSleep(workflow);
                } catch (InterruptedException ie) {
                    //wake up!
                    running = false;
                    break;
                }
            }

        }
    }

    protected void runRandomPickOne() {
        int i = 1;
        while (running) {
            WorkflowStep step = workflow.getSteps().get(generateRandomNumber(0, workflow.getSteps().size() - 1));;
            executeStep(step);

            if (workflow.shouldRepeat(i++)) {
                try {
                    performWorkflowSleep(workflow);
                } catch (InterruptedException ie) {
                    //wake up!
                    running = false;
                    break;
                }
            }
        }
    }

    protected void executeStep(WorkflowStep step) {
        int i = 0;
        if (step.getDuration() == 0) {
            if(step.getIterations() == -1) {
                //Just generate this event and move on to the next one
                executeAllConfigs(step);
            } else {
                // Run for the number of iterations
                while (running && i++ < step.getIterations()) {
                    executeRandomConfig(step);
                }
            }
        } else if (step.getDuration() == -1) {
            if(step.getIterations() == -1) {
                //Run this step forever
                while(running) {
                    executeRandomConfig(step);
                }
            } else {
                //Run for the number of iterations
                while (running && i++ < step.getIterations()) {
                    executeRandomConfig(step);
                }
            }
        } else {
            long now = new Date().getTime();
            long stopTime = now + step.getDuration();
            if(step.getIterations() == -1) {
                //They want to continue generating events of this step over a duration
                while (running && new Date().getTime() < stopTime) {
                    executeRandomConfig(step);
                }
            } else {
                //They want to continue generating events of this step over a duration and a number of iterations.
                //Which ever ends first.
                while (running && new Date().getTime() < stopTime && i++ < step.getIterations()) {
                    executeRandomConfig(step);
                }
            }
        }

        if (log.isTraceEnabled()) {
            generatedEvents++;

            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > 1000) { //1sec

                double recordsPerSec = generatedEvents / (elapsed / 1000);

                log.trace("Generator( " + generatorName + " ) generated " + recordsPerSec + " events/sec");

                startTime = System.currentTimeMillis();
                generatedEvents = 0;
            }
        }
    }

    private void executeAllConfigs(WorkflowStep step) {
        for (Map<String, Object> config : step.getConfig()) {
            Map<String, Object> wrapper = new LinkedHashMap<>();
            wrapper.put(null, config);
            try {
                String event = generateEvent(wrapper);
                for (EventLogger l : eventLoggers) {
                    l.logEvent(event, step.getProducerConfig());
                }
                try {
                    performEventSleep(workflow);
                } catch (InterruptedException ie) {
                    //wake up!
                    running = false;
                    break;
                }
            } catch (IOException ioe) {
                log.error("Error generating json event", ioe);
            }
        }
    }

    private void executeRandomConfig(WorkflowStep step) {
        List<Map<String, Object>> configs = step.getConfig();
        try {
            Map<String, Object> wrapper = new LinkedHashMap<>();
            wrapper.put(null, configs.get(generateRandomNumber(0, configs.size() - 1)));
            String event = generateEvent(wrapper);
            for (EventLogger l : eventLoggers) {
                l.logEvent(event, step.getProducerConfig());
            }
            try {
                performEventSleep(workflow);
            } catch (InterruptedException ie) {
                //wake up!
                running = false;
            }
        } catch (IOException ioe) {
            log.error("Error generating json event", ioe);
        }
    }


    private void performEventSleep(Workflow workflow) throws InterruptedException {
        long durationBetweenEvents = workflow.getEventFrequency();
        if (workflow.isVaryEventFrequency()) {
            long minSleep = durationBetweenEvents - durationBetweenEvents / 2;
            long maxSleep = durationBetweenEvents;
            Thread.sleep(generateRandomNumber(minSleep, maxSleep));
        } else {
            Thread.sleep(durationBetweenEvents);
        }
    }

    private void performWorkflowSleep(Workflow workflow) throws InterruptedException {
        if (workflow.getTimeBetweenRepeat() > 0) {
            if (workflow.isVaryRepeatFrequency()) {
                long sleepDur = workflow.getTimeBetweenRepeat();
                long minSleep = sleepDur - sleepDur / 2;
                long maxSleep = sleepDur;
                Thread.sleep(generateRandomNumber(minSleep, maxSleep));
            } else {
                Thread.sleep(workflow.getTimeBetweenRepeat());
            }
        }
    }

    public String generateEvent(Map<String, Object> config) throws IOException {
        final long start = System.currentTimeMillis();
        String json = generator.generateJson(config);

        SimulationRunner.metrics.timer(
            MetricRegistry.name(EventGenerator.class, "event", "generation", "duration", "ms"))
            .update(Duration.ofMillis(System.currentTimeMillis() - start));

        return json;
    }

    private int generateRandomNumber(int min, int max) {
        return min + (int) (Math.random() * ((max - min) + 1));
    }

    private long generateRandomNumber(long min, long max) {
        return min + (long) (Math.random() * ((max - min) + 1));
    }

    public void run() {
        try {
            setRunning(true);
            runWorkflow();
            setRunning(false);
        } catch (Throwable ie) {
            log.fatal("Exception occured causing the Generator to shutdown", ie);
            setRunning(false);
        }
    }

    /**
     * @return the running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * @param running the running to set
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

}
