package swen.adventure.datastorage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 17/09/15.
 */
public class BundleObject {

    private final Map<String, Property> storedValues;

    public BundleObject() {
        this.storedValues = new HashMap<>();
    }

    BundleObject(Map<String, Property> storedValues) {
        this.storedValues = storedValues;
    }

    public void put(String name, int value) { put(name, value, Integer.class); }
    public int getInt(String name) { return getValue(name, Integer.class); }

    public void put(String name, float value) { put(name, value, Float.class); }
    public float getFloat(String name) { return getValue(name, Float.class); }

    public void put(String name, long value) { put(name, value, Long.class); }
    public long getLong(String name) { return getValue(name, Long.class); }

    public void put(String name, String value) { put(name, value, String.class); }
    public String getString(String name) { return getValue(name, String.class); }

    public void put(String name, BundleArray bundleArray) { put(name, bundleArray, BundleArray.class); }
    public BundleArray getBundleArray(String name) { return getValue(name, BundleArray.class); }

    public void put(String name, BundleObject bundleObject) { put(name, bundleObject, BundleObject.class); }
    public BundleObject getBundleObject(String name) { return getValue(name, BundleObject.class); }

    public boolean hasProperty(String name) {
        return storedValues.containsKey(name);
    }

    Iterable<Property> getProperties() {
        return storedValues.values();
    }

    private <T> void put(String name, T value, Class<T> class0) {
        storedValues.put(name, new Property(name, value, class0));
    }

    private <T> T getValue(String name, Class<T> class0) {
        Property property = storedValues.get(name);
        if(property == null)
            throw new IllegalArgumentException("Property " + name + " doesn't exist.");

        if(class0 != property.class0)
            throw new ClassCastException("Property \"" + name + "\" isn't a type of " + class0.getSimpleName());

        return class0.cast(property.value);
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
                .map(pair -> pair.getKey() + "=" + pair.getValue().value)
                .collect(Collectors.toList());

        return "BundleObject={" + String.join(", ", keyValueStrings) + "}";
    }
}
