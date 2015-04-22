/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.stream.JsonGeneratorFactory;
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

    public JsonGenerator() {
        JsonGeneratorFactory factory = Json.createGeneratorFactory(null);
        StringWriter w = new StringWriter();
        javax.json.stream.JsonGenerator gen = factory.createGenerator(w);

        Map<String, Object> props = new HashMap<>();
        props.put("firstName", "firstName()");
        props.put("lastName", "firstName()");
        props.put("randomName", "random('one',\"two\",'three')");
        props.put("active", "boolean()");
        props.put("rand-long", "long()");
        props.put("rand-long-min", "long(895043890865)");
        props.put("rand-long-range", "long(787658, 8948555)");
        props.put("rand-int", "integer()");
        props.put("rand-int-min", "integer(80000)");
        props.put("rand-int-range", "integer(10, 20)");
        props.put("rand-double", "double()");
        props.put("rand-double-min", "double(80000.44)");
        props.put("rand-double-range", "double(10.5, 20.3)");
        props.put("alpha", "alpha(5)");
        props.put("alphaNumeric", "alphaNumeric(10)");

        Map<String, Object> nestedProps = new HashMap<>();
        nestedProps.put("nested-alpha", "alpha(10)");
        nestedProps.put("nested-long", "long(10, 20)");
        props.put("nested-props", nestedProps);

        List<Map<String, Object>> listOfNestedProps = new ArrayList<Map<String, Object>>();
        Map<String, Object> nestedProps1 = new HashMap<>();
        nestedProps1.put("nested1-alpha", "alpha(10)");
        nestedProps1.put("nested1-long", "long(10, 20)");
        props.put("nested1-props", nestedProps1);
        listOfNestedProps.add(nestedProps1);
        
        Map<String, Object> nestedProps2 = new HashMap<>();
        nestedProps2.put("nested1-alpha", "alpha(10)");
        nestedProps2.put("nested1-long", "long(10, 20)");
        props.put("nested1-props", nestedProps2);
        listOfNestedProps.add(nestedProps2);
        
        props.put("list-of-stuff", listOfNestedProps);
        
        processConfig(gen, props, null);
        gen.flush();

        log.info("Gernerated json: " + w.toString());
    }

    private javax.json.stream.JsonGenerator processConfig(javax.json.stream.JsonGenerator gen, Map<String, Object> props, String objectName) {
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
                    TypeHandler th = TypeHandlerFactory.getTypeHandler(type);
                    if (th != null) {
                        Object val = th.getNextRandomValue();
                        outputValues.put(propName, val);
                        addValue(gen, propName, val);
                    } else {
                        log.warn("Unknown Type: [ " + type + " ]. Prop [ " + propName + " ] being ignored in output");
                    }
                }
            } else if (Map.class.isAssignableFrom(value.getClass())) {
                //nested object
                Map<String, Object> nestedProps = (Map<String, Object>) value;
                processConfig(gen, nestedProps, propName);
            } else if (List.class.isAssignableFrom(value.getClass())) {
                //array
                List<Map<String, Object>> listOfNestedProps = (List<Map<String, Object>>) value;
                gen.writeStartArray(propName);
                for (Map<String, Object> nestedProps : listOfNestedProps) {
                    processConfig(gen, nestedProps, null);
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
        }
        return gen;
    }

    public static void main(String... args) {

        JsonGenerator gen = new JsonGenerator();
    }
}
