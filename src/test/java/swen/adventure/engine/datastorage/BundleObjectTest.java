package swen.adventure.engine.datastorage;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;
import static swen.adventure.TestUtilities.createTestFile;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 16/09/15.
 */
public class BundleObjectTest {

    private final BundleObject bundle = new BundleObject();

    @Test
    public void testValidPutInt() throws Exception {
        bundle.put("name", 4);
        assertEquals(4, bundle.getInt("name"));
    }

    @Test
    public void testValidPutString() throws Exception {
        bundle.put("name", "string");
        assertEquals("string", bundle.getString("name"));
    }

    @Test
    public void testValidPutLong() throws Exception {
        bundle.put("name", 5L);
        assertEquals(5L, bundle.getLong("name"));
    }

    @Test
    public void testValidPutFloat() throws Exception {
        bundle.put("name", 5f);
        assertEquals(5f, bundle.getFloat("name"), 0);
    }

    @Test
    public void testValidPutBundleArray() throws Exception {
        BundleArray array = new BundleArray();
        bundle.put("name", array);
        assertEquals(array, bundle.getBundleArray("name"));
    }

    @Test
    public void testValidPutBundleObject() throws Exception {
        BundleObject object = new BundleObject();
        object.put("test0", 4);
        bundle.put("name", object);
        assertEquals(object, bundle.getBundleObject("name"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidGet() throws Exception {
        bundle.getInt("name");
    }

    @Test(expected = ClassCastException.class)
    public void testGetWrongType() throws Exception {
        bundle.put("int key", 5);
        bundle.put("string key", "this is a string");
        bundle.getString("int key");
    }

    @Test
    public void testHasProperty() throws Exception {
        assertFalse(bundle.hasProperty("name"));
        bundle.put("name", 4);
        assertTrue(bundle.hasProperty("name"));
    }

    @Test
    public void testEqualsReflexive() throws Exception {
        testEquals(bundle, bundle);
        bundle.put("key", 5);
        testEquals(bundle, bundle);
    }

    @Test
    public void testEqualsTransitive() throws Exception {
        BundleObject bundleObject1 = new BundleObject();
        BundleObject bundleObject2 = new BundleObject();
        BundleObject bundleObject3 = new BundleObject();

        putAllKeys(bundleObject1);
        putAllKeys(bundleObject2);
        putAllKeys(bundleObject3);

        testEquals(bundleObject1, bundleObject2);
        testEquals(bundleObject2, bundleObject3);
        testEquals(bundleObject1, bundleObject3);
    }

    @Test
    public void testEqualsSymmetric() throws Exception {
        BundleObject bundleObject1 = new BundleObject();
        BundleObject bundleObject2 = new BundleObject();

        testEquals(bundleObject1, bundleObject2);

        putAllKeys(bundleObject1);
        putAllKeys(bundleObject2);

        testEquals(bundleObject1, bundleObject2);
    }

    @Test
    public void testEqualsNotEquals() throws Exception {
        bundle.put("name", 5);
        BundleObject other = new BundleObject();
        assertNotEquals(bundle, other);
    }

    @Test
    public void testSavingToDisk() throws Exception {
        BundleObject bundle = new BundleObject();
        bundle
                .put("playerName", "Daniel")
                .put("age", 2)
                .put("id", "747fc88");

        BundleSerializer serializer = new BundleSerializer();
        File file = createTestFile("testSavingToDisk");
        serializer.toXmlFile(bundle, file);
        BundleObject loaded = serializer.fromXml(file);
        assertEquals(bundle, loaded);
    }

    private static void testEquals(BundleObject bundleObject1, BundleObject bundleObject2) throws Exception {
        assertEquals(bundleObject1, bundleObject2);
        assertEquals(bundleObject1.hashCode(), bundleObject2.hashCode());
    }

    private static void putAllKeys(BundleObject bundle) {
        BundleArray array = new BundleArray();
        array.put(5);

        BundleObject otherObject = new BundleObject();
        otherObject.put("key", 5);

        bundle
                .put("key1", 1)
                .put("key2", 5f)
                .put("key3", 5L)
                .put("key4", "5")
                .put("key5", array)
                .put("key6", otherObject);
    }

}
