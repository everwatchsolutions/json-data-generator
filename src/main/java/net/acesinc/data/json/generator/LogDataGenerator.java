/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator;

import java.io.IOException;
import net.acesinc.data.json.generator.config.SimulationConfig;
import net.acesinc.data.json.generator.config.JSONConfigReader;
import net.acesinc.data.json.generator.log.Log4JLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author andrewserff
 */
public class LogDataGenerator {

    private static final Logger log = LogManager.getLogger(LogDataGenerator.class);

    private SimulationRunner simRunner;
    private String simConfigFile;

    public LogDataGenerator(String simConfig) {
        simConfigFile = simConfig;
        try {
            log.debug("Creating Simulation Runner using Simulation Config [ " + simConfig + " ]");
            simRunner = new SimulationRunner(getSimConfig(), new Log4JLogger());
        } catch (IOException ex) {
            log.error("Error getting Simulation Config [ " + simConfig + " ]");
        }
    }

    public void startRunning() {
        simRunner.startSimulation();
    }
    public void stopRunning() {
        simRunner.stopSimulation();
    }

    private SimulationConfig getSimConfig() throws IOException {
        return JSONConfigReader.readConfig(this.getClass().getClassLoader().getResourceAsStream(simConfigFile), SimulationConfig.class);
    }

    public boolean isRunning() {
        return simRunner.isRunning();
    }

    public static void main(String[] args) {
        String simConfig = "defaultSimConfig.json";
        if (args.length > 0) {
            simConfig = args[0];
            log.info("Overriding Simulation Config file from command line to use [ " + simConfig + " ]");
        }

        final LogDataGenerator gen = new LogDataGenerator(simConfig);

        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                log.info("Shutdown Hook Invoked.  Shutting Down Loggers");
                gen.stopRunning();
                try {
                    mainThread.join();
                } catch (InterruptedException ex) {
                    //oh well
                }
            }
        });

        gen.startRunning();
        while (gen.isRunning()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                //wakie wakie!
            }
        }

    }

    /**
     * @return the simConfigFile
     */
    public String getSimConfigFile() {
        return simConfigFile;
    }

    /**
     * @param simConfigFile the simConfigFile to set
     */
    public void setSimConfigFile(String simConfigFile) {
        this.simConfigFile = simConfigFile;
    }

}
