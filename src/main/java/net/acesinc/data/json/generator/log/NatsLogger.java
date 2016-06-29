package net.acesinc.data.json.generator.log;

import nats.client.Nats;
import nats.client.NatsConnector;
import net.acesinc.data.json.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;

/**
 * Created by betselot on 6/27/16.
 */
public class NatsLogger implements EventLogger {
    private static final Logger log = LogManager.getLogger(NatsLogger.class);

    public static final String NATS_SERVER_PROP_NAME = "broker.server";
    public static final String NATS_PORT_PROP_NAME = "broker.port";

    private final String topic;
    private final boolean sync;
    private final boolean flatten;
    private JsonUtils jsonUtils;
    private Nats nats;
    private NatsConnector natsConnector = new NatsConnector();
    StringBuilder natsURL = new StringBuilder("nats://");


    public NatsLogger(Map<String, Object> props) {
        String brokerHost = (String) props.get(NATS_SERVER_PROP_NAME);
        Integer brokerPort = (Integer) props.get(NATS_PORT_PROP_NAME);

        natsURL.append(brokerHost);
        natsURL.append(":");
        natsURL.append(brokerPort);

        nats= natsConnector.addHost(natsURL.toString()).connect();

        this.topic = props.get("topic").toString();
        this.sync = (Boolean) props.get("sync");
        this.flatten = (Boolean) props.get("flatten");
        this.jsonUtils = new JsonUtils();

    }

    @Override
    public void logEvent(String event) {
        String output = event;
        if (flatten) {
            try {
                output = jsonUtils.flattenJson(event);
            } catch (IOException ex) {
                log.error("Error flattening json. Unable to send event [ " + event + " ]", ex);
                return;
            }
        }

        log.debug("Sending event to gnatsd: [ " + output + " ]");
        nats.publish(topic,output);
    }

    @Override
    public void shutdown() {
        nats.close();
    }
}
