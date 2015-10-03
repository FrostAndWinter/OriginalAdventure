package swen.adventure.engine.datastorage;

import swen.adventure.engine.rendering.maths.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 17/09/15.
 */
public class BundleObject implements BundleSerializable {

    private final Map<String, Property> storedValues;

    public BundleObject() {
        this.storedValues = new HashMap<>();
    }

    BundleObject(Map<String, Property> storedValues) {
        this.storedValues = storedValues;
    }

    public BundleObject put(String name, boolean value) { putValue(name, value); return this; }
    public boolean getBoolean(String name) { return getValue(name, Boolean.class); }

    public BundleObject put(String name, int value) { putValue(name, value); return this; }
    public int getInt(String name) { return getValue(name, Integer.class); }

    public BundleObject put(String name, float value) { putValue(name, value); return this; }
    public float getFloat(String name) { return getValue(name, Float.class); }

    public BundleObject put(String name, long value) { putValue(name, value); return this; }
    public long getLong(String name) { return getValue(name, Long.class); }

    public BundleObject put(String name, String value) { putValue(name, value); return this; }
    public String getString(String name) { return getValue(name, String.class); }

    public BundleObject put(String name, BundleArray value) { putValue(name, value); return this; }
    public BundleArray getBundleArray(String name) { return getValue(name, BundleArray.class); }

    public BundleObject put(String name, BundleObject value) { putValue(name, value); return this; }
    public BundleObject getBundleObject(String name) { return getValue(name, BundleObject.class); }

    public BundleObject put(String name, Vector3 value) { putValue(name, value); return this; }
    public Vector3 getVector3(String name) { return getValue(name, Vector3.class); }

    public BundleObject put(String name, Vector4 value) { putValue(name, value); return this; }
    public Vector4 getVector4(String name) { return getValue(name, Vector4.class); }

    public BundleObject put(String name, Quaternion value) { putValue(name, value); return this; }
    public Quaternion getQuaternion(String name) { return getValue(name, Quaternion.class); }

    public BundleObject put(String name, Matrix3 value) { putValue(name, value); return this; }
    public Matrix3 getMatrix3(String name) { return getValue(name, Matrix3.class); }

    public BundleObject put(String name, Matrix4 value) { putValue(name, value); return this; }
    public Matrix4 getMatrix4(String name) { return getValue(name, Matrix4.class); }

    public boolean hasProperty(String name) {
        return storedValues.containsKey(name);
    }

    Iterable<Property> getProperties() {
        return storedValues.values();
    }

    private <T> void putValue(String name, T value) {
        storedValues.put(name, new Property(name, value, value.getClass()));
    }

    /*private <T> Optional<T> getOptionalValue(String name, Class<T> class0) {
        Property property = storedValues.get(name);
        if(property == null)
            return Optional.empty();

        if(class0 != property.class0)
            return Optional.empty();

        return Optional.of(class0.cast(property.value));
    }*/

    private <T> T getValue(String name, Class<T> class0) {
        Property property = storedValues.get(name);
        if(property == null)
            throw new IllegalArgumentException("Property " + name + " doesn't exist.");

        if(class0 != property.class0)
            throw new ClassCastException("Property \"" + name + "\" isn't a type of " + class0.getSimpleName());

        return class0.cast(property.value);
    }

    @Override
    public BundleObject toBundle() {
        return this;
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
