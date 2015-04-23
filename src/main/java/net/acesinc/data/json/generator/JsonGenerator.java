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
import javax.json.stream.JsonGeneratorFactory;
import net.acesinc.data.json.generator.config.ConfigReader;
import net.acesinc.data.json.generator.types.TypeHandler;
import net.acesinc.data.json.generator.types.TypeHandlerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author andrewserff
 */
public class JsonGenerator {
    
    private static final Logger log = LogManager.getLogger(JsonGenerator.class);
    private SimpleDateFormat iso8601DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    
    public JsonGenerator(String config) throws IOException {
        //First generate the config
        Map<String, Object> props = ConfigReader.readConfig(this.getClass().getClassLoader().getResourceAsStream(config));
        log.debug("Map: " + props.toString());
//        Map<String, Object> props = new HashMap<>();
//        props.put("firstName", "firstName()");
//        props.put("lastName", "firstName()");
//        props.put("randomName", "random('one',\"two\",'three')");
//        props.put("active", "boolean()");
//        props.put("rand-long", "long()");
//        props.put("rand-long-min", "long(895043890865)");
//        props.put("rand-long-range", "long(787658, 8948555)");
//        props.put("rand-int", "integer()");
//        props.put("rand-int-min", "integer(80000)");
//        props.put("rand-int-range", "integer(10, 20)");
//        props.put("rand-double", "double()");
//        props.put("rand-double-min", "double(80000.44)");
//        props.put("rand-double-range", "double(10.5, 20.3)");
//        props.put("alpha", "alpha(5)");
//        props.put("alphaNumeric", "alphaNumeric(10)");
//
//        Map<String, Object> nestedProps = new HashMap<>();
//        nestedProps.put("nested-alpha", "alpha(10)");
//        nestedProps.put("nested-long", "long(10, 20)");
//        props.put("nested-props", nestedProps);
//
//        List<Map<String, Object>> listOfNestedProps = new ArrayList<>();
//        Map<String, Object> nestedProps1 = new HashMap<>();
//        nestedProps1.put("nested1-alpha", "alpha(10)");
//        nestedProps1.put("nested1-long", "long(10, 20)");
//        listOfNestedProps.add(nestedProps1);
//        
//        Map<String, Object> nestedProps2 = new HashMap<>();
//        nestedProps2.put("nested2-alpha", "alpha(10)");
//        nestedProps2.put("nested2-long", "long(10, 20)");
//        listOfNestedProps.add(nestedProps2);
//        
//        props.put("list-of-stuff", listOfNestedProps);

        //now generate the json
        JsonGeneratorFactory factory = Json.createGeneratorFactory(null);
        StringWriter w = new StringWriter();
        javax.json.stream.JsonGenerator gen = factory.createGenerator(w);
        processProperties(gen, props, null);
        gen.flush();
        
        log.info("Gernerated json: " + w.toString());
    }
    
    private javax.json.stream.JsonGenerator processProperties(javax.json.stream.JsonGenerator gen, Map<String, Object> props, String objectName) {
        if (objectName == null) {
            gen.writeStartObject();
        } else {
            gen.writeStartObject(objectName);
        }
        Map<String, Object> outputValues = new LinkedHashMap<>();
        for (String propName : props.keySet()) {
            Object value = props.get(propName);
            if (String.class.isAssignableFrom(value.getClass())) {
                String type = (String) value;
                
                if (type.startsWith("this.")) {
                    //this could be recursive
                    //can't do this at the same time as the type handlers because the value might not be made yet...
                } else {
                    try {
                        TypeHandler th = TypeHandlerFactory.getTypeHandler(type);
                        
                        if (th != null) {
                            Object val = th.getNextRandomValue();
                            outputValues.put(propName, val);
                            addValue(gen, propName, val);
                        } else {
                            log.warn("Unknown Type: [ " + type + " ]. Prop [ " + propName + " ] being ignored in output");
                            Object val = type;
                            if (type.contains("\"")) {
                                val = type.replaceAll("\"", "").trim();
                            } else {
                                //trial and error!
                                try {
                                    val = Long.parseLong(type);
                                } catch (NumberFormatException nfe) {
                                    //not a long..
                                    try {
                                        val = Double.parseDouble(type);
                                    } catch (NumberFormatException nfe2) {
                                        //not a double..
                                        try {
                                            val = Integer.parseInt(type);
                                        } catch (NumberFormatException nfe3) {
                                            //not an int..
                                            //at this point, must be a boolean
                                            val = Boolean.parseBoolean(type);
                                        }
                                    }
                                }
                                
                                
                            }
                            outputValues.put(propName, val);
                            addValue(gen, propName, val);
                        }
                    } catch (IllegalArgumentException iae) {
                        log.warn("Error creating type [ " + type + " ]. Prop [ " + propName + " ] being ignored in output.  Reason: " + iae.getMessage());
                    }
                }
            } else if (Map.class.isAssignableFrom(value.getClass())) {
                //nested object
                Map<String, Object> nestedProps = (Map<String, Object>) value;
                processProperties(gen, nestedProps, propName);
            } else if (List.class.isAssignableFrom(value.getClass())) {
                //array
                List<Map<String, Object>> listOfNestedProps = (List<Map<String, Object>>) value;
                gen.writeStartArray(propName);
                for (Map<String, Object> nestedProps : listOfNestedProps) {
                    processProperties(gen, nestedProps, null);
                }
                gen.writeEnd();
            }
        }
        gen.writeEnd();
        return gen;
    }
    
    private javax.json.stream.JsonGenerator addValue(javax.json.stream.JsonGenerator gen, String propName, Object val) {
        if (String.class.isAssignableFrom(val.getClass())) {
            gen.write(propName, (String) val);
        } else if (Boolean.class.isAssignableFrom(val.getClass())) {
            gen.write(propName, (Boolean) val);
        } else if (Long.class.isAssignableFrom(val.getClass())) {
            gen.write(propName, (Long) val);
        } else if (Integer.class.isAssignableFrom(val.getClass())) {
            gen.write(propName, (Integer) val);
        } else if (Double.class.isAssignableFrom(val.getClass())) {
            gen.write(propName, (Double) val);
        } else if (Date.class.isAssignableFrom(val.getClass())) {
            gen.write(propName, iso8601DF.format((Date)val));
        }
        return gen;
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
