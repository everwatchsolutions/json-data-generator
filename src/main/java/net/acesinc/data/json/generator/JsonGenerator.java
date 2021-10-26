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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.acesinc.data.json.generator.config.JSONConfigReader;
import net.acesinc.data.json.generator.config.WorkflowConfig;

/**
 *
 * @author andrewserff
 */
public class JsonGenerator {

    private static final Logger log = LogManager.getLogger(JsonGenerator.class);
    private WorkflowConfig workflowConfig;

    public JsonGenerator() throws IOException {
        workflowConfig = new WorkflowConfig();
    }

    public Map<String, Object> testMapGenerator(String config) throws IOException {
        Map<String, Object> props = JSONConfigReader.readConfig(this.getClass().getClassLoader().getResourceAsStream(config), Map.class);
        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put(null, props);
        RandomJsonGenerator generator = new RandomJsonGenerator(workflowConfig);
        Map<String, Object> map = generator.generateJsonMap(wrapper);
        return map;
    }
    public String testFlatJsonGenerator(String config) throws IOException {
        Map<String, Object> props = JSONConfigReader.readConfig(this.getClass().getClassLoader().getResourceAsStream(config), Map.class);
        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put(null, props);
        RandomJsonGenerator generator = new RandomJsonGenerator(workflowConfig);
        String json = generator.generateFlattnedJson(wrapper);
        return json;
    }
    public List<Map<String, Object>> testListGenerator(String config) throws IOException {
        List<Map<String, Object>> props = JSONConfigReader.readConfig(this.getClass().getClassLoader().getResourceAsStream(config), List.class);
        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put(null, props);
        RandomJsonGenerator generator = new RandomJsonGenerator(workflowConfig);
        List<Map<String, Object>> list = generator.generateJsonList(wrapper);
        return list;
    }

    public static void main(String... args) {
        String config = "config-array-test.json";
//        String config = "config1.json";
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonGenerator gen = new JsonGenerator();
            log.info("Generated json Map: " + mapper.writeValueAsString(gen.testMapGenerator(config)));
            log.info("Generated flattened json Map: " + gen.testFlatJsonGenerator(config));
            JsonGenerator gen2 = new JsonGenerator();
            log.info("Generated json List: " + gen2.testListGenerator("config3.json"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
