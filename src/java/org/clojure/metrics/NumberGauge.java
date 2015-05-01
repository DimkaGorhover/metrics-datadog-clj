package org.clojure.metrics;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
* @author Gorkhover D.
* @since 2014-11-18
*/
class NumberGauge<T extends Number> implements SettableGauge<T>, Future<T> {

    private volatile Callable<T> value;

    NumberGauge() {
    }

    public void setValue(T value) {
        this.value = new SimpleCallable<>(value);
    }

    @Override
    public void setValue(Callable<T> callable) {
        this.value = callable;
    }

    @Override
    public T getValue() {
        try {
            return value.call();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
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
    public T get() {
        return getValue();
    }

    @Override
    public T get(long timeout, TimeUnit _) {
        return get();
    }
}
