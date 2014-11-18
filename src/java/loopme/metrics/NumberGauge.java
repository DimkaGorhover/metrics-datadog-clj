package loopme.metrics;

/**
* @author Gorkhover D.
* @since 2014-11-18
*/
class NumberGauge<T extends Number> implements SettableGauge<T> {

    private volatile T value;

    NumberGauge() {
    }

    public T setValue(T value) {
        this.value = value;
        return value;
    }

    @Override
    public T getValue() {
        return value;
    }
}
