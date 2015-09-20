package swen.adventure.datastorage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 17/09/15.
 */
public class BundleArray {

    private final List<Property> storedValues = new ArrayList<>();

    public BundleArray put(int value) { putValue(size(), value); return this; }
    public BundleArray put(int index, int value) { putValue(index, value); return this; }
    public int getInt(int index) { return getValue(index, Integer.class); }

    public BundleArray put(float value) { putValue(size(), value); return this; }
    public BundleArray put(int index, float value) { putValue(index, value); return this; }
    public float getFloat(int index) { return getValue(index, Float.class); }

    public BundleArray put(String value) { putValue(size(), value); return this; }
    public BundleArray put(int index, String value) { putValue(index, value); return this; }
    public String getString(int index) { return getValue(index, String.class); }

    public BundleArray put(long value) { putValue(size(), value); return this; }
    public BundleArray put(int index, long value) { putValue(index, value); return this; }
    public long getLong(int index) { return getValue(index, Long.class); }

    public BundleArray put(BundleArray value) { putValue(size(), value); return this; }
    public BundleArray put(int index, BundleArray value) { putValue(index, value); return this; }
    public BundleArray getBundleArray(int index) { return getValue(index, BundleArray.class); }

    public BundleArray put(BundleObject value) { putValue(size(), value); return this; }
    public BundleArray put(int index, BundleObject value) { putValue(index, value); return this; }
    public BundleObject getBundleObject(int index) { return getValue(index, BundleObject.class); }

    private <T> T getValue(int index, Class<T> class0) {
        if(index < 0 || index >= size())
            throw new IllegalArgumentException("index is out of bounds " + index);

        Property property = storedValues.get(index);

        if(class0 != property.class0)
            throw new ClassCastException("Property at index" + index + " isn't a type of " + class0.getSimpleName());

        return class0.cast(property.value);
    }

    private void putValue(int index, Object value) {
        storedValues.add(index, new Property(Integer.toString(index), value, value.getClass()));
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
        List<String> toStrings = storedValues.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
        return "BundleArray{" + String.join(",", toStrings) + '}';
    }
}
