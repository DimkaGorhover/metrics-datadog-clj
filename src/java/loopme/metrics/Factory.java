package loopme.metrics;

import com.codahale.metrics.Gauge;

import java.util.concurrent.Callable;

/**
 * @author Gorkhover D.
 * @since 2014-11-17
 */
public final class Factory {

    private Factory() {
    }

    public static <T extends Number> SettableGauge<T> gauge(T value) {
        SettableGauge<T> gauge = new NumberGauge<>();
        gauge.setValue(value);
        return gauge;
    }

    public static <T extends Number> Gauge<T> gauge(Callable<T> value) {
        return new CallableGauge<>(value);
    }
}
