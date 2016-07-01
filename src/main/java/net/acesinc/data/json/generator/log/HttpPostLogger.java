/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
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

    private String url;
    private CloseableHttpClient httpclient;

    public HttpPostLogger(Map<String, Object> props) {
        this.url = (String) props.get(URL_PROP_NAME);
        this.httpclient = HttpClients.createDefault();
    }

    @Override
    public void logEvent(String event) {
        try {
            HttpPost request = new HttpPost(url);
            StringEntity input = new StringEntity(event);
            input.setContentType("application/json");
            request.setEntity(input);

//            log.debug("executing request " + request);
            CloseableHttpResponse response = null;
            try {
                response = httpclient.execute(request);
            } catch (IOException ex) {
                log.error("Error POSTing Event", ex);
            }
            if (response != null) {
                try {
//                    log.debug("----------------------------------------");
//                    log.debug(response.getStatusLine().toString());
                    HttpEntity resEntity = response.getEntity();
                    if (resEntity != null) {
//                        log.debug("Response content length: " + resEntity.getContentLength());
                    }
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
            httpclient.close();
        } catch (IOException ex) {
            //oh well
        }
    }
    
    public static void main(String[] args) {
        Map<String, Object> conf = new HashMap<>();
        conf.put(URL_PROP_NAME, "http://localhost:8050/ingest");
        HttpPostLogger l = new HttpPostLogger(conf);
        
        String json = "{\n" +
"    \"transmissionId\": \"123\",\n" +
"    \"upDate\": {\n" +
"        \"$date\": \"2016-03-17T14:34:27.227Z\"\n" +
"    },\n" +
"    \"downDate\": {\n" +
"        \"$date\": \"2016-03-17T14:35:27.227Z\"\n" +
"    },\n" +
"    \"lastUpdated\": {\n" +
"        \"$date\": \"2016-03-17T14:35:27.227Z\"\n" +
"    },\n" +
"    \"df\": {\n" +
"        \"depressionAngle\": 10.2,\n" +
"        \"platformLocation\": {\n" +
"            \"lat\": 45.9,\n" +
"            \"lon\": 43.2,\n" +
"            \"roll\": 34.5,\n" +
"            \"pitch\": 2.2,\n" +
"            \"yaw\": 34.5\n" +
"        },\n" +
"        \"targetLocation\": {\n" +
"            \"lat\": 44.9,\n" +
"            \"lon\": 44.2\n" +
"        }\n" +
"    },\n" +
"    \"freq\": 445.5,\n" +
"    \"bw\": 10.0,\n" +
"    \"modType\": \"mod1\",\n" +
"    \"quality\": 23.3,\n" +
"    \"priority\": 3,\n" +
"    \"originatorId\": \"324554\",\n" +
"    \"snr\": 33\n" +
"}";
        
        l.logEvent(json);
    }

}
