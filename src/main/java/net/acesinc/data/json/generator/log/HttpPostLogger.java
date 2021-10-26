/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.log;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
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
                response = httpClient.execute(request);
                if (response.getStatusLine().getStatusCode() != 200) {
                    log.warn("Non-201 response code: " + response.getStatusLine().getStatusCode());
                    log.warn("Response: " + EntityUtils.toString(response.getEntity()));
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
