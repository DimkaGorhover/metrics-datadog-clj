package loopme.metrics;

import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Snapshot;

import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Gorkhover D.
 * @since 2015-04-22
 */
public class FutureHistogram extends Histogram implements Future<IPersistentMap> {
    /**
     * Creates a new {@link Histogram} with the given reservoir.
     *
     * @param reservoir the reservoir to create a histogram from
     */
    public FutureHistogram(Reservoir reservoir) {
        super(reservoir);
    }

    @Override
    public boolean cancel(boolean _) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public IPersistentMap get() {
        return wrap(getSnapshot());
    }

    @Override
    public IPersistentMap get(long timeout, TimeUnit _) {
        return get();
    }

    @Override
    public Snapshot getSnapshot() {
        return super.getSnapshot();
    }

    private static IPersistentMap wrap(Snapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        HashMap<Keyword, Double> map = new HashMap<>();
        map.put(Keyword.intern("75"), snapshot.get75thPercentile());
        map.put(Keyword.intern("95"), snapshot.get95thPercentile());
        map.put(Keyword.intern("98"), snapshot.get98thPercentile());
        map.put(Keyword.intern("99"), snapshot.get99thPercentile());
        map.put(Keyword.intern("999"), snapshot.get999thPercentile());
        map.put(Keyword.intern("mean"), snapshot.getMean());
        map.put(Keyword.intern("median"), snapshot.getMedian());
        map.put(Keyword.intern("std-dev"), snapshot.getStdDev());
        map.put(Keyword.intern("max"), (double) snapshot.getMax());
        map.put(Keyword.intern("min"), (double) snapshot.getMin());
        return PersistentHashMap.create(map);
    }
}
