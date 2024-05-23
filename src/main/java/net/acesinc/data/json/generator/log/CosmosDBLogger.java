package net.acesinc.data.json.generator.log;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CosmosDBLogger implements EventLogger {

    private static final Logger log = LogManager.getLogger(CosmosDBLogger.class);
    private CosmosClient cosmosClient;
    private CosmosDatabase cosmosDatabase;
    private CosmosContainer cosmosContainer;
    private final ObjectMapper mapper = new ObjectMapper();

    public CosmosDBLogger(Map<String, Object> props) {
        String uri = (String) props.get("uri");
        String key = (String) props.get("key");
        String databaseName = (String) props.get("databaseName");
        String containerName = (String) props.get("containerName");

        cosmosClient = new CosmosClientBuilder()
                .endpoint(uri)
                .key(key)
                .buildClient();

        cosmosDatabase = cosmosClient.getDatabase(databaseName);
        cosmosContainer = cosmosDatabase.getContainer(containerName);
    }

    @Override
    public void logEvent(String event, Map<String, Object> producerConfig) {
        try {
            JsonNode jsonNode = mapper.readTree(event);
            CosmosItemResponse<JsonNode> item = cosmosContainer.createItem(jsonNode);
            log.info("Document added to Cosmos DB with request charge of " + item.getRequestCharge() + " within session " + item.getSessionToken());
        } catch (Exception e) {
            log.error("Error inserting JSON data into Cosmos DB", e);
        }
    }

    @Override
    public void shutdown() {
        if (cosmosClient != null) {
            cosmosClient.close();
            log.info("Cosmos DB client closed successfully");
        }
    }
}
