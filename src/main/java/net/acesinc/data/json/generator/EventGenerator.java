/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.acesinc.data.json.generator.log.EventLogger;
import net.acesinc.data.json.generator.workflow.Workflow;
import net.acesinc.data.json.generator.workflow.WorkflowStep;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    public EventGenerator(Workflow workflow, String generatorName, List<EventLogger> loggers) {
        this.workflow = workflow;
        this.generatorName = generatorName;
        this.eventLoggers = loggers;
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
        while (running && it.hasNext()) {
            WorkflowStep step = it.next();
            executeStep(step);

            if (!it.hasNext() && workflow.isRepeatWorkflow()) {
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
        while (running && it.hasNext()) {
            WorkflowStep step = it.next();
            executeStep(step);

            if (!it.hasNext() && workflow.isRepeatWorkflow()) {
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
        while (running) {
            WorkflowStep step = workflow.getSteps().get(generateRandomNumber(0, workflow.getSteps().size() - 1));;
            executeStep(step);

            if (workflow.isRepeatWorkflow()) {
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
        if (step.getDuration() == 0) {
            //Just generate this event and move on to the next one
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
        } else if (step.getDuration() == -1) {
            //Run this step forever
            //They want to continue generating events of this step over a duration
            List<Map<String, Object>> configs = step.getConfig();
            while (running) {
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
                        break;
                    }
                } catch (IOException ioe) {
                    log.error("Error generating json event", ioe);
                }
            }
        } else {
            //They want to continue generating events of this step over a duration
            long now = new Date().getTime();
            long stopTime = now + step.getDuration();
            List<Map<String, Object>> configs = step.getConfig();
            while (new Date().getTime() < stopTime && running) {
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
                        break;
                    }
                } catch (IOException ioe) {
                    log.error("Error generating json event", ioe);
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
        RandomJsonGenerator generator = new RandomJsonGenerator(config);
        return generator.generateJson();
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
