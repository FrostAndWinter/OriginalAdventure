package swen.adventure.engine.datastorage;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import swen.adventure.engine.Utilities;
import swen.adventure.engine.rendering.maths.BoundingBox;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.*;
import swen.adventure.game.PuzzleConditionParser;
import swen.adventure.game.scenenodes.FlickeringLight;
import swen.adventure.game.scenenodes.Player;
import swen.adventure.engine.scenegraph.Puzzle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 22/09/15.
 * Modified by Thomas Roughton, Student ID 300313924.
 * Modified by Joseph Bennett.
 *
 * The SceneGraphParser can read from an XML file describing a scene graph and generate an internal memory representation of that graph.
 * These files are generally used to describe the level – i
 */
public class SceneGraphParser {

    private static final String TRANSFORM_NODE_TAG = "TransformNode";
    private static final String GAME_OBJECT_TAG = "GameObject";
    private static final String MESH_NODE_TAG = "MeshNode";
    private static final String AMBIENT_LIGHT_TAG = "AmbientLight";
    private static final String DIRECTIONAL_LIGHT_TAG = "DirectionalLight";
    private static final String POINT_LIGHT_TAG = "PointLight";
    private static final String FLICKERING_LIGHT_TAG = "FlickeringLight";
    private static final String CAMERA_TAG = "Camera";
    private static final String PLAYER_TAG = "Player";
    private static final String PUZZLE_TAG = "Puzzle";

    public static TransformNode parseSceneGraph(String input) {
        InputStream is = Utilities.stringToInputStream(input);
        return parseSceneNode(is);
    }

    public static TransformNode parseSceneGraph(File inputFile) throws FileNotFoundException {
        InputStream is = Utilities.fileToInputStream(inputFile);
        return parseSceneNode(is);
    }

    private static TransformNode parseSceneNode(InputStream is){
        Document doc = Utilities.loadExistingXmlDocument(is);
        TransformNode rootNode = new TransformNode("root", Vector3.zero, new Quaternion(), Vector3.one); //All scene graphs start with an identity root node.

        NodeList nodes = doc.getFirstChild().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            parseNode(nodes.item(i), rootNode);
        }

        return rootNode;
    }

    private static SceneNode parseNode(Node xmlNode, TransformNode parent) {
        String name = xmlNode.getNodeName();
        switch (name) {
            case TRANSFORM_NODE_TAG:
                return parseTransformNode(xmlNode, parent);
            case GAME_OBJECT_TAG:
                return parseGameObject(xmlNode, parent);
            case MESH_NODE_TAG:
                return parseMeshNode(xmlNode, parent);
            case AMBIENT_LIGHT_TAG:
                return parseAmbientLight(xmlNode, parent);
            case DIRECTIONAL_LIGHT_TAG:
                return parseDirectionalLight(xmlNode, parent);
            case POINT_LIGHT_TAG:
                return parsePointLight(xmlNode, parent);
            case CAMERA_TAG:
                return parseCameraNode(xmlNode, parent);
            case PLAYER_TAG:
                return parsePlayerNode(xmlNode, parent);
            case FLICKERING_LIGHT_TAG:
                return parseFlickeringLightNode(xmlNode, parent);
            case PUZZLE_TAG:
                return parsePuzzle(xmlNode, parent);
            default:
               // fail("Unrecognised node: " + name);
                return parseGameObject(xmlNode, parent);
        }
    }

    private static SceneNode parseGameObject(Node xmlNode, TransformNode parent) {
        try {
            Class<?> gameObjectClass = Class.forName("swen.adventure.game.scenenodes." + xmlNode.getNodeName());
            Constructor<?> constructor = gameObjectClass.getConstructor(String.class, TransformNode.class);

            String id = getAttribute("id", xmlNode, Function.identity());
            boolean enabled = getAttribute("enabled", xmlNode, Boolean::valueOf, true);

            GameObject gameObject = (GameObject)constructor.newInstance(id, parent);
            gameObject.setEnabled(enabled);
            return gameObject;
        } catch (ClassNotFoundException e) {
            return null;
        } catch (NoSuchMethodException|InvocationTargetException|InstantiationException|IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static MeshNode parseMeshNode(Node xmlNode, TransformNode parent) {
        String id = getAttribute("id", xmlNode, Function.identity());
        String directory = getAttribute("directory", xmlNode, Function.identity(), "");
        String fileName = getAttribute("fileName", xmlNode, Function.identity());
        Vector3 textureRepeat = getAttribute("textureRepeat", xmlNode, ParserManager.getFromStringFunction(Vector3.class), Vector3.one);
        boolean isCollidable = getAttribute("isCollidable", xmlNode, Boolean::parseBoolean, false);

        Optional<String> materialDirectory = (Optional<String>) getAttribute("materialDirectory", xmlNode, Optional::of, Optional.empty());
        Optional<String> materialFileName = (Optional<String>)getAttribute("materialFileName", xmlNode, Optional::of, Optional.empty());
        Optional<String> materialName = (Optional<String>)getAttribute("materialName", xmlNode, Optional::of, Optional.empty());

        MeshNode node = new MeshNode(id, directory, fileName, parent);
        node.setTextureRepeat(textureRepeat);
        node.setCollidable(isCollidable);

        materialFileName.ifPresent(matFileName ->
                materialName.ifPresent(matName -> {
                    String matDirectory = materialDirectory.orElse("");
                    node.setMaterialOverride(MaterialLibrary.libraryWithName(matDirectory, matFileName).materialWithName(matName));
                }));

        return node;
    }

    public static Puzzle parsePuzzle(Node xmlNode, TransformNode parent) {
        String id = getAttribute("id", xmlNode, Function.identity());
        String conditionsList = getAttribute("conditions", xmlNode, Function.identity());
        List<Puzzle.PuzzleCondition> conditions = PuzzleConditionParser.parseConditionList(conditionsList, parent);

        return new Puzzle(id, parent, conditions);
    }

    private static FlickeringLight parseFlickeringLightNode(Node xmlNode, TransformNode parent) {
        String id = getAttribute("id", xmlNode, Function.identity());
        String directory = getAttribute("directory", xmlNode, Function.identity(), "");
        String fileName = getAttribute("fileName", xmlNode, Function.identity());

        Vector3 colour = getAttribute("colour", xmlNode, ParserManager.getFromStringFunction(Vector3.class), Vector3.one);
        float intensity = getAttribute("intensity", xmlNode, Float::parseFloat, 1.f);
        Light.LightFalloff falloff = getAttribute("falloff", xmlNode, Light.LightFalloff::fromString, Light.LightFalloff.Quadratic);

        FlickeringLight flickeringLight = new FlickeringLight(id, parent, fileName, directory, colour, intensity, falloff);
        float intensityVariation = getAttribute("intensityVariation", xmlNode, Float::parseFloat, 0.f);
        flickeringLight.setIntensityVariation(intensityVariation);
        return flickeringLight;
    }

    private static CameraNode parseCameraNode(Node xmlNode, TransformNode parent) {
        String id = getAttribute("id", xmlNode, Function.identity());
        return new CameraNode(id, parent);
    }

    private static Player parsePlayerNode(Node xmlNode, TransformNode parent) {
        String id = getAttribute("id", xmlNode, Function.identity());
        BoundingBox boundingBox = getAttribute("boundingBox", xmlNode, ParserManager.getFromStringFunction(BoundingBox.class), new BoundingBox(Vector3.zero, Vector3.zero));
        Player player = new Player(id, parent);
        player.setCollisionNode(new CollisionNode(id + "Collider", parent, boundingBox));
        return player;
    }

    private static Light parseAmbientLight(Node xmlNode, TransformNode parent) {
        String id = getAttribute("id", xmlNode, Function.identity());
        Vector3 colour = getAttribute("colour", xmlNode, ParserManager.getFromStringFunction(Vector3.class), Vector3.one);
        float intensity = getAttribute("intensity", xmlNode, Float::parseFloat, 1.f);

        Light node = Light.createAmbientLight(id, parent, colour, intensity);

        return node;
    }

    private static Light parseDirectionalLight(Node xmlNode, TransformNode parent) {
        String id = getAttribute("id", xmlNode, Function.identity());
        Vector3 colour = getAttribute("colour", xmlNode, ParserManager.getFromStringFunction(Vector3.class), Vector3.one);
        float intensity = getAttribute("intensity", xmlNode, Float::parseFloat, 1.f);
        Vector3 fromDirection = getAttribute("fromDirection", xmlNode, ParserManager.getFromStringFunction(Vector3.class));

        Light node = Light.createDirectionalLight(id, parent, colour, intensity, fromDirection);

        return node;
    }

    private static Light parsePointLight(Node xmlNode, TransformNode parent) {
        String id = getAttribute("id", xmlNode, Function.identity());
        Vector3 colour = getAttribute("colour", xmlNode, ParserManager.getFromStringFunction(Vector3.class), Vector3.one);
        float intensity = getAttribute("intensity", xmlNode, Float::parseFloat, 1.f);
        Light.LightFalloff falloff = getAttribute("falloff", xmlNode, Light.LightFalloff::fromString);

        Light node = Light.createPointLight(id, parent, colour, intensity, falloff);

        return node;
    }


    private static TransformNode parseTransformNode(Node xmlNode, TransformNode parent) {
        String id = getAttribute("id", xmlNode, Function.identity());
        Vector3 translation = getAttribute("translation", xmlNode, ParserManager.getFromStringFunction(Vector3.class), Vector3.zero);
        Quaternion rotation = getAttribute("rotation", xmlNode, ParserManager.getFromStringFunction(Quaternion.class), new Quaternion());
        Vector3 scale = getAttribute("scale", xmlNode, ParserManager.getFromStringFunction(Vector3.class), Vector3.one);

        boolean isDynamic = getAttribute("isDynamic", xmlNode, ParserManager.getFromStringFunction(Boolean.class), false);
        TransformNode node = new TransformNode(id, parent, isDynamic, translation, rotation, scale);

        // now parse any children
        NodeList children = xmlNode.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            parseNode(child, node);
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
