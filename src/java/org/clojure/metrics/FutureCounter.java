package org.clojure.metrics;

import com.codahale.metrics.Counter;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Gorkhover D.
 * @since 2015-04-22
 */
public class FutureCounter extends Counter implements Future<Long> {
    @Override
    public boolean cancel(boolean _) {
        return true;
    }

    @Override
    public boolean isCancelled() {
        return true;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public Long get() {
        return this.getCount();
    }

    @Override
    public Long get(long timeout, TimeUnit _) {
        return get();
    }
}
