/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import net.acesinc.data.json.generator.types.TypeHandler;
import net.acesinc.data.json.generator.types.TypeHandlerFactory;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author andrewserff
 */
public class RandomJsonGenerator {

    private static final Logger log = LogManager.getLogger(RandomJsonGenerator.class);
    private SimpleDateFormat iso8601DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

    private Map<String, Object> config;
    private JsonGeneratorFactory factory = Json.createGeneratorFactory(null);

    public RandomJsonGenerator(Map<String, Object> config) {
        this.config = config;
    }

    public String generateJson() {
        StringWriter w = new StringWriter();
        javax.json.stream.JsonGenerator gen = factory.createGenerator(w);

        processProperties(gen, config);

        gen.flush();
        return w.toString();
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

    private javax.json.stream.JsonGenerator processProperties(javax.json.stream.JsonGenerator gen, Map<String, Object> props) {
        Map<String, Object> outputValues = new LinkedHashMap<>();
        for (String propName : props.keySet()) {
            Object value = props.get(propName);
            if (value == null) {
                outputValues.put(propName, null);
                addValue(gen, propName, null);
            } else if (String.class.isAssignableFrom(value.getClass())) {
                String type = (String) value;

                if (type.startsWith("this.")) {
                    //this could be recursive
                    //can't do this at the same time as the type handlers because the value might not be made yet...
                    log.warn("Referencing other properties is not implemented yet. Prop [ " + propName + " ] being ignored in output.");
                } else {
                    try {
                        TypeHandler th = TypeHandlerFactory.getInstance().getTypeHandler(type);

                        if (th != null) {
                            Object val = th.getNextRandomValue();
                            outputValues.put(propName, val);
                            addValue(gen, propName, val);
                        } else {
//                            log.debug("Unknown Type: [ " + type + " ] for prop [ " + propName + " ]. Attempting to echo literal value.");
                            outputValues.put(propName, type);
                            addValue(gen, propName, type);
                        }
                    } catch (IllegalArgumentException iae) {
                        log.warn("Error creating type [ " + type + " ]. Prop [ " + propName + " ] being ignored in output.  Reason: " + iae.getMessage());
                    }
                }
            } else if (Map.class.isAssignableFrom(value.getClass())) {
                //nested object
                Map<String, Object> nestedProps = (Map<String, Object>) value;
                if (propName == null) {
                    gen.writeStartObject();
                } else {
                    gen.writeStartObject(propName);
                }
                processProperties(gen, nestedProps);
                gen.writeEnd();
            } else if (List.class.isAssignableFrom(value.getClass())) {
                //array
                List<Object> listOfItems = (List<Object>) value;
                if (propName != null) {
                    gen.writeStartArray(propName);
                } else {
                    gen.writeStartArray();
                }

                if (!listOfItems.isEmpty()) {
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
                                    processList(subList, gen);
                                }
                                break;
                            }
                        }
                    } else {
                        processList(listOfItems, gen);
                    }
                }
                gen.writeEnd();
            } else {
                //literals
                addValue(gen, propName, value);
            }
        }

        return gen;
    }

    protected void processList(List<Object> listOfItems, JsonGenerator gen) {
        for (Object item : listOfItems) {
            if (String.class.isAssignableFrom(item.getClass())) {
                //literal string, just add it
                addValue(gen, null, (String) item);
            } else if (Map.class.isAssignableFrom(item.getClass())) {
                Map<String, Object> nestedProps = (Map<String, Object>) item;
                gen.writeStartObject();
                processProperties(gen, nestedProps);
                gen.writeEnd();
            }
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
            gen.write(propName, (Boolean) val);
        } else if (Long.class.isAssignableFrom(val.getClass())) {
            gen.write(propName, (Long) val);
        } else if (Integer.class.isAssignableFrom(val.getClass())) {
            gen.write(propName, (Integer) val);
        } else if (Double.class.isAssignableFrom(val.getClass())) {
            gen.write(propName, (Double) val);
        } else if (Date.class.isAssignableFrom(val.getClass())) {
            gen.write(propName, iso8601DF.format((Date) val));
        }
        return gen;
    }

}
