package swen.adventure.engine.datastorage;

import org.junit.Test;

import java.io.File;

import static swen.adventure.TestUtilities.createTestFile;
import static org.junit.Assert.assertEquals;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 18/09/15.
 */
public class BundleSerializerTest {

    private static final String XML_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";

    @Test
    public void testBundleObjectToXml(){
        BundleSerializer dataMan = new BundleSerializer();
        String xml = dataMan.toXml(createBundleObject());

        isXmlCorrect(
                "<BundleObject><property name=\"id\"><value type=\"java.lang.Integer\">25</value></property></BundleObject>",
                xml);
    }

    @Test
    public void testNestedBundleObjectToXml1(){
        BundleSerializer dataMan = new BundleSerializer();

        BundleObject bundleObject1 = createBundleObject();
        BundleObject bundleObject2 = new BundleObject();
        bundleObject2.put("key", 3f);
        bundleObject1.put("nest", bundleObject2);

        String xml = dataMan.toXml(bundleObject1);

        isXmlCorrect(
                "<BundleObject><property name=\"id\"><value type=\"java.lang.Integer\">25</value></property>" +
                        "<property name=\"nest\"><value type=\"swen.adventure.engine.datastorage.BundleObject\">" +
                        "<BundleObject><property name=\"key\"><value type=\"java.lang.Float\">3.0</value></property>" +
                        "</BundleObject></value></property></BundleObject>",
                xml);
    }

    @Test
    public void testNestedBundleObjectToXml2(){
        BundleSerializer dataMan = new BundleSerializer();

        BundleObject bundleObject1 = createBundleObject();
        BundleObject bundleObject2 = new BundleObject();
        bundleObject2.put("key", 3f);
        bundleObject1.put("nest", bundleObject2);

        String xml = dataMan.toXml(bundleObject1);
        assertEquals(bundleObject1, dataMan.fromXml(xml));
    }

    @Test
    public void
    testSimpleXmlToBundleObject(){
        BundleSerializer dataMan = new BundleSerializer();
        BundleObject bundleObject = dataMan.fromXml("<BundleObject><property name=\"id\"><value type=\"java.lang.Integer\">25</value></property></BundleObject>");

        assertEquals(createBundleObject(), bundleObject);
    }

    @Test public void
    should_be_able_to_reload_serializable_object() throws Exception {
        BundleSerializer bundleSerializer = new BundleSerializer();
        BundleSerializable bundleSerializable = createSerializable();
        File savePoint = createTestFile("should_be_able_to_reload_serializable_object");

        // save the object
        bundleSerializer.toXmlFile(bundleSerializable, savePoint);

        // load the object
        BundleSerializable loadedBundleSerializable = bundleSerializer.loadObjectFromBundle(savePoint,
                TestBundleSerializable.class);

        assertEquals(bundleSerializable, loadedBundleSerializable);
    }

    private static BundleSerializable createSerializable() {
        return new TestBundleSerializable("room1", 23, 64);
    }

    private static BundleObject createBundleObject(){
        BundleObject bundleObject = new BundleObject();
        bundleObject.put("id", 25);
        return bundleObject;
    }

    private static void isXmlCorrect(String expectedBody, String xml){
        String expected = XML_HEAD + expectedBody;
        assertEquals(expected, xml);
    }

    private static class TestBundleSerializable implements BundleSerializable {
        private final String id;
        private final int x;
        private final int y;

        private TestBundleSerializable(String id, int x, int y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }

        @Override
        public BundleObject toBundle() {
            return new BundleObject()
                    .put("id", id)
                    .put("x", x)
                    .put("y", y);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;

            TestBundleSerializable that = (TestBundleSerializable) object;

            if (x != that.x) return false;
            if (y != that.y) return false;
            return !(id != null ? !id.equals(that.id) : that.id != null);

        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + x;
            result = 31 * result + y;
            return result;
        }

        private static TestBundleSerializable createFromBundle(BundleObject bundle) {
            String id = bundle.getString("id");
            int x = bundle.getInt("x");
            int y = bundle.getInt("y");
            return new TestBundleSerializable(id, x, y);
        }
    }
}
