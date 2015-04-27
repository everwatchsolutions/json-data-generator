/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.acesinc.data.json.generator.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
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
            Map<String, Object> eventMap = mapper.readValue(event, Map.class);
            dataLogger.info(mapper.writeValueAsString(eventMap));
        } catch (IOException ex) {
            log.error("Error logging event", ex);
        }
    }

    @Override
    public void shutdown() {
        //nothing to shutdown
    }
    
}
