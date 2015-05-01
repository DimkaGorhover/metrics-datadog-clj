package org.clojure.metrics;

import java.util.concurrent.Callable;

/**
 * @author Gorkhover D.
 * @since 2015-04-22
 */
class SimpleCallable<T> implements Callable<T> {

    private volatile T value;

    SimpleCallable(T value) {
        this.value = value;
    }

    @Override
    public T call() {
        return value;
    }
}
