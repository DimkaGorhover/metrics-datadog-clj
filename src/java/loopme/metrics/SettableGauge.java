package loopme.metrics;

import com.codahale.metrics.Gauge;

import java.util.concurrent.Callable;

/**
 * @author Gorkhover D.
 * @since 2014-11-18
 */
public interface SettableGauge<T> extends Gauge<T> {

    void setValue(T value);

    void setValue(Callable<T> callable);
}
