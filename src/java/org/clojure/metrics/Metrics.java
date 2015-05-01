package org.clojure.metrics;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Gauge;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author Gorkhover D.
 * @since 2015-04-27
 */
public class Metrics {

//    clock

    public static Clock defaultClock() {
        return Clock.defaultClock();
    }

    public static TimeUnit defaultTimeUnit() {
        return TimeUnit.NANOSECONDS;
    }

    public static Clock userTimeClock() {
        return new Clock.UserTimeClock();
    }

    public static Clock cpuTimeClock() {
        return new Clock.CpuTimeClock();
    }

//    gauge

    public static <T extends Number> SettableGauge<T> gauge(T value) {
        SettableGauge<T> gauge = new NumberGauge<>();
        gauge.setValue(value);
        return gauge;
    }

    public static <T extends Number> Gauge<T> gauge(Callable<T> value) {
        return new CallableGauge<>(value);
    }
}
