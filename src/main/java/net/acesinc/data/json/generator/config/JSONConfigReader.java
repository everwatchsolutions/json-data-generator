/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.acesinc.data.json.generator.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author andrewserff
 */
public class JSONConfigReader {
    private static final Logger log = LogManager.getLogger(JSONConfigReader.class);
    
    public static String getJsonConfig(Object o) {
        ObjectMapper mapper = new ObjectMapper();
        String json = null;
        try {
            json = mapper.writeValueAsString(o);
        } catch (JsonProcessingException ex) {
            log.warn("Error parsing object into json", ex);
        }
        return json;
    }
    
    public static <T> T readConfig(File input, Class<T> targetClass) throws IOException {
        return readConfig(new FileInputStream(input), targetClass);
    }
    
    public static <T> T readConfig(InputStream input, Class<T> targetClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(input, targetClass);
    }
}
