package net.acesinc.data.json.generator.log;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class KinesisLogger implements EventLogger {

    private static final Logger log = LogManager.getLogger(KinesisLogger.class);

    private static final String TIMESTAMP = Long.toString(System.currentTimeMillis());

    AmazonKinesis kinesisClient;

    String streamName = "";

    private long startTimeFull;

    Integer maxRecords = 0;

    // The monotonically increasing sequence number we will put in the data of each record
    final AtomicLong sequenceNumber = new AtomicLong(0);

    public KinesisLogger(Map<String, Object> props) {
        this.streamName = (String) props.get("stream");
        String region = (String) props.get("region");
        String roleARN = (String) props.get("roleARN");
        this.maxRecords = (Integer) props.get("max.records");
        if(this.maxRecords == null)
            this.maxRecords = 0;

        AmazonKinesisClientBuilder clientBuilder = AmazonKinesisClientBuilder.standard();

        AWSCredentialsProvider credentialsProvider = null;
        if(roleARN != null) {
            AWSSecurityTokenService client = AWSSecurityTokenServiceClientBuilder.standard().build();
            AssumeRoleRequest request = new AssumeRoleRequest().withRoleArn(roleARN)
                    .withRoleSessionName("json-data-generator");
//          .withDurationSeconds(3600)
            AssumeRoleResult response = client.assumeRole(request);

            BasicSessionCredentials sessionCredentials = new BasicSessionCredentials(
                    response.getCredentials().getAccessKeyId(),
                    response.getCredentials().getSecretAccessKey(),
                    response.getCredentials().getSessionToken());
            credentialsProvider = new AWSStaticCredentialsProvider(sessionCredentials);
        } else {
            credentialsProvider = new DefaultAWSCredentialsProviderChain();
        }

        clientBuilder.setRegion(region);
        clientBuilder.setCredentials(credentialsProvider);

        kinesisClient = clientBuilder.build();

        startTimeFull = System.currentTimeMillis();
        log.info("Starting puts...");
    }

    public static ByteBuffer generateData(String eventData) {
        try {
            return ByteBuffer.wrap(eventData.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void logEvent(String event, Map<String, Object> producerConfig) {

        String streamName = (String) producerConfig.get("stream");
        if(streamName == null){
            streamName = this.streamName;
        }

        sequenceNumber.getAndIncrement();
        try {

            PutRecordRequest putRecordRequest = new PutRecordRequest();
            putRecordRequest.setStreamName( streamName);
            putRecordRequest.setData(generateData(event));
            putRecordRequest.setPartitionKey( TIMESTAMP);
            PutRecordResult putRecordResult = kinesisClient.putRecord( putRecordRequest );
        } catch (Exception ex) {
            //got interrupted while waiting
            log.error("Error while publishing events : ", ex);
        }
        long totalTimeElasped = System.currentTimeMillis() - startTimeFull;
        log.info("Events Published : " +  sequenceNumber + " events in " + (totalTimeElasped / 1000) + " secs");
        if(this.maxRecords != 0 && sequenceNumber.intValue() == maxRecords){
            shutdown();
            System.exit(0);
        }
    }

    @Override
    public void shutdown() {
        kinesisClient.shutdown();
        log.info("Kinesis Logger shutdown.");
        long totalTimeElasped = System.currentTimeMillis() - startTimeFull;
        log.info("Events Published : " +  sequenceNumber + " events in " + (totalTimeElasped / 1000) + " secs");
    }
}
