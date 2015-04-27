/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.acesinc.data.json.generator.config.JSONConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author andrewserff
 */
public class JsonGenerator {
    
    private static final Logger log = LogManager.getLogger(JsonGenerator.class);
    
    public JsonGenerator() throws IOException {
    }
    
    public Map<String, Object> testMapGenerator(String config) throws IOException {
        Map<String, Object> props = JSONConfigReader.readConfig(this.getClass().getClassLoader().getResourceAsStream(config), Map.class);
        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put(null, props);
        RandomJsonGenerator generator = new RandomJsonGenerator(wrapper);
        Map<String, Object> map = generator.generateJsonMap();
        return map;
    }
    public List<Map<String, Object>> testListGenerator(String config) throws IOException {
        List<Map<String, Object>> props = JSONConfigReader.readConfig(this.getClass().getClassLoader().getResourceAsStream(config), List.class);
        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put(null, props);
        RandomJsonGenerator generator = new RandomJsonGenerator(wrapper);
        List<Map<String, Object>> list = generator.generateJsonList();
        return list;
    }
    
    public static void main(String... args) {
        String config = "config4.json";
        try {
            JsonGenerator gen = new JsonGenerator();
            log.info("Generated json Map: " + gen.testMapGenerator(config));
            JsonGenerator gen2 = new JsonGenerator();
            log.info("Generated json List: " + gen2.testListGenerator("config3.json"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
