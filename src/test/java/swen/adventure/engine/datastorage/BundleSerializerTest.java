package swen.adventure.engine.datastorage;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 18/09/15.
 */
public class BundleSerializerTest {

    private static final String XML_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";

    @Test
    public void testBundleObjectToXml(){
        BundleSerializer dataMan = new BundleSerializer();
        String xml = dataMan.toXml(createDummyBundleObject());

        isXmlCorrect(
                "<BundleObject><property name=\"id\"><value type=\"java.lang.Integer\">25</value></property></BundleObject>",
                xml);
    }

    @Test
    public void testNestedBundleObjectToXml1(){
        BundleSerializer dataMan = new BundleSerializer();

        BundleObject bundleObject1 = createDummyBundleObject();
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

        BundleObject bundleObject1 = createDummyBundleObject();
        BundleObject bundleObject2 = new BundleObject();
        bundleObject2.put("key", 3f);
        bundleObject1.put("nest", bundleObject2);

        String xml = dataMan.toXml(bundleObject1);

        System.out.println(xml);

        assertEquals(bundleObject1, dataMan.fromXml(xml));
    }

    @Test
    public void testSimpleXmlToBundleObject(){
        BundleSerializer dataMan = new BundleSerializer();
        BundleObject bundleObject = dataMan.fromXml("<BundleObject><property name=\"id\"><value type=\"java.lang.Integer\">25</value></property></BundleObject>");

        assertEquals(createDummyBundleObject(), bundleObject);
    }

    private static BundleObject createDummyBundleObject(){
        BundleObject bundleObject = new BundleObject();
        bundleObject.put("id", 25);
        return bundleObject;
    }

    private static void isXmlCorrect(String expectedBody, String xml){
        String expected = XML_HEAD + expectedBody;
        assertEquals(expected, xml);
    }
}
