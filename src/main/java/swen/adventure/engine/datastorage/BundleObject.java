package swen.adventure.engine.datastorage;

import swen.adventure.engine.rendering.maths.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 17/09/15.
 *
 * BundleObject maps names to values. The values can have the type of {@code int, float, long, BundleArray or BundleObject}.
 * It also provides type safe access to the values through the various get methods.
 *
 * It allows chaining the calls on put e.g. {@code bundleObject.put("id", 3).put("message", "Hello World!").put("alright number", 3.14f);}
 */
public class BundleObject implements BundleSerializable {

    private final Map<String, BundleProperty> storedValues;

    /**
     * Construct a new empty BundleObject.
     */
    public BundleObject() {
        this.storedValues = new HashMap<>();
    }

    /**
     * Construct a new BundleObject with the existing properties.
     */
    BundleObject(Map<String, BundleProperty> storedValues) {
        this.storedValues = new HashMap<>(storedValues);
    }

    /**
     * Maps name to this value.
     *
     * @param name name to associate with value.
     * @param value value to be mapped to by name.
     * @return itself (the bundle array which is being added to) to allow chaining calls.
     */
    public BundleObject put(String name, boolean value) { putValue(name, value); return this; }

    /**
     * Get a boolean value which is mapped to by the given name.
     *
     * @param name name to associate with value.
     * @return the boolean value which is associated to by the given name.
     * @throws ClassCastException if the value associated by name isn't of type boolean.
     * @throws IllegalArgumentException if the given name doesn't map to any value.
     */
    public boolean getBoolean(String name) { return getValue(name, Boolean.class); }

    /**
     * Maps name to this value.
     *
     * @param name name to associate with value.
     * @param value value to be mapped to by name.
     * @return itself (the bundle array which is being added to) to allow chaining calls.
     */
    public BundleObject put(String name, int value) { putValue(name, value); return this; }

    /**
     * Get a int value which is mapped to by the given name.
     *
     * @param name name to associate with value.
     * @return the int value which is associated to by the given name.
     * @throws ClassCastException if the value associated by name isn't of type int.
     * @throws IllegalArgumentException if the given name doesn't map to any value.
     */
    public int getInt(String name) { return getValue(name, Integer.class); }

    /**
     * Maps name to this value.
     *
     * @param name name to associate with value.
     * @param value value to be mapped to by name.
     * @return itself (the bundle array which is being added to) to allow chaining calls.
     */
    public BundleObject put(String name, float value) { putValue(name, value); return this; }

    /**
     * Get a float value which is mapped to by the given name.
     *
     * @param name name to associate with value.
     * @return the float value which is associated to by the given name.
     * @throws ClassCastException if the value associated by name isn't of type float.
     * @throws IllegalArgumentException if the given name doesn't map to any value.
     */
    public float getFloat(String name) { return getValue(name, Float.class); }

    /**
     * Maps name to this value.
     *
     * @param name name to associate with value.
     * @param value value to be mapped to by name.
     * @return itself (the bundle array which is being added to) to allow chaining calls.
     */
    public BundleObject put(String name, long value) { putValue(name, value); return this; }

    /**
     * Get a long value which is mapped to by the given name.
     *
     * @param name name to associate with value.
     * @return the long value which is associated to by the given name.
     * @throws ClassCastException if the value associated by name isn't of type long.
     * @throws IllegalArgumentException if the given name doesn't map to any value.
     */
    public long getLong(String name) { return getValue(name, Long.class); }

    /**
     * Maps name to this value.
     *
     * @param name name to associate with value.
     * @param value value to be mapped to by name.
     * @return itself (the bundle array which is being added to) to allow chaining calls.
     */
    public BundleObject put(String name, String value) { putValue(name, value); return this; }

    /**
     * Get a String value which is mapped to by the given name.
     *
     * @param name name to associate with value.
     * @return the String value which is associated to by the given name.
     * @throws ClassCastException if the value associated by name isn't of type String.
     * @throws IllegalArgumentException if the given name doesn't map to any value.
     */
    public String getString(String name) { return getValue(name, String.class); }

    /**
     * Maps name to this value.
     *
     * @param name name to associate with value.
     * @param value value to be mapped to by name.
     * @return itself (the bundle array which is being added to) to allow chaining calls.
     */
    public BundleObject put(String name, BundleArray value) { putValue(name, value); return this; }

