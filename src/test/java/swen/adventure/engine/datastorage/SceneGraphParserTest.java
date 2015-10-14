/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Liam O'Niell (oneilliam) (300312734) */ 
 /* Liam O'Niell (oneilliam) (300312734) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 package swen.adventure.engine.datastorage;

import org.junit.Test;
import swen.adventure.engine.Utilities;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.SceneNode;
import swen.adventure.engine.scenegraph.TransformNode;
import swen.adventure.game.scenenodes.Container;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNoException;

/**
 * Created by liam on 23/09/15.
 */
public class SceneGraphParserTest {

    private static final String DIRECTORY = "src/test/resources/swen/adventure/engine/datastorage/";

    @Test
    public void testSingleTransformNode() throws Exception {
        testAgainstFile("testSingleTransformNode", createRoot());
    }

    @Test
    public void testNestedTransformNodes() throws Exception {
        TransformNode root = createRoot();
        new TransformNode(
                "id2",
                root,
                false,
                new Vector3(36.646f, 383.0f, -263.7f),
                new Quaternion(-1.0f, 2.0f, 6.5f, -4.3f),
                new Vector3(74.2f, 5736.3f, 3623.2f)
        );

        testAgainstFile("testNestedTransformNodes", root);
    }

    @Test
    public void testGameObjectInTransformNode() {
        TransformNode root = createRoot();
        new GameObject("id2", root);

        testAgainstFile("testNestedTransformNodes", root);
    }

    @Test public void
    should_be_able_to_parse_container() {
        TransformNode root = createRoot();
        new Container("container1", root);

        testAgainstFile("should_be_able_to_parse_container", root);
    }

    private static TransformNode createRoot() {
        return new TransformNode(
                "root",
                Vector3.zero,
                new Quaternion(),
                Vector3.one
        );
    }

    private static void testAgainstFile(String fileName, SceneNode expected) {
        String graphXml = readFile(fileName);
        try {
            SceneNode result = SceneGraphParser.parseSceneGraph(graphXml);
            assertEquals(expected, result);
        } catch (ParserException e) {
            fail();
        }
    }

    private static String readFile(String fileName) {
        try {
            return Utilities.readFile(DIRECTORY + fileName + ".xml");
        } catch (IOException e) {
            assumeNoException(e);
            return null; // dead code
        }
    }
}