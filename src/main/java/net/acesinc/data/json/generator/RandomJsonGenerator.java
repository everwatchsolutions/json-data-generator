/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.acesinc.data.json.generator.config.WorkflowConfig;
import net.acesinc.data.json.generator.types.TypeHandler;
import net.acesinc.data.json.generator.types.TypeHandlerFactory;
import net.acesinc.data.json.util.JsonUtils;

/**
 *
 * @author andrewserff
 */
public class RandomJsonGenerator {

    private static final Logger log = LogManager.getLogger(RandomJsonGenerator.class);
    private SimpleDateFormat iso8601DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private Map<String, Object> config;
    private static JsonGeneratorFactory factory = Json.createGeneratorFactory(null);
    private Map<String, Object> generatedValues;
    private JsonUtils jsonUtils;
    private WorkflowConfig workflowConfig;

    public RandomJsonGenerator(Map<String, Object> config, WorkflowConfig workflowConfig) {
        this.config = config;
        this.workflowConfig = workflowConfig;
        jsonUtils = new JsonUtils();
        TypeHandlerFactory.getInstance().configure(workflowConfig);
    }

    public String generateJson() {
        StringWriter w = new StringWriter();
        javax.json.stream.JsonGenerator gen = factory.createGenerator(w);
        generatedValues = new LinkedHashMap<>();

        processProperties(gen, config, "");

        gen.flush();
        return w.toString();
    }

    public String generateFlattnedJson() throws IOException {
        String json = generateJson();
        return jsonUtils.flattenJson(json);
    }

