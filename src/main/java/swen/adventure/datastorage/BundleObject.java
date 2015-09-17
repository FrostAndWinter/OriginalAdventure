package swen.adventure.datastorage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 17/09/15.
 */
public class BundleObject {

    private final Map<String, Object> storedValues = new HashMap<>();

    public void put(String name, int value) {
        putValue(name, value);
    }

    public int getInt(String name) {
        return getValue(name, Integer.class);
    }

    public void put(String name, float value) {
        putValue(name, value);
    }

    public float getFloat(String name) {
        return getValue(name, Float.class);
    }

    public void put(String name, long value) {
        putValue(name, value);
    }

    public long getLong(String name) {
        return getValue(name, Long.class);
    }

    public void put(String name, String value) {
        putValue(name, value);
    }

    public String getString(String name) {
        return getValue(name, String.class);
    }

    public void put(String name, BundleArray bundleArray) {
        putValue(name, bundleArray);
    }

    public BundleArray getBundleArray(String name) {
        return getValue(name, BundleArray.class);
    }

    public void put(String name, BundleObject bundleObject) {
        putValue(name, bundleObject);
    }

    public BundleObject getBundleObject(String name) {
        return getValue(name, BundleObject.class);
    }

    private void putValue(String name, Object value) {
        storedValues.put(name, value);
    }

    private <T> T getValue(String name, Class<T> class0) {
        Object value = storedValues.get(name);
        if(value == null)
            throw new IllegalArgumentException("Property " + name + " doesn't exist.");

        if(!class0.isInstance(value))
            throw new ClassCastException("Property " + name + " isn't a type of " + class0.getSimpleName());

        return class0.cast(value);
    }

    public boolean hasProperty(String name) {
        return storedValues.containsKey(name);
    }

    private static String toString(Object value) {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BundleObject that = (BundleObject) o;

        return storedValues.equals(that.storedValues);

    }

    @Override
    public int hashCode() {
        return storedValues.hashCode();
    }

    @Override
    public String toString() {
        List<String> keyValueStrings = storedValues.entrySet().stream()
                .map(pair -> pair.getKey() + "=" + toString(pair.getValue()))
                .collect(Collectors.toList());

        return "BundleObject={" + String.join(", ", keyValueStrings) + "}";
    }
}
