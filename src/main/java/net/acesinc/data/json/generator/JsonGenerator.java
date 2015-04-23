/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator;

import java.io.IOException;
import java.util.Map;
import net.acesinc.data.json.generator.config.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author andrewserff
 */
public class JsonGenerator {
    
    private static final Logger log = LogManager.getLogger(JsonGenerator.class);
    
    public JsonGenerator(String config) throws IOException {
        //First generate the config
        Map<String, Object> props = ConfigReader.readConfig(this.getClass().getClassLoader().getResourceAsStream(config));
        log.debug("Map: " + props.toString());
        
        //now generate the json
        RandomJsonGenerator generator = new RandomJsonGenerator(props);
        log.info("Gernerated json: " + generator.generateJson());
    }
    
    public static void main(String... args) {
        String config = "config1.json";
        try {
            JsonGenerator gen = new JsonGenerator(config);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