    public Map<String, Object> generateJsonMap() throws IOException {
        String json = generateJson();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, Map.class);
    }

    public List<Map<String, Object>> generateJsonList() throws IOException {
        String json = generateJson();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, List.class);
    }

    private javax.json.stream.JsonGenerator processProperties(javax.json.stream.JsonGenerator gen, Map<String, Object> props, String currentContext) {
        for (String propName : props.keySet()) {
            Object value = props.get(propName);
            if (value == null) {
                generatedValues.put(currentContext + propName, null);
                addValue(gen, propName, null);
            } else if (String.class.isAssignableFrom(value.getClass())) {
                String type = (String) value;

                handleStringGeneration(type, currentContext, gen, propName);
            } else if (Map.class.isAssignableFrom(value.getClass())) {
                //nested object
                Map<String, Object> nestedProps = (Map<String, Object>) value;
                if (propName == null) {
                    gen.writeStartObject();
                } else {
                    gen.writeStartObject(propName);
                }
                String newContext = "";
                if (propName != null) {
                    if (currentContext.isEmpty()) {
                        newContext = propName + ".";
                    } else {
                        newContext = currentContext + propName + ".";
                    }
                }
                processProperties(gen, nestedProps, newContext);
                gen.writeEnd();
            } else if (List.class.isAssignableFrom(value.getClass())) {
                //array
                List<Object> listOfItems = (List<Object>) value;
                String newContext = "";
                if (propName != null) {
                    gen.writeStartArray(propName);

                    if (currentContext.isEmpty()) {
                        newContext = propName;
                    } else {
                        newContext = currentContext + propName;
                    }
                } else {
                    gen.writeStartArray();
                }

                if (!listOfItems.isEmpty()) {
                    //Check if this is a special function at the start of the array
                    boolean wasSpecialCase = false;
                    if (String.class.isAssignableFrom(listOfItems.get(0).getClass()) && ((String) listOfItems.get(0)).contains("(")) {
                        //special function in array
                        String name = (String) listOfItems.get(0);
                        String specialFunc = null;
                        String[] specialFuncArgs = {};

                        specialFunc = name.substring(0, name.indexOf("("));
                        String args = name.substring(name.indexOf("(") + 1, name.indexOf(")"));

                        if (!args.isEmpty()) {
                            specialFuncArgs = args.split(",");
                        }

                        switch (specialFunc) {
                            case "repeat": {
                                int timesToRepeat = 1;
                                if (specialFuncArgs.length == 1) {
                                    timesToRepeat = Integer.parseInt(specialFuncArgs[0]);
                                } else {
                                    timesToRepeat = new RandomDataGenerator().nextInt(0, 10);
                                }
                                List<Object> subList = listOfItems.subList(1, listOfItems.size());
                                for (int i = 0; i < timesToRepeat; i++) {
                                    processList(subList, gen, newContext);
                                }
                                wasSpecialCase = true;
                                break;
                            }
                            case "random": { //choose one of the items in the list at random
                                List<Object> subList = listOfItems.subList(1, listOfItems.size());
                                Object item = subList.get(new RandomDataGenerator().nextInt(0, subList.size() - 1));
                                processItem(item, gen, newContext + "[0]");
                                wasSpecialCase = true;
                                break;
                            }
                        }
                    }

                    if (!wasSpecialCase) { //it's not a special function, so process it like normal
                        processList(listOfItems, gen, newContext);
                    }
                }
                gen.writeEnd();
            } else {
                //literals
                generatedValues.put(currentContext + propName, value);
                addValue(gen, propName, value);
            }
        }

        return gen;
    }

    protected void handleStringGeneration(String type, String currentContext, JsonGenerator gen, String propName) {
        if (type.startsWith("this.") || type.startsWith("cur.")) {
            String refPropName = null;
            if (type.startsWith("this.")) {
                refPropName = type.substring("this.".length(), type.length());
            } else if (type.startsWith("cur.")) {
                refPropName = currentContext + type.substring("cur.".length(), type.length());
            }
            Object refPropValue = generatedValues.get(refPropName);
            if (refPropValue != null) {
                addValue(gen, propName, refPropValue);
            } else {
                log.warn("Sorry, unable to reference property [ " + refPropName + " ]. Maybe it hasn't been generated yet?");
            }
        } else {
            try {
                TypeHandler th = TypeHandlerFactory.getInstance().getTypeHandler(type, generatedValues, currentContext);

                if (th != null) {
                    Object val = th.getNextRandomValue();
//                            outputValues.put(propName, val);
                    generatedValues.put(currentContext + propName, val);
                    addValue(gen, propName, val);
                } else {
//                            log.debug("Unknown Type: [ " + type + " ] for prop [ " + propName + " ]. Attempting to echo literal value.");
//                            outputValues.put(propName, type);
                    generatedValues.put(currentContext + propName, type);
                    addValue(gen, propName, type);
                }
            } catch (IllegalArgumentException iae) {
                log.warn("Error creating type [ " + type + " ]. Prop [ " + propName + " ] being ignored in output.  Reason: " + iae.getMessage());
                log.debug("Error creating type [ " + type + " ]. Prop [ " + propName + " ] being ignored in output.", iae);
            }
        }
    }

    protected void processList(List<Object> listOfItems, JsonGenerator gen, String currentContext) {
        for (int i = 0; i < listOfItems.size(); i++) {
            Object item = listOfItems.get(i);
            String newContext = currentContext + "[" + i + "]";
            processItem(item, gen, newContext);
        }
    }

    protected void processItem(Object item, JsonGenerator gen, String currentContext) {
        if (String.class.isAssignableFrom(item.getClass())) {
            //process it like normal
            handleStringGeneration((String) item, currentContext, gen, null);
        } else if (Map.class.isAssignableFrom(item.getClass())) {
            Map<String, Object> nestedProps = (Map<String, Object>) item;
            gen.writeStartObject();

            processProperties(gen, nestedProps, currentContext + ".");
            gen.writeEnd();
        }
    }

    private javax.json.stream.JsonGenerator addValue(javax.json.stream.JsonGenerator gen, String propName, Object val) {
        if (val == null) {
            gen.writeNull(propName);
        } else if (String.class.isAssignableFrom(val.getClass())) {
            if (propName != null) {
                gen.write(propName, (String) val);
            } else {
                gen.write((String) val);
            }
        } else if (Boolean.class.isAssignableFrom(val.getClass())) {
            if (propName != null) {
                gen.write(propName, (Boolean) val);
            } else {
                gen.write((Boolean) val);
            }
        } else if (Long.class.isAssignableFrom(val.getClass())) {
            if (propName != null) {
                gen.write(propName, (Long) val);
            } else {
                gen.write((Long) val);
            }
        } else if (Integer.class.isAssignableFrom(val.getClass())) {
            if (propName != null) {
                gen.write(propName, (Integer) val);
            } else {
                gen.write((Integer) val);
            }
        } else if (Double.class.isAssignableFrom(val.getClass())) {
            if (propName != null) {
                gen.write(propName, (Double) val);
            } else {
                gen.write((Double) val);
            }
        } else if (Date.class.isAssignableFrom(val.getClass())) {
            if (propName != null) {
                gen.write(propName, iso8601DF.format((Date) val));
            } else {
                gen.write(iso8601DF.format((Date) val));
            }
        }
        return gen;
    }

}
