/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author andrewserff
 */
public class ProblemHandler extends DeserializationProblemHandler {

    private static final Logger log = LogManager.getLogger(ProblemHandler.class);

    @Override
    public boolean handleUnknownProperty(DeserializationContext dc, JsonParser jp, JsonDeserializer<?> jd, Object bean, String property) throws IOException, JsonProcessingException {
        log.debug("Handling unknown property: " + property);
        return false;
    }

}
