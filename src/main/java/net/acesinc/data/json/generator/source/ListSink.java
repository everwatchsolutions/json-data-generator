package net.acesinc.data.json.generator.source;

import io.deephaven.csv.sinks.Sink;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * This ListSink will return an List of Objects instead of an Array of primitives.
 * This allows for there to be null items in the list when the field in the csv is empty
 */
public class ListSink<TARRAY, T, TARGET> implements Sink<TARRAY> {
    private final List<TARGET> list;
    private final Function<T, TARGET> converter;

    public ListSink(int colNum, Function<T, TARGET> converter) {
        // colNum is unused in this sink
        this.list = new ArrayList<>();
        this.converter = converter;
    }

    @Override
    public final void write(
            final TARRAY src,
            final boolean[] isNull,
            final long destBegin,
            final long destEnd,
            boolean appending) {
        if (destBegin == destEnd) {
            return;
        }
        final int destBeginAsInt = Math.toIntExact(destBegin);
        final int destEndAsInt = Math.toIntExact(destEnd);
        final int destSize = Math.toIntExact(destEnd - destBegin);

        // Ensure capacity
        while (list.size() < destEndAsInt) {
            list.add(null);
        }

        // Populate elements
        for (int i = 0; i < destSize; ++i) {
            TARGET converted = null;
            if (!isNull[i]) {
                T element = (T) Array.get(src, i);
                converted = converter.apply(element);
            }
            list.set(destBeginAsInt + i, converted);
        }
    }

    @Override
    public List<TARGET> getUnderlying() {
        return list;
    }
}
