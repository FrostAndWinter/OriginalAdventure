package swen.adventure.engine.datastorage;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Created by liam on 5/10/15.
 */
class Property<T, R> {

    private final Function<T, R> getter;
    private final BiConsumer<T, R> setter;

    public Property(Function<T, R> getter, BiConsumer<T, R> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public boolean isDifferent(T instance1, T instance2) {
        R result1 = getter.apply(instance1);
        R result2 = getter.apply(instance2);
        return Objects.equals(result1, result2);
    }

    public void updateProperty(T instance, R value) {
        setter.accept(instance, value);
    }

}
