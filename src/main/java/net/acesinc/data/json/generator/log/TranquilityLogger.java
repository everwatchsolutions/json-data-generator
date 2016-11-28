/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.metamx.common.Granularity;
import com.metamx.tranquility.beam.ClusteredBeamTuning;
import com.metamx.tranquility.druid.DruidBeams;
import com.metamx.tranquility.druid.DruidDimensions;
import com.metamx.tranquility.druid.DruidLocation;
import com.metamx.tranquility.druid.DruidRollup;
import com.metamx.tranquility.druid.DruidSpatialDimension;
import com.metamx.tranquility.typeclass.Timestamper;
import com.twitter.finagle.Service;
import com.twitter.util.Await;
import com.twitter.util.Future;
import io.druid.data.input.impl.TimestampSpec;
import io.druid.granularity.QueryGranularity;
import io.druid.query.aggregation.AggregatorFactory;
import io.druid.query.aggregation.CountAggregatorFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.acesinc.data.json.util.JsonUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 * Logger that sends data to Druid (http://druid.io) using Tranquility
 * (https://github.com/metamx/tranquility)
 *
 * @author andrewserff
 */
public class TranquilityLogger implements EventLogger {

    private static final Logger log = LogManager.getLogger(TranquilityLogger.class);
    public static final String OVERLORD_NAME_PROP_NAME = "overlord.name";
    public static final String FIREHOSE_PATTERN_PROP_NAME = "firehose.pattern";
    public static final String DISCOVERY_PATH_PROP_NAME = "discovery.path";
    public static final String DATASOURCE_NAME_PROP_NAME = "datasource.name";
    public static final String DIMENSIONS_PROP_NAME = "dimensions";
    public static final String GEOSPATIAL_DIMENSIONS_PROP_NAME = "geo.dimensions";
    public static final String TIMESTAMP_NAME_PROP_NAME = "timestamp.name";
    public static final String TIMESTAMP_FORMAT_PROP_NAME = "timestamp.format";
    public static final String SEGMENT_GRANULARITY_PROP_NAME = "segment.granularity";
    public static final String QUERY_GRANULARITY_PROP_NAME = "query.granularity";
    public static final String ZOOKEEPER_HOST_PROP_NAME = "zookeeper.host";
    public static final String ZOOKEEPER_PORT_PROP_NAME = "zookeeper.port";
    public static final String FLATTEN_PROP_NAME = "flatten";
    public static final String SYNC_PROP_NAME = "sync";

    private final String indexService;
    private final String firehosePattern;
    private final String discoveryPath;
    private final String dataSourceName;
    private final String dimensionNames;
    private final String geoSpatialDims;
    private final String timestampName;
    private final String timestampFormat; //default is auto
    private final String segmentGranularity; //default is hour
    private final String queryGranularity; //default is minute

    private final String zookeeperHost;
    private final Integer zookeeperPort;
    private final boolean flatten; //default is true
    private final boolean sync; //default is false

    private final List<String> dimensions;
    private DruidDimensions druidDimensions;
    private final List<AggregatorFactory> aggregators;
    private final Timestamper<Map<String, Object>> timestamper;
    private final CuratorFramework curator;
    private final TimestampSpec timestampSpec;
    private final Service<List<Map<String, Object>>, Integer> druidService;

    private JsonUtils jsonUtils;
    private ObjectMapper mapper;

    public TranquilityLogger(Map<String, Object> props) {
        this.jsonUtils = new JsonUtils();
        this.mapper = new ObjectMapper();

        this.indexService = (String) props.get(OVERLORD_NAME_PROP_NAME);
        this.firehosePattern = (String) props.get(FIREHOSE_PATTERN_PROP_NAME);
        this.discoveryPath = (String) props.get(DISCOVERY_PATH_PROP_NAME);
        this.dataSourceName = (String) props.get(DATASOURCE_NAME_PROP_NAME);
        this.dimensionNames = (String) props.get(DIMENSIONS_PROP_NAME);
        this.geoSpatialDims = (String) props.get(GEOSPATIAL_DIMENSIONS_PROP_NAME);
        this.timestampName = (String) props.get(TIMESTAMP_NAME_PROP_NAME);
        this.timestampFormat = (String) props.getOrDefault(TIMESTAMP_FORMAT_PROP_NAME, "auto");
        this.segmentGranularity = ((String) props.getOrDefault(SEGMENT_GRANULARITY_PROP_NAME, "hour")).toUpperCase();
        this.queryGranularity = ((String) props.getOrDefault(QUERY_GRANULARITY_PROP_NAME, "minute")).toUpperCase();
        this.zookeeperHost = (String) props.get(ZOOKEEPER_HOST_PROP_NAME);
        this.zookeeperPort = (Integer) props.get(ZOOKEEPER_PORT_PROP_NAME);
        this.flatten = (Boolean) props.getOrDefault(FLATTEN_PROP_NAME, true);
        this.sync = (Boolean) props.getOrDefault(SYNC_PROP_NAME, false);

        dimensions = new ArrayList<>();
        if (dimensionNames != null && !dimensionNames.isEmpty()) {
            String[] dims = dimensionNames.split(",");
            for (String s : dims) {
                dimensions.add(s.trim());
            }
        }
        if (dimensions.isEmpty()) {
            log.debug("Configuring Tranquility with Schemaless ingestion");
            druidDimensions = DruidDimensions.schemaless();
        } else {
            log.debug("Configuring Tranqulity with the following dimensions: " + dimensions.toString());
            druidDimensions = DruidDimensions.specific(dimensions);
        }

        List<String> geoDims = new ArrayList<>();
        if (geoSpatialDims != null && !geoSpatialDims.isEmpty()) {
            String[] dims = geoSpatialDims.split(",");
            for (String s : dims) {
                geoDims.add(s.trim());
            }
        }
        if (!geoDims.isEmpty()) {
            log.debug("Adding Geospatial Dimensions: " + geoDims.toString());
            druidDimensions = druidDimensions.withSpatialDimensions(
                    Lists.newArrayList(
                            DruidSpatialDimension.multipleField(
                                    "geo",
                                    geoDims
                            )
                    ));
        }

        aggregators = ImmutableList.<AggregatorFactory>of(
                new CountAggregatorFactory(
                        "events"
                )
        );

        // Tranquility needs to be able to extract timestamps from your object type (in this case, Map<String, Object>).
        timestamper = new Timestamper<Map<String, Object>>() {
            @Override
            public DateTime timestamp(Map<String, Object> theMap) {
                return new DateTime(theMap.get(timestampName));
            }
        };

        // Tranquility uses ZooKeeper (through Curator) for coordination.
        curator = CuratorFrameworkFactory
                .builder()
                .connectString(zookeeperHost + ":" + zookeeperPort.toString())
                .retryPolicy(new ExponentialBackoffRetry(1000, 20, 30000))
                .build();
        curator.start();

        // The JSON serialization of your object must have a timestamp field in a format that Druid understands. By default,
        // Druid expects the field to be called "timestamp" and to be an ISO8601 timestamp.
        log.debug("Confiuring Tranqulity Timestamp Spec with { name: " + timestampName + ", format: " + timestampFormat + " }");
        timestampSpec = new TimestampSpec(timestampName, timestampFormat);

        // Tranquility needs to be able to serialize your object type to JSON for transmission to Druid. By default this is
        // done with Jackson. If you want to provide an alternate serializer, you can provide your own via ```.objectWriter(...)```.
        // In this case, we won't provide one, so we're just using Jackson.
        log.debug("Creating Druid Beam for DataSource [ " + dataSourceName + " ]");
        druidService = DruidBeams
                .builder(timestamper)
                .curator(curator)
                .discoveryPath(discoveryPath)
                .location(
                        DruidLocation.create(
                                indexService,
                                firehosePattern,
                                dataSourceName
                        )
                )
                .timestampSpec(timestampSpec)
                .rollup(DruidRollup.create(druidDimensions, aggregators, QueryGranularity.fromString(queryGranularity)))
                .tuning(
                        ClusteredBeamTuning
                        .builder()
                        .segmentGranularity(Granularity.valueOf(segmentGranularity))
                        .windowPeriod(new Period("PT10M"))
                        .partitions(1)
                        .replicants(1)
                        .build()
                )
                .buildJavaService();

    }

    @Override
    public void logEvent(String event, Map<String, Object> producerConfig) {
        logEvent(event);
    }
    
    private void logEvent(String event) {
        try {
            String output = event;
            if (flatten) {
                try {
                    output = jsonUtils.flattenJson(event);
                } catch (IOException ex) {
                    log.error("Error flattening json. Unable to send event [ " + event + " ]", ex);
                    return;
                }
            }

            log.debug("Sending event to Tranquility: [ " + output + " ]");
            Map<String, Object> map = mapper.readValue(output, Map.class);
            List<Map<String, Object>> listOfEvents = ImmutableList.of(map);

            // Send events to Druid:
            final Future<Integer> numSentFuture = druidService.apply(listOfEvents);

            if (sync) {
                // Wait for confirmation:
                log.debug("Waiting for ACK");
                final Integer numSent = Await.result(numSentFuture);
                log.debug("ACK recieved! Continue on");
            }
        } catch (Exception ex) {
            log.error("Error sending event to Druid", ex);
        }
    }

    @Override
    public void shutdown() {
        try {
            log.info("Shutting down Tranquility Logger");
            Await.result(druidService.close());
            curator.close();
            log.info("Successfully Shutdown Tranquility Logger");
        } catch (Exception ex) {
            log.error("Error shutting down Tranquility Logger", ex);
        }
    }

}
