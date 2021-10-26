/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.log;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.net.ssl.SSLContext;
import net.acesinc.data.json.generator.SimulationRunner;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author andrewserff
 */
public class HttpPostLogger implements EventLogger {

    private static final Logger log = LogManager.getLogger(HttpPostLogger.class);
    public static final String URL_PROP_NAME = "url";
    public static final String HEADERS_PROP_NAME = "headers";

    private Map<String, String> headers = new HashMap<>();
    private String url;
    private CloseableHttpClient httpClient;

    private final Timer requestTimer;
    private final Counter successCounter;
    private final Counter badRequestCounter;
    private final Counter serverErrorCounter;

    public HttpPostLogger(Map<String, Object> props) throws NoSuchAlgorithmException {
        this.url = (String) props.get(URL_PROP_NAME);

        final Object headerConfigs = props.get(HEADERS_PROP_NAME);
        if (headerConfigs instanceof Map) {
            for (Entry<Object, Object> header : ((Map<Object, Object>) headerConfigs).entrySet()) {
                if (header.getKey() instanceof String && header.getValue() instanceof String) {
                    headers.put((String) header.getKey(), (String) header.getValue());
                }
            }
        }

        SSLConnectionSocketFactory sf = new SSLConnectionSocketFactory(SSLContext.getDefault(), new NoopHostnameVerifier());
        this.httpClient = HttpClientBuilder.create().setSSLSocketFactory(sf).build();

        this.requestTimer = SimulationRunner.metrics.timer(
            MetricRegistry.name(HttpPostLogger.class, "request", "duration", "ms"));
        this.successCounter = SimulationRunner.metrics.counter(
                MetricRegistry.name(HttpPostLogger.class, "response", "status", "200"));
        this.badRequestCounter = SimulationRunner.metrics.counter(
            MetricRegistry.name(HttpPostLogger.class, "response", "status", "4XX"));
        this.serverErrorCounter = SimulationRunner.metrics.counter(
            MetricRegistry.name(HttpPostLogger.class, "response", "status", "5XX"));
    }

    @Override
    public void logEvent(String event, Map<String, Object> producerConfig) {
        logEvent(event);
    }
    
    private void logEvent(String event) {
        try {
            HttpPost request = new HttpPost(url);
            StringEntity input = new StringEntity(event);
            input.setContentType("application/json");
            request.setEntity(input);

            for (Entry<String, String> header : headers.entrySet()) {
                request.addHeader(header.getKey(), header.getValue());
            }

            CloseableHttpResponse response = null;
            try {
                final Context time = this.requestTimer.time();
                response = httpClient.execute(request);
                time.stop();

                final String statusCode = String.valueOf(response.getStatusLine().getStatusCode());
                if (statusCode.startsWith("2")) {
                    this.successCounter.inc();
                } else if (statusCode.startsWith("4")) {
                    this.badRequestCounter.inc();
                } else if (statusCode.startsWith("5")) {
                    this.serverErrorCounter.inc();
                } else {
                    log.warn("Unknown status code: " + statusCode);
                }

            } catch (IOException ex) {
                log.error("Error POSTing Event", ex);
            }
            if (response != null) {
                try {
                    HttpEntity resEntity = response.getEntity();
                    EntityUtils.consume(resEntity);
                } catch (IOException ioe) {
                    //oh well
                } finally {
                    try {
                        response.close();
                    } catch (IOException ex) {
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void shutdown() {
        try {
            httpClient.close();
        } catch (IOException ex) {
            //oh well
        }
    }
}
