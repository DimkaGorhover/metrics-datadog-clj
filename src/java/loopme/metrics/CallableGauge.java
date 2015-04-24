package loopme.metrics;

import com.codahale.metrics.Gauge;

import java.util.concurrent.*;

/**
* @author Gorkhover D.
* @since 2014-11-18
*/
class CallableGauge<T extends Number> implements Gauge<T>, Future<T> {

    private final Callable<T> callable;

    CallableGauge(Callable<T> callable) {
        this.callable = callable;
    }

    @Override
    public T getValue() {
        try {
            return callable.call();
        } catch (Exception e) {
            return null;
        }
    }

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
    public T get() {
        return getValue();
    }

    @Override
    public T get(long timeout, TimeUnit _) {
        return get();
    }
}
