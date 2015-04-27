/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.log;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author andrewserff
 */
public class KafkaLogger implements EventLogger {

    private static final Logger log = LogManager.getLogger(KafkaLogger.class);
    public static final String BROKER_SERVER_PROP_NAME = "broker.server";
    public static final String BROKER_PORT_PROP_NAME = "broker.port";
    
    private final KafkaProducer<String, String> producer;
    private final String topic;
    private final boolean sync;
    private final Properties props = new Properties();

    public KafkaLogger(Map<String, Object> props) {
        String brokerHost = (String) props.get(BROKER_SERVER_PROP_NAME);
        Integer brokerPort = (Integer) props.get(BROKER_PORT_PROP_NAME);
        
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,brokerHost + ":" + brokerPort.toString());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());
        
        producer = new KafkaProducer<>(props);
        this.topic = (String) props.get("topic");
        this.sync = (Boolean) props.get("sync");
    }

    @Override
    public void logEvent(String event) {
        boolean sync = false;
        
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, event);
        if (sync) {
            try {
                producer.send(producerRecord).get();
            } catch (InterruptedException | ExecutionException ex) {
                //got interrupted while waiting
                log.warn("Thread interrupted while waiting for synchronous response from producer", ex);
            }
        } else {
            log.debug("Sending event to Kafka: [ " + event + " ]");
            producer.send(producerRecord);
        }
    }

    @Override
    public void shutdown() {
        producer.close();
    }

}
