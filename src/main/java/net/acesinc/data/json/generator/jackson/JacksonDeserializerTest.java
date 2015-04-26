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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JacksonDeserializerTest {
    private Logger log = Logger.getLogger(JacksonDeserializerTest.class.getName());
    public JacksonDeserializerTest() {
        String validJson = "{ \"test\":{\"test1\":\"one\",\"test2\":\"two\"}}";
        String invalidJson = "{ \"test\":{\"test1\":nonStandardThing(),\"test2\":\"two\"}}";

        ObjectMapper mapper = new ObjectMapper();
        mapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public boolean handleUnknownProperty(DeserializationContext dc, JsonParser jp, JsonDeserializer<?> jd, Object bean, String property) throws IOException, JsonProcessingException {
                System.out.println("Handling unknown property: " + property);
                return false;
            }
        });
        
        try {
            log.log(Level.INFO, "Valid json looks like: {0}", mapper.readValue( validJson, Map.class).toString());
            log.log(Level.INFO, "Invalid json looks like: {0}", mapper.readValue(invalidJson, Map.class).toString());
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Error parsing json", ex);
        }

    }

    public static void main(String[] args) {
        JacksonDeserializerTest test = new JacksonDeserializerTest();
    }
}
