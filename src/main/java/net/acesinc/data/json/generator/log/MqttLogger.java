/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.log;

import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created by ygalblum on 11/24/16.
 */
public class MqttLogger implements EventLogger {
    private static final Logger log = LogManager.getLogger(MqttLogger.class);
    
    /* Constants fpr Properties names */
    private static final String BROKER_SERVER_PROP_NAME = "broker.server";
    private static final String BROKER_PORT_PROP_NAME = "broker.port";
    private static final String TOPIC_PROP_NAME = "topic";
    private static final String CLIENT_ID_PROP_NAME = "clientId";
    private static final String QOS_PROP_NAME = "qos";

    /* Constants for default values */
    private static final String DEFAULT_CLIENT_ID     = "JsonGenerator";
    private static final int DEFAULT_QOS = 2;
    
    /* Instance properties */
    private final MqttClient mqttClient;
    private final String topic;
    private final int qos;
    
    public MqttLogger(Map<String, Object> props) throws MqttException {
        String brokerHost = (String) props.get(BROKER_SERVER_PROP_NAME);
        Integer brokerPort = (Integer) props.get(BROKER_PORT_PROP_NAME);
        String brokerAddress = brokerHost + ":" + brokerPort.toString();
        
        topic = (String) props.get(TOPIC_PROP_NAME);
        String clientId = (String) props.get(CLIENT_ID_PROP_NAME);
        Integer _qos = (Integer) props.get(QOS_PROP_NAME);
        qos = null == _qos ? DEFAULT_QOS : _qos;
        
        mqttClient = new MqttClient(brokerAddress, null == clientId ? DEFAULT_CLIENT_ID : clientId);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        log.debug("Connecting to broker: "+brokerAddress);
        mqttClient.connect(connOpts);
        log.debug("Connected");
    }

    @Override
    public void logEvent(String event, Map<String, Object> producerConfig) {
        logEvent(event);
    }
    
    private void logEvent(String event) {
        MqttMessage message = new MqttMessage(event.getBytes());
        message.setQos(qos);
        try {
            mqttClient.publish(topic, message);
            log.debug("Message published");
        } catch (MqttException ex) {
            log.error("Failed to publish message", ex);
        }
    }

    @Override
    public void shutdown() {
        if (null != mqttClient) {
            try {
                mqttClient.disconnect();
                System.out.println("Disconnected");
            } catch (MqttException ex) {
                log.error("Error in disconnect", ex);
            }
        }
    }
}
