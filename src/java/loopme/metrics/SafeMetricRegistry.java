package loopme.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;

/**
 * @author Gorkhover D.
 * @since 2014-12-05
 */
public class SafeMetricRegistry extends MetricRegistry {

    private boolean override = true;

    public static final class Builder {

        private final SafeMetricRegistry registry = new SafeMetricRegistry();

        public Builder setOverride(boolean override) {
            registry.override = override;
            return this;
        }

        public MetricRegistry build() {
            return registry;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Metric> T register(String name, T newMetric) throws IllegalArgumentException {
        if (newMetric == null) {
            return null;
        }
        Metric metric = getMetrics().get(name);
        if (metric != null) {
            if (newMetric.getClass().isAssignableFrom(metric.getClass())) {
                return (T) metric;
            }
            if (override) {
                remove(name);
            }
        }
        return super.register(name, newMetric);
    }
}
