package loopme.metrics;

import com.codahale.metrics.Gauge;

/**
 * @author Gorkhover D.
 * @since 2014-11-18
 */
public interface SettableGauge<T> extends Gauge<T> {

    T setValue(T value);
}
