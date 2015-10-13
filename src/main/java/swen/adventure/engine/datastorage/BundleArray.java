package swen.adventure.engine.datastorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 17/09/15.
 *
 * BundleArray maps indices to values. The values can have the type of {@code int, float, long, BundleArray or BundleObject}.
 * It also provides type safe access to the values through the various get methods.
 *
 * It allows chaining the calls on put e.g. {@code bundleArray.put(3).put("Hello World!").put(3.14f);}
 */
public class BundleArray {

    // list of properties which have been added
    private final List<BundleProperty> storedValues;

    /**
     * Construct a new empty BundleArray.
     */
    public BundleArray(){
        this.storedValues = new ArrayList<>();
    }

    /**
     * Package-private constructor which constructs a new BundleArray with the given properties.
     */
    BundleArray(List<BundleProperty> storedValues) {
        this.storedValues = new ArrayList<>(storedValues);
    }

    /**
     * Puts an int value at the end of this BundleArray.
     *
     * @param value value to assign at the end of this array.
     * @return itself (the bundle array which is being added to) to allow chaining calls.
     */
    public BundleArray put(int value) { putValue(size(), value); return this; }

    /**
     * Puts an int value at the index specified.
     * If index isn't equal to size() this put call will override the value at that index.
     *
     * @param index the index specifying where to add this value.
     * @param value value to assign at the specified index.
     * @return itself (the bundle array which is being added to) to allow chaining calls.
     */
    public BundleArray put(int index, int value) { putValue(index, value); return this; }

    /**
     * Get a int value at index.
     *
     * @param index index to retrieve the value.
     * @throws ClassCastException will be thrown in the case where the value present at index isn't of type int.
     * @throws IndexOutOfBoundsException will be thrown if index is out of bounds
     * @return the value at index
     */
    public int getInt(int index) { return getValue(index, Integer.class); }


    /**
     * Put a float value at the end of this BundleArray.
     *
     * @param value value to assign at the end of this array.
     * @return itself (the bundle array which is being added to) to allow chaining calls.
     */
    public BundleArray put(float value) { putValue(size(), value); return this; }

    /**
     * Puts a float value at the index specified.
     * If index isn't equal to size() this put call will override the value at that index.
     *
     * @param index the index specifying where to add this value.
     * @param value value to assign at the specified index.
     * @return itself (the bundle array which is being added to) to allow chaining calls.
     */
    public BundleArray put(int index, float value) { putValue(index, value); return this; }

    /**
     * Get a float value at index.
     *
     * @param index index to retrieve the value.
     * @throws ClassCastException will be thrown in the case where the value present at index isn't of type float.
     * @throws IndexOutOfBoundsException will be thrown if index is out of bounds
     * @return the value at index
     */
    public float getFloat(int index) { return getValue(index, Float.class); }


    /**
     * Put a String value at the end of this BundleArray.
     *
     * @param value value to assign at the end of this array.
     * @return itself (the bundle array which is being added to) to allow chaining calls.
     */
    public BundleArray put(String value) { putValue(size(), value); return this; }

    /**
     * Puts a String value at the index specified.
     * If index isn't equal to size() this put call will override the value at that index.
     *
     * @param index the index specifying where to add this value.
     * @param value value to assign at the specified index.
     * @return itself (the bundle array which is being added to) to allow chaining calls.
     */
    public BundleArray put(int index, String value) { putValue(index, value); return this; }

    /**
     * Get a String value at index.
     *
     * @param index index to retrieve the value.
     * @throws ClassCastException will be thrown in the case where the value present at index isn't of type String.
     * @throws IndexOutOfBoundsException will be thrown if index is out of bounds
     * @return the value at index
     */
    public String getString(int index) { return getValue(index, String.class); }


    /**
     * Put a long value at the end of this BundleArray.
     *
     * @param value value to assign at the end of this array.
     * @return itself (the bundle array which is being added to) to allow chaining calls.
     */
    public BundleArray put(long value) { putValue(size(), value); return this; }

