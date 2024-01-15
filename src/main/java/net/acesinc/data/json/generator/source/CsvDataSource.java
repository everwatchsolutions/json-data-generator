package net.acesinc.data.json.generator.source;

import io.deephaven.csv.CsvSpecs;
import static io.deephaven.csv.parsers.DataType.BOOLEAN_AS_BYTE;
import static io.deephaven.csv.parsers.DataType.BYTE;
import static io.deephaven.csv.parsers.DataType.CHAR;
import static io.deephaven.csv.parsers.DataType.DATETIME_AS_LONG;
import static io.deephaven.csv.parsers.DataType.DOUBLE;
import static io.deephaven.csv.parsers.DataType.FLOAT;
import static io.deephaven.csv.parsers.DataType.INT;
import static io.deephaven.csv.parsers.DataType.LONG;
import static io.deephaven.csv.parsers.DataType.SHORT;
import static io.deephaven.csv.parsers.DataType.STRING;
import static io.deephaven.csv.parsers.DataType.TIMESTAMP_AS_LONG;
import io.deephaven.csv.reading.CsvReader;
import io.deephaven.csv.reading.CsvReader.ResultColumn;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.acesinc.data.json.generator.log.FileLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A DataSource that is loaded from a CSV file. 
 * This class uses the Deephaven CVS library which is column based, not row based
 * and it provides Type Inference. 
 */
public class CsvDataSource implements DataSource {

    private static final Logger log = LogManager.getLogger(FileLogger.class);

    public static final String SOURCE_FILENAME = "filename";

    private Random rand;
    private final CsvReader.Result result;

    private int currentReadIndex = 0;

    public CsvDataSource(Map<String, Object> props) throws Exception {
        rand = new Random(System.currentTimeMillis());

        String filename = (String) props.get(SOURCE_FILENAME);

        final InputStream inputStream = new FileInputStream(filename);
        final CsvSpecs specs = CsvSpecs.csv();
        result = CsvReader.read(specs, inputStream, ListSinkFactory.INSTANCE);
        log.info("CVS File [ " + filename + " ] has " + result.numRows() + " rows and " + result.numCols() + " columns");
    }

    /**
     * Returns the next data item like an iterator. The first time you call it, you'll 
     * get the first item in the list, then the second and so on. 
     * @return The next data item, null if you have run out of data items
     */
    @Override
    public Map<String, Object> getNextDataItem() {
        try {
            Map<String, Object> returnResult = getDataItemAtIndex(currentReadIndex);
            currentReadIndex++;
            return returnResult;
        } catch (IndexOutOfBoundsException ioe) {
            log.warn("There are not that many rows", ioe);
        }
        
        return null;
    }

    @Override
    public Map<String, Object> getRandomDataItem() {
        long min = 0L;
        long max = getNumberOfDataItems() - 1L;
        long randomIndex = min + (long) (rand.nextDouble() * (max - min + 1));
        return getDataItemAtIndex(Long.valueOf(randomIndex).intValue());
    }

    public Map<String, Object> getDataItemAtIndex(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= getNumberOfDataItems()) {
            throw new IndexOutOfBoundsException("Index must be within range [ 0, " + getNumberOfDataItems() + " ]");
        }
        
        Map<String, Object> returnResult = new HashMap<>();
        
        for (CsvReader.ResultColumn col : result) {
            addColumnToMap(col, index, returnResult);
        }
        
        return returnResult;
    }
    
    protected void addColumnToMap(ResultColumn col, int index, Map<String, Object> map) {
        String columnName = col.name();
//        log.info("Reading column " + columnName);
        Object value = null;
        switch (col.dataType()) {
            case BOOLEAN_AS_BYTE: {
                List<Byte> data = (List<Byte>) col.data();
                value = data.get(index);
                break;
            }
            case SHORT: {
                List<Short> data = (List<Short>) col.data();
                value = data.get(index);
                break;
            }
            case BYTE: {
                List<Byte> data = (List<Byte>) col.data();
                value = data.get(index);
                break;
            }
            case CHAR: {
                List<Character> data = (List<Character>) col.data();
                value = data.get(index);
                break;
            }
            case DOUBLE: {
                List<Double> data = (List<Double>) col.data();
                value = data.get(index);
                break;
            }
            case FLOAT: {
                List<Float> data = (List<Float>) col.data();
                value = data.get(index);
                break;
            }
            case INT: {
                List<Integer> data = (List<Integer>) col.data();
                value = data.get(index);
                break;
            }
            case LONG:
            case DATETIME_AS_LONG:
            case TIMESTAMP_AS_LONG: {
                List<Long> data = (List<Long>) col.data();
                value = data.get(index);
                break;
            }
            case STRING: {
                List<String> data = (List<String>) col.data();
                value = data.get(index);
                break;
            }
        }

        map.put(columnName, value);
    }

    @Override
    public void shutdown() {

    }

    @Override
    public long getNumberOfDataItems() {
        return result.numRows();
    }
    
    public static void main(String[] args) {
        Map<String, Object> props = new HashMap<>();
        props.put("filename", "/Users/aserff/Downloads/navaids.csv");
        try {
            CsvDataSource source = new CsvDataSource(props);
            log.info(source.getNextDataItem());
            log.info(source.getRandomDataItem());
        } catch (Exception ex) {
            log.error("Error", ex);
        }
    }
}
