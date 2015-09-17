package swen.adventure.datastorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 17/09/15.
 */
public class BundleArray {

    private final List<Object> storedValues = new ArrayList<>();

    public void put(int value) {
        putValue(size(), value);
    }

    public void put(int index, int value) {
        putValue(index, value);
    }

    public int getInt(int index) {
        return getValue(index, Integer.class);
    }

    public void put(float value) {
        putValue(size(), value);
    }

    public void put(int index, float value) {
        putValue(index, value);
    }

    public float getFloat(int index) {
        return getValue(index, Float.class);
    }

    public void put(String value) {
        putValue(size(), value);
    }

    public void put(int index, String value) {
        putValue(index, value);
    }

    public String getString(int index) {
        return getValue(index, String.class);
    }

    public void put(long value) {
        putValue(size(), value);
    }

    public void put(int index, long value) {
        putValue(index, value);
    }

    public long getLong(int index) {
        return getValue(index, Long.class);
    }

    public void put(BundleArray value) {
        putValue(size(), value);
    }

    public void put(int index, BundleArray value) {
        putValue(index, value);
    }

    public BundleArray getBundleArray(int index) {
        return getValue(index, BundleArray.class);
    }

    public void put(BundleObject value) {
        putValue(size(), value);
    }

    public void put(int index, BundleObject value) {
        putValue(index, value);
    }

    public BundleObject getBundleObject(int index) {
        return getValue(index, BundleObject.class);
    }

    private <T> T getValue(int index, Class<T> class0) {
        if(index < 0 || index >= size())
            throw new IllegalArgumentException("index is out of bounds " + index);

        Object value = storedValues.get(index);

        if(!class0.isInstance(value))
            throw new ClassCastException("Property at index" + index + " isn't a type of " + class0.getSimpleName());

        return class0.cast(value);
    }

    private void putValue(int index, Object value) {
        storedValues.add(index, value);
    }

    public int size() {
        return storedValues.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BundleArray array = (BundleArray) o;

        return storedValues.equals(array.storedValues);

    }

    @Override
    public int hashCode() {
        return storedValues.hashCode();
    }

    @Override
    public String toString() {
        return "BundleObject={" + storedValues + "}";
    }
}
