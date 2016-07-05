/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.log;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
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

    private String url;
    private CloseableHttpClient httpClient;

    public HttpPostLogger(Map<String, Object> props) throws NoSuchAlgorithmException {
        this.url = (String) props.get(URL_PROP_NAME);
        SSLConnectionSocketFactory sf = new SSLConnectionSocketFactory(SSLContext.getDefault(), new NoopHostnameVerifier());
        this.httpClient = HttpClientBuilder.create().setSSLSocketFactory(sf).build();
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
                response = httpClient.execute(request);
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
            httpClient.close();
        } catch (IOException ex) {
            //oh well
        }
    }
}
