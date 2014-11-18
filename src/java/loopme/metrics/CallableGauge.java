package loopme.metrics;

import com.codahale.metrics.Gauge;

import java.util.concurrent.Callable;

/**
* @author Gorkhover D.
* @since 2014-11-18
*/
class CallableGauge<T extends Number> implements Gauge<T> {

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
}
