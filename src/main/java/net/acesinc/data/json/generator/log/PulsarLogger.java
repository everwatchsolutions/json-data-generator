/*
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.acesinc.data.json.generator.log;

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.ClientConfiguration;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.impl.PulsarClientImpl;

import net.acesinc.data.json.util.JsonUtils;

public class PulsarLogger implements EventLogger {

    public static final String PULSAR_SERVICE_URL_PROP_NAME = "broker.server";
    public static final String PULSAR_SERVICE_URL_PORT_PROP_NAME = "broker.port";
    
    private static final Logger log = LogManager.getLogger(PulsarLogger.class);
    
    private final String topic;
    private final boolean sync;
    private final boolean flatten;
    private final PulsarClient pulsarClient;
    private Producer producer;
    private JsonUtils jsonUtils;
    StringBuilder pulsarURL = new StringBuilder("pulsar://");
    
    public PulsarLogger(Map<String, Object> props) throws PulsarClientException {
        
        String brokerHost = (String) props.get(PULSAR_SERVICE_URL_PROP_NAME);
        Integer brokerPort = (Integer) props.get(PULSAR_SERVICE_URL_PORT_PROP_NAME);

        pulsarURL.append(brokerHost);
        pulsarURL.append(":");
        pulsarURL.append(brokerPort);
        
        this.topic = (String) props.get("topic");
        
        if (props.get("sync") != null) {
            this.sync = (Boolean) props.get("sync");
        } else {
            this.sync = false;
        }
        
        if (props.get("flatten") != null) {
            this.flatten = (Boolean) props.get("flatten");
        } else {
            this.flatten = false;
        }
        
        this.pulsarClient = new PulsarClientImpl(pulsarURL.toString(), new ClientConfiguration());
        this.producer = pulsarClient.createProducer(topic);
        
        this.jsonUtils = new JsonUtils();
        
    }
    
    @Override
    public void logEvent(String event, Map<String, Object> producerConfig) {
        
        String output = event;
        
        if (flatten) {
            try {
                output = jsonUtils.flattenJson(event);
            } catch (IOException ex) {
                log.error("Error flattening json. Unable to send event [ " + event + " ]", ex);
                return;
            }
        }
        
        if (sync) {
            try {
                producer.send(output.getBytes());
            } catch (PulsarClientException e) {
                log.error("Unable to send event to Pulsar", e);
            }
        } else {
            producer.sendAsync(output.getBytes());
        }

    }

    @Override
    public void shutdown() {
        
        try {
            producer.close();
            pulsarClient.close();
        } catch (final Exception ex) {
            
        }

    }

}
