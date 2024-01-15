package net.acesinc.data.json.generator.source;

import io.deephaven.csv.sinks.SinkFactory;
import java.time.Instant;
import java.util.function.UnaryOperator;

/**
 * A custom ListSink for the Deephaven CSV parser so that it returns Lists of objects. 
 * This allows for the List to have Null values in it. 
 * Note that using this Sink has a performance hit over using the built in primitives
 */
public class ListSinkFactory {
     public static final SinkFactory INSTANCE = SinkFactory.ofSimple(
            colNum -> new ListSink<>(colNum, UnaryOperator.identity()),
            colNum -> new ListSink<>(colNum, UnaryOperator.identity()),
            colNum -> new ListSink<>(colNum, UnaryOperator.identity()),
            colNum -> new ListSink<>(colNum, UnaryOperator.identity()),
            colNum -> new ListSink<>(colNum, UnaryOperator.identity()),
            colNum -> new ListSink<>(colNum, UnaryOperator.identity()),
            colNum -> new ListSink<>(colNum, ListSinkFactory::convertToBoolean),
            colNum -> new ListSink<>(colNum, UnaryOperator.identity()),
            colNum -> new ListSink<>(colNum, UnaryOperator.identity()),
            colNum -> new ListSink<>(colNum, ListSinkFactory::convertToInstant),
            colNum -> new ListSink<>(colNum, ListSinkFactory::convertToInstant)
    );

    private static Boolean convertToBoolean(Byte o) {
        return o != 0;
    }

    private static Instant convertToInstant(Long totalNanos) {
        long seconds = totalNanos / 1_000_000_000;
        long nanos = totalNanos % 1_000_000_000;
        return Instant.ofEpochSecond(seconds, nanos);
    }
}