    /**
     * Get a BundleArray value which is mapped to by the given name.
     *
     * @param name name to associate with value.
     * @return the BundleArray value which is associated to by the given name.
     * @throws ClassCastException if the value associated by name isn't of type BundleArray.
     * @throws IllegalArgumentException if the given name doesn't map to any value.
     */
    public BundleArray getBundleArray(String name) { return getValue(name, BundleArray.class); }

    /**
     * Maps name to this value.
     *
     * @param name name to associate with value.
     * @param value value to be mapped to by name.
     * @return itself (the bundle array which is being added to) to allow chaining calls.
     */
    public BundleObject put(String name, BundleObject value) { putValue(name, value); return this; }

    /**
     * Get a BundleObject value which is mapped to by the given name.
     *
     * @param name name to associate with value.
     * @return the BundleObject value which is associated to by the given name.
     * @throws ClassCastException if the value associated by name isn't of type BundleObject.
     * @throws IllegalArgumentException if the given name doesn't map to any value.
     */
    public BundleObject getBundleObject(String name) { return getValue(name, BundleObject.class); }

    /**
     * Maps name to this value.
     *
     * @param name name to associate with value.
     * @param value value to be mapped to by name.
     * @return itself (the bundle array which is being added to) to allow chaining calls.
     */
    public BundleObject put(String name, Vector3 value) { putValue(name, value); return this; }

    /**
     * Get a Vector3 value which is mapped to by the given name.
     *
     * @param name name to associate with value.
     * @return the Vector3 value which is associated to by the given name.
     * @throws ClassCastException if the value associated by name isn't of type Vector3.
     * @throws IllegalArgumentException if the given name doesn't map to any value.
     */
    public Vector3 getVector3(String name) { return getValue(name, Vector3.class); }

    /**
     * Maps name to this value.
     *
     * @param name name to associate with value.
     * @param value value to be mapped to by name.
     * @return itself (the bundle array which is being added to) to allow chaining calls.
     */
    public BundleObject put(String name, Vector4 value) { putValue(name, value); return this; }

    /**
     * Get a Vector4 value which is mapped to by the given name.
     *
     * @param name name to associate with value.
     * @return the Vector4 value which is associated to by the given name.
     * @throws ClassCastException if the value associated by name isn't of type Vector4.
     * @throws IllegalArgumentException if the given name doesn't map to any value.
     */
    public Vector4 getVector4(String name) { return getValue(name, Vector4.class); }

    /**
     * Maps name to this value.
     *
     * @param name name to associate with value.
     * @param value value to be mapped to by name.
     * @return itself (the bundle array which is being added to) to allow chaining calls.
     */
    public BundleObject put(String name, Quaternion value) { putValue(name, value); return this; }

    /**
     * Get a Quaternion value which is mapped to by the given name.
     *
     * @param name name to associate with value.
     * @return the Quaternion value which is associated to by the given name.
     * @throws ClassCastException if the value associated by name isn't of type Quaternion.
     * @throws IllegalArgumentException if the given name doesn't map to any value.
     */
    public Quaternion getQuaternion(String name) { return getValue(name, Quaternion.class); }

    /**
     * Returns whether this bundleObject has a property with the given name.
     *
     * @param name name to test for
     * @return true iff there is a value associated with that name.
     */
    public boolean hasProperty(String name) {
        return storedValues.containsKey(name);
    }

    /**
     * Package-private method to get all the properties stored in this collection.
     * @return all properties of this bundleObject
     */
    Iterable<BundleProperty> getProperties() {
        return Collections.unmodifiableCollection(storedValues.values());
    }

    /**
     * Helper method to add a new property to the backing map.
     *
     * @param name name of the property.
     * @param value value of the property.
     */
    private <T> void putValue(String name, T value) {
        storedValues.put(name, new BundleProperty(name, value, value.getClass()));
    }

    /**
     * Helper method for retrieving a value with a particular name.
     *
     * @param name name of the property.
     * @param class0 type of the value.
     * @throws ClassCastException if the type of the value doesn't match class0.
     * @return the object which has been cast to its type.
     */
    private <T> T getValue(String name, Class<T> class0) {
        BundleProperty property = storedValues.get(name);
        if(property == null)
            throw new IllegalArgumentException("BundleProperty " + name + " doesn't exist.");

        if(class0 != property.class0)
            throw new ClassCastException("BundleProperty \"" + name + "\" isn't a type of " + class0.getSimpleName());

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
