package swen.adventure.engine.datastorage;

import org.junit.Test;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.SceneNode;
import swen.adventure.engine.scenegraph.TransformNode;

import static org.junit.Assert.assertEquals;

/**
 * Created by liam on 6/10/15.
 */
public class SceneGraphSerializerTest {

    private static final String XML_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n";

    @Test public void
    should_serialize_root() {
        assertXmlEqual("<root/>", createRoot());
    }

    @Test public void
    should_serialize_root_and_transform_node() {
        TransformNode root = createRoot();
        createChildTransformNode(root);
        assertXmlEqual(
                "<root>\n" +
                "<TransformNode id=\"id-37636\" isDynamic=\"true\" rotation=\"4.0, 0.4, 0.1, 1.0\" scale=\"1.0, 1.0, 1.0\" translation=\"2.0, 3.0, 4.0\"/>\n" +
                "</root>",
                root);
    }

    private static TransformNode createRoot(){
        return new TransformNode("root", Vector3.zero, new Quaternion(), Vector3.one);
    }

    private static TransformNode createChildTransformNode(TransformNode parent) {
        return new TransformNode("id-37636", parent, true, new Vector3(2f,3f,4f), new Quaternion(4f, 0.4f, 0.1f, 1f),
                new Vector3(1f, 1f, 1f));
    }

    private static String serialize(SceneNode root) {
        return SceneGraphSerializer.serializeToString(root);
    }

    private static void assertXmlEqual(String expected, SceneNode root) {
        String expectedCleaned = cleanXml(expected);
        String actualCleaned = cleanXml(serialize(root));
        assertEquals(expectedCleaned, actualCleaned);
    }

    private static void assertXmlEqual(String expected, String actual) {
        String expectedCleaned = cleanXml(expected);
        String actualCleaned = cleanXml(actual);
        assertEquals(expectedCleaned, actualCleaned);
    }

    private static String cleanXml(String xml) {
        if(xml.startsWith(XML_HEAD))
            return xml.substring(XML_HEAD.length()).trim();
        else
            return xml.trim();
    }
}
