package swen.adventure.engine.datastorage;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import swen.adventure.engine.Utilities;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.SceneNode;
import swen.adventure.engine.scenegraph.TransformNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 22/09/15.
 */
public class SceneGraphParser {

    private static final String TRANSFORM_NODE_TAG = "TransformNode";
    private static final String GAME_OBJECT_TAG = "GameObject";
    private static final ParserManager PARSER_MANAGER = new ParserManager();

    public SceneNode parseSceneGraph(String input){
        InputStream is = Utilities.stringToInputStream(input);
        return parseSceneNode(is);
    }

    public SceneNode parseSceneGraph(File inputFile) throws FileNotFoundException {
        InputStream is = Utilities.fileToInputStream(inputFile);
        return parseSceneNode(is);
    }

    private SceneNode parseSceneNode(InputStream is){
        Document doc = Utilities.loadExistingXmlDocument(is);
        Node node = doc.getFirstChild();
        String name = node.getNodeName();
        if(!name.equals(TRANSFORM_NODE_TAG)) {
            fail("Unrecognised node: " + name);
        }

        return parseNode(node, Optional.empty());
    }

    private static SceneNode parseNode(Node xmlNode, Optional<TransformNode> parent) {
        String name = xmlNode.getNodeName();
        switch (name) {
            case TRANSFORM_NODE_TAG:
                return parseTransformNode(xmlNode, parent);
            case GAME_OBJECT_TAG:
                if (!parent.isPresent())
                    fail("A GameObject should always have a transform node as a parent.");

                return parseGameObject(xmlNode, parent.get());
            default:
                fail("Unrecognised node: " + name);
                break;
        }

        return null; // dead code
    }

    private static SceneNode parseGameObject(Node xmlNode, TransformNode parent) {
        String id = getAttribute("id", xmlNode, Function.identity());
        return new GameObject(id, parent);
    }

    private static TransformNode parseTransformNode(Node xmlNode, Optional<TransformNode> parent) {
        String id = getAttribute("id", xmlNode, Function.identity());
        Vector3 translation = getAttribute("translation", xmlNode, PARSER_MANAGER.getFromStringFunction(Vector3.class), Vector3.zero);
        Quaternion rotation = getAttribute("rotation", xmlNode, PARSER_MANAGER.getFromStringFunction(Quaternion.class), new Quaternion());
        Vector3 scale = getAttribute("scale", xmlNode, PARSER_MANAGER.getFromStringFunction(Vector3.class), Vector3.one);

        TransformNode node;
        if(!parent.isPresent())
            node = new TransformNode(id, translation, rotation, scale);
        else {
            boolean isDynamic = getAttribute("isDynamic", xmlNode, PARSER_MANAGER.getFromStringFunction(Boolean.class), false);
            node = new TransformNode(id, parent.get(), isDynamic, translation, rotation, scale);
        }

        // now parse any children
        NodeList children = xmlNode.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String value = child.getNodeValue();
            if(value == null || value.trim().equals(""))
                continue;
            
            parseNode(child, Optional.of(node));
        }

        return node;
    }

    private static void fail(String message) throws RuntimeException {
        throw new RuntimeException(message);
    }

    private static <T> T getAttribute(String name, Node node, Function<String, T> converter, T defaultValue) {
        NamedNodeMap attributes = node.getAttributes();
        Node valueNode = attributes.getNamedItem(name);
        if (valueNode == null) {
            if (defaultValue == null) {
                fail("Node " + node + " has no attribute for name " + name);
            }
            return defaultValue;
        }
        String value = valueNode.getTextContent();
        return converter.apply(value);
    }

    private static <T> T getAttribute(String name, Node node, Function<String, T> converter) {
       return getAttribute(name, node, converter, null);
    }
}