    /**
     * Puts a long value at the index specified.
     * If index isn't equal to size() this put call will override the value at that index.
     *
     * @param index the index specifying where to add this value.
     * @param value value to assign at the specified index.
     * @return itself (the bundle array which is being added to) to allow chaining calls.
     */
    public BundleArray put(int index, long value) { putValue(index, value); return this; }

    /**
     * Get a long value at index.
     *
     * @param index index to retrieve the value.
     * @throws ClassCastException will be thrown in the case where the value present at index isn't of type long.
     * @throws IndexOutOfBoundsException will be thrown if index is out of bounds
     * @return the value at index
     */
    public long getLong(int index) { return getValue(index, Long.class); }


    /**
     * Put a BundleArray value at the end of this BundleArray.
     *
     * @param value value to assign at the end of this array.
     * @return itself (the bundle array which is being added to) to allow chaining calls.
     */
    public BundleArray put(BundleArray value) { putValue(size(), value); return this; }

    /**
     * Puts a BundleArray value at the index specified.
     * If index isn't equal to size() this put call will override the value at that index.
     *
     * @param index the index specifying where to add this value.
     * @param value value to assign at the specified index.
     * @return itself (the bundle array which is being added to) to allow chaining calls.
     */
    public BundleArray put(int index, BundleArray value) { putValue(index, value); return this; }

    /**
     * Get a BundleArray value at index.
     *
     * @param index index to retrieve the value.
     * @throws ClassCastException will be thrown in the case where the value present at index isn't of type BundleArray.
     * @throws IndexOutOfBoundsException will be thrown if index is out of bounds
     * @return the value at index
     */
    public BundleArray getBundleArray(int index) { return getValue(index, BundleArray.class); }


    /**
     * Put a BundleObject value at the end of this BundleArray.
     *
     * @param value value to assign at the end of this array.
     * @return itself (the bundle array which is being added to) to allow chaining calls.
     */
    public BundleArray put(BundleObject value) { putValue(size(), value); return this; }

    /**
     * Puts a BundleObject value at the index specified.
     * If index isn't equal to size() this put call will override the value at that index.
     *
     * @param index the index specifying where to add this value.
     * @param value value to assign at the specified index.
     * @return itself (the bundle array which is being added to) to allow chaining calls.
     */
    public BundleArray put(int index, BundleObject value) { putValue(index, value); return this; }

    /**
     * Get a BundleObject value at index.
     *
     * @param index index to retrieve the value.
     * @throws ClassCastException will be thrown in the case where the value present at index isn't of type BundleObject.
     * @throws IndexOutOfBoundsException will be thrown if index is out of bounds
     * @return the value at index
     */
    public BundleObject getBundleObject(int index) { return getValue(index, BundleObject.class); }

    /**
     * Helper method for retrieving a value at a particular index.
     *
     * @param index index at which to retrieve the value.
     * @param class0 type of the value at the index.
     * @throws IndexOutOfBoundsException if {@code index < 0 || index >= size()}.
     * @throws ClassCastException if the type of the value at index doesn't match class0.
     * @return the object which has been cast to its type.
     */
    private <T> T getValue(int index, Class<T> class0) {
        if(index < 0 || index >= size())
            throw new IndexOutOfBoundsException("index is out of bounds " + index);

        BundleProperty property = storedValues.get(index);

        if(class0 != property.class0)
            throw new ClassCastException("BundleProperty at index" + index + " isn't a type of " + class0.getSimpleName());

        return class0.cast(property.value);
    }

    /**
     * Helper method to add a new property to the backing list.
     *
     * @param index index to add to
     * @param value value to add
     */
    private void putValue(int index, Object value) {
        storedValues.add(index, new BundleProperty(Integer.toString(index), value, value.getClass()));
    }

    /**
     * Returns the size of this BundleArray.
     * The size is equal to the number of present values.
     *
     * @return the size of this bundleArray instance.
     */
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

    /**
     * Package-private method to return all the underlying properties.
     * @return an unmodifiable list of all properties this bundleArray instance has.
     */
    List<BundleProperty> getProperties() {
        return Collections.unmodifiableList(storedValues);
    }
}
