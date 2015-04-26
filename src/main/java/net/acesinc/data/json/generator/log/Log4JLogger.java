/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.acesinc.data.json.generator.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    
    public void logEvent(Map<String, Object> event) {
        try {
            dataLogger.info(mapper.writeValueAsString(event));
        } catch (JsonProcessingException ex) {
            log.error("Error processing SystemEvent into json string", ex);
        }
    }
    
}
