/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author andrewserff
 */
public class Log4JLogger implements EventLogger {

    private static final Logger log = LogManager.getLogger(Log4JLogger.class);
    private static final Logger dataLogger = LogManager.getLogger("data-logger");
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void logEvent(String event) {
        try {
            Object theValue = null;
            if (event.startsWith("{")) { //plain json object = Map
                theValue = mapper.readValue(event, Map.class);
            } else if (event.startsWith("[")) { //array of json objects = List
                theValue = mapper.readValue(event, List.class);
            } else { //unknown, so leave it as the literal string
                theValue = event;
            }
            dataLogger.info(mapper.writeValueAsString(theValue));
        } catch (IOException ex) {
            log.error("Error logging event", ex);
        }
    }

    @Override
    public void shutdown() {
        //nothing to shutdown
    }

}
