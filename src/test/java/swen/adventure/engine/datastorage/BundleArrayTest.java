package swen.adventure.engine.datastorage;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 17/09/15.
 */
public class BundleArrayTest {

    private final BundleArray array = new BundleArray();

    @Test
    public void testValidPutInt() throws Exception {
        array.put(4);
        assertEquals(4, array.getInt(0));
    }

    @Test
    public void testValidPutString() throws Exception {
        array.put("string");
        assertEquals("string", array.getString(0));
    }

    @Test
    public void testValidPutLong() throws Exception {
        array.put(5L);
        assertEquals(5L, array.getLong(0));
    }

    @Test
    public void testValidPutFloat() throws Exception {
        array.put(5f);
        assertEquals(5f, array.getFloat(0), 0);
    }

    @Test
    public void testValidPutBundleArray() throws Exception {
        BundleArray otherArray = new BundleArray();
        array.put(otherArray);
        assertEquals(otherArray, array.getBundleArray(0));
    }

    @Test
    public void testValidPutBundleObject() throws Exception {
        BundleObject bundleObject = new BundleObject();
        bundleObject.put("test0", 4);
        array.put(bundleObject);
        assertEquals(bundleObject, array.getBundleObject(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidGet1() throws Exception {
        array.getInt(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidGet2() throws Exception {
        array.getInt(array.size());
    }

    @Test(expected = ClassCastException.class)
    public void testGetWrongType() throws Exception {
        array.put(5);
        array.getString(0);
    }

    @Test
    public void testEqualsReflexive() throws Exception {
        testEquals(array, array);
        array.put(5);
        testEquals(array, array);
    }

    @Test
    public void testEqualsTransitive() throws Exception {
        BundleArray array1 = new BundleArray();
        BundleArray array2 = new BundleArray();
        BundleArray array3 = new BundleArray();

        putAllKeys(array1);
        putAllKeys(array2);
        putAllKeys(array3);

        testEquals(array1, array2);
        testEquals(array2, array3);
        testEquals(array1, array3);
    }

    @Test
    public void testEqualsSymmetric() throws Exception {
        BundleArray array1 = new BundleArray();
        BundleArray array2 = new BundleArray();

        testEquals(array1, array2);

        putAllKeys(array1);
        putAllKeys(array2);

        testEquals(array1, array2);
    }

    @Test
    public void testEqualsNotEquals() throws Exception {
        array.put(5);
        BundleObject other = new BundleObject();
        assertNotEquals(array, other);
    }

    private static void testEquals(BundleArray bundleArray1, BundleArray bundleArray2) throws Exception {
        assertEquals(bundleArray1, bundleArray2);
        assertEquals(bundleArray1.hashCode(), bundleArray2.hashCode());
    }

    private static void putAllKeys(BundleArray array) {
        array.put(1);
        array.put(5f);
        array.put(5L);
        array.put("5");
        BundleArray otherArray = new BundleArray();
        array.put(5);
        array.put(otherArray);
        BundleObject otherObject = new BundleObject();
        otherObject.put("key", 5);
        array.put(otherObject);
    }
}
