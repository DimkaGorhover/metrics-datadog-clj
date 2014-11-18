package loopme.metrics;

import com.codahale.metrics.*;

import java.util.concurrent.Callable;

/**
 * @author Gorkhover D.
 * @since 2014-11-17
 */
public final class Factory {

    private Factory() {}

    public static <T extends Number> Gauge<T> gauge(T value) {
        final T newValue = value;
        return new Gauge<T>() {
            @Override
            public T getValue() {
                return newValue;
            }
        };
    }

    public static <T extends Number> Gauge<T> gauge(Callable<T> value) {
        final Callable<T> newValue = value;
        return new Gauge<T>() {
            @Override
            public T getValue() {
                try {
                    return newValue.call();
                } catch (Exception e) {
                    return null;
                }
            }
        };
    }
}
