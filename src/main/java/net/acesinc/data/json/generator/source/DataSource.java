package net.acesinc.data.json.generator.source;

import java.util.Map;

/**
 *
 */
public interface DataSource {
    public Map<String, Object> getNextDataItem();
    public Map<String, Object> getRandomDataItem();
    public long getNumberOfDataItems();
    public void shutdown();
}
