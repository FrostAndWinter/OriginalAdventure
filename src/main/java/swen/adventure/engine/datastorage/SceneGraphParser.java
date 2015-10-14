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
import swen.adventure.game.scenenodes.*;
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
 * These files are generally used to describe the level, but can also describe the saved state.
 */
public class SceneGraphParser {

    private static final String TRANSFORM_NODE_TAG = "TransformNode";
    private static final String GAME_OBJECT_TAG = "GameObject";
    private static final String MESH_NODE_TAG = "MeshNode";
    private static final String REGION_TAG = "Region";
    private static final String AMBIENT_LIGHT_TAG = "AmbientLight";
    private static final String DIRECTIONAL_LIGHT_TAG = "DirectionalLight";
    private static final String POINT_LIGHT_TAG = "PointLight";
    private static final String FLICKERING_LIGHT_TAG = "FlickeringLight";
    private static final String CAMERA_TAG = "Camera";
    private static final String PLAYER_TAG = "Player";
    private static final String PUZZLE_TAG = "Puzzle";
    private static final String CONTAINER_TAG = "Container";
    private static final String LEVER_TAG = "Lever";
    private static final String DOOR_TAG = "Door";
    private static final String INVENTORY_TAG = "Inventory";

    /**
     * Parse a graph which is stored in xml format from a input string.
     *
     * @param input xml representation of the scene graph
     * @return root of the graph
     */
    public static TransformNode parseSceneGraph(String input) throws ParserException {
        InputStream is = Utilities.stringToInputStream(input);
        return parseSceneNode(is);
    }

    /**
     * Parse a graph which is stored in xml format from a input string.
     *
     * Applies changes to the existing scene graph.
     *
     * @param existingGraph The existing graph loaded from a level file.
     * @param input xml representation of the scene graph
     * @return root of the graph
     */
    public static TransformNode parseSceneGraph(String input, TransformNode existingGraph) throws ParserException {
        InputStream is = Utilities.stringToInputStream(input);
        return parseSceneNode(is, existingGraph);
    }

    /**
     * Parse a graph which is stored in xml format from a input string.
     *
     * @param inputFile file containing a xml representation of the scene graph.
     * @throws FileNotFoundException if the file doesn't exist.
     * @return root of the graph.
     */
    public static TransformNode parseSceneGraph(File inputFile) throws FileNotFoundException, ParserException {
        InputStream is = Utilities.fileToInputStream(inputFile);
        return parseSceneNode(is);
    }

    /**
     * Parse a graph which is stored in xml format from a input string.
     * Applies changes to the existing scene graph.
     *
     * @param existingGraph The existing graph loaded from a level file.
     * @param inputFile file containing a xml representation of the scene graph.
     * @throws FileNotFoundException if the file doesn't exist.
     * @return root of the graph.
     */
    public static TransformNode parseSceneGraph(File inputFile, TransformNode existingGraph) throws FileNotFoundException, ParserException {
        InputStream is = Utilities.fileToInputStream(inputFile);
        return parseSceneNode(is, existingGraph);
    }

    /**
     * Parse a graph which from a input stream.
     *
     * @param inputStream stream containing a xml representation of the scene graph.
     * @return root of the graph.
     */
    private static TransformNode parseSceneNode(InputStream inputStream) throws ParserException {
        TransformNode rootNode = new TransformNode("root", Vector3.zero, new Quaternion(), Vector3.one); //All scene graphs start with an identity root node.
        return SceneGraphParser.parseSceneNode(inputStream, rootNode);
    }

    /**
     * Parse a scene graph and set its parent to the given graph
     *
     * @param is input stream constraining a xml representation of the scene graph.
     * @param graph graph which will parent the parsed graph.
     * @return the root of the new graph.
     */
    private static TransformNode parseSceneNode(InputStream is, TransformNode graph) throws ParserException {
        try {
            Document doc = Utilities.loadExistingXmlDocument(is);

            NodeList nodes = doc.getFirstChild().getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                parseNode(nodes.item(i), graph);
            }

            return graph;
        } catch (Throwable t) {
            throw new ParserException("Error while parsing the scene graph.", t);
        }
    }

    /**
     * Construct a scene node by from the information in the given xml node.
     *
     * @param xmlNode node containing all the information for a node
     * @param parent parent of this node (as given in the xml structure)
     * @return the newly constructed scene node
     */
    private static SceneNode parseNode(Node xmlNode, TransformNode parent) {
        String name = xmlNode.getNodeName();
        switch (name) {

            // Note parseTransformNode will recursively parse all children under it.
            case TRANSFORM_NODE_TAG:
                return parseTransformNode(xmlNode, parent);

            // All other nodes are leafs in the graph.
            case GAME_OBJECT_TAG:
                return parseGameObject(xmlNode, parent, SceneNode.class);
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
            case CONTAINER_TAG:
                return parseContainer(xmlNode, parent);
            case LEVER_TAG:
                return parseLever(xmlNode, parent);
            case DOOR_TAG:
                return parseDoor(xmlNode, parent);
            case INVENTORY_TAG:
                return parseInventory(xmlNode, parent);
            case REGION_TAG:
                return parseRegion(xmlNode, parent);
            default:
                return parseGameObject(xmlNode, parent, SceneNode.class);
        }
    }

    /**
     * Construct a inventory node from its xml representation.
     *
     * @param xmlNode node from the xml document which represents a inventory node.
     * @param parent the transform node which will be set as the newly constructed node's parent.
     * @return a newly constructed inventory node with the same state as represented in the xml node.
     */
    private static Region parseRegion(Node xmlNode, TransformNode parent) {
        String id = getAttribute("regionName", xmlNode);

        Region region = parent.findNodeWithIdOrCreate(id, () -> {
            BoundingBox boundingBox = getAttribute("boundingBox", xmlNode, BoundingBox.class);
            return new Region(id, boundingBox, parent);
        });
        return region;
    }

    /**
     * Construct a inventory node from its xml representation.
     *
     * @param xmlNode node from the xml document which represents a inventory node.
     * @param parent the transform node which will be set as the newly constructed node's parent.
     * @return a newly constructed inventory node with the same state as represented in the xml node.
     */
    private static Inventory parseInventory(Node xmlNode, TransformNode parent) {
        String id = getAttribute("id", xmlNode);
        int selectedSlot = getAttribute("selectedSlot", xmlNode, Integer.class);
        Inventory inventory = new Inventory(id, parent);
        inventory.selectSlot(selectedSlot);
        return inventory;
    }

    /**
     * Construct a lever node from its xml representation.
     *
     * @param xmlNode node from the xml document which represents a lever node.
     * @param parent the transform node which will be set as the newly constructed node's parent.
     * @return a newly constructed lever node with the same state as represented in the xml node.
     */
    private static SceneNode parseLever(Node xmlNode, TransformNode parent) {
        Lever lever = parseGameObject(xmlNode, parent, Lever.class);
        boolean isDown = getAttribute("isDown", xmlNode, Boolean.class, false);
        lever.setIsDown(isDown);
        return lever;
    }

    /**
     * Construct a door node from its xml representation.
     *
     * @param xmlNode node from the xml document which represents a door node.
     * @param parent the transform node which will be set as the newly constructed node's parent.
     * @return a newly constructed door node with the same state as represented in the xml node.
     */
    private static SceneNode parseDoor(Node xmlNode, TransformNode parent) {
        Door door = parseGameObject(xmlNode, parent, Door.class);
        boolean isOpen = getAttribute("isOpen", xmlNode, Boolean.class, false);
        boolean requiresKey = getAttribute("requiresKey", xmlNode, Boolean.class, false);
        boolean canDirectlyInteractWith = getAttribute("canDirectlyInteractWith", xmlNode, Boolean.class, true);
        door.setIsOpen(isOpen);
        door.setRequiresKey(requiresKey);
        door.setCanDirectlyInteractWith(canDirectlyInteractWith);
        return door;
    }

    /**
     * Construct a game object node from its xml representation.
     *
     * @param xmlNode node from the xml document which represents a game object node.
     * @param parent the transform node which will be set as the newly constructed node's parent.
     * @return a newly constructed game object node with the same state as represented in the xml node.
     */
    private static <T extends SceneNode> T parseGameObject(Node xmlNode, TransformNode parent, Class<T> class0) {
        try {
            Class<?> gameObjectClass = Class.forName("swen.adventure.game.scenenodes." + xmlNode.getNodeName());
            Constructor<?> constructor = gameObjectClass.getConstructor(String.class, TransformNode.class);

            String id = getAttribute("id", xmlNode, Function.identity());
            boolean enabled = getAttribute("enabled", xmlNode, Boolean::valueOf, true);

            GameObject gameObject = (GameObject)parent.findNodeWithIdOrCreate(id, () -> {
                try {
                    return (SceneNode)constructor.newInstance(id, parent);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Error constructing Game Object with id " + id + ": " + e);
                }
            });

            gameObject.setEnabled(enabled);
            gameObject.setParent(parent);

            if (gameObject instanceof Item) {
                Item item = (Item) gameObject;

                getOptionalAttribute("inContainer", xmlNode)
                        .flatMap(parent::nodeWithID)
                        .map(Container.class::cast)
                        .ifPresent(item::moveToContainer);

                getOptionalAttribute("description", xmlNode)
                        .ifPresent(item::setDescription);

            } else if (gameObject instanceof Door) {
                boolean requiresKey = getAttribute("requiresKey", xmlNode, Boolean::valueOf, false);
                ((Door) gameObject).setRequiresKey(requiresKey);
            }

            if (gameObject instanceof AdventureGameObject) {
                getOptionalAttribute("container", xmlNode)
                        .flatMap(parent::nodeWithID)
                        .map(Container.class::cast)
                        .ifPresent(container -> {
                            AdventureGameObject adventureGameObject = (AdventureGameObject) gameObject;
                            adventureGameObject.setContainer(container);
                        });
            }

            return class0.cast(gameObject);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            return null;
            //throw new RuntimeException("Game object doesn't have default constructor: " + xmlNode);
        }
    }

    /**
     * Construct a mesh node from its xml representation.
     *
     * @param xmlNode node from the xml document which represents a mesh node.
     * @param parent the transform node which will be set as the newly constructed node's parent.
     * @return a newly constructed mesh node with the same state as represented in the xml node.
     */
    private static MeshNode parseMeshNode(Node xmlNode, TransformNode parent) {
        String id = getAttribute("id", xmlNode, Function.identity());
        boolean isCollidable = getAttribute("isCollidable", xmlNode, Boolean::parseBoolean, false);

        Optional<String> fileName = getOptionalAttribute("fileName", xmlNode);
        MeshNode node = parent.findNodeWithIdOrCreate(id, () -> {
            if (fileName.isPresent()) { //if it's not present, we know that the mesh will be loaded later dynamically (e.g. for a player).
                String directory = getAttribute("directory", xmlNode, Function.identity(), "");
                Vector3 textureRepeat = getAttribute("textureRepeat", xmlNode, ParserManager.getFromStringFunction(Vector3.class), Vector3.one);
                Optional<String> materialDirectory = getOptionalAttribute("materialDirectory", xmlNode);
                Optional<String> materialFileName = getOptionalAttribute("materialFileName", xmlNode);
                Optional<String> materialName = getOptionalAttribute("materialName", xmlNode);
                MeshNode retVal = new MeshNode(id, directory, fileName.get(), parent);
                retVal.setTextureRepeat(textureRepeat);

                materialFileName.ifPresent(matFileName ->
                        materialName.ifPresent(matName -> {
                            String matDirectory = materialDirectory.orElse("");
                            retVal.setMaterialOverride(MaterialLibrary.libraryWithName(matDirectory, matFileName).materialWithName(matName));
                        }));

                return retVal;
            } else {
                return null;
            }

        });

        if (node != null) {
            node.setCollidable(isCollidable);
            node.setParent(parent);
        }

        return node;
    }

    /**
     * Construct a puzzle node from its xml representation.
     *
     * @param xmlNode node from the xml document which represents a puzzle node.
     * @param parent the transform node which will be set as the newly constructed node's parent.
     * @return a newly constructed puzzle node with the same state as represented in the xml node.
     */
    public static Puzzle parsePuzzle(Node xmlNode, TransformNode parent) {

        String id = getAttribute("id", xmlNode, Function.identity());

        Puzzle puzzle = parent.findNodeWithIdOrCreate(id, () -> {
            String conditionsList = getAttribute("conditions", xmlNode, Function.identity());
            List<Puzzle.PuzzleCondition> conditions = PuzzleConditionParser.parseConditionList(conditionsList, parent);
            return new Puzzle(id, parent, conditions);
        });

        puzzle.setParent(parent);

        return puzzle;
    }

    /**
     * Construct a flickering light node from its xml representation.
     *
     * @param xmlNode node from the xml document which represents a flickering light node.
     * @param parent the transform node which will be set as the newly constructed node's parent.
     * @return a newly constructed flickering light node with the same state as represented in the xml node.
     */
    private static FlickeringLight parseFlickeringLightNode(Node xmlNode, TransformNode parent) {
        String id = getAttribute("id", xmlNode, Function.identity());

        FlickeringLight flickeringLight = parent.findNodeWithIdOrCreate(id, () -> {
            String directory = getAttribute("directory", xmlNode, Function.identity(), "");
            String fileName = getAttribute("fileName", xmlNode, Function.identity());

            Vector3 colour = getAttribute("colour", xmlNode, ParserManager.getFromStringFunction(Vector3.class), Vector3.one);
            float intensity = getAttribute("intensity", xmlNode, Float::parseFloat, 1.f);
            Light.LightFalloff falloff = getAttribute("falloff", xmlNode, Light.LightFalloff::fromString, Light.LightFalloff.Quadratic);
            return new FlickeringLight(id, parent, fileName, directory, colour, intensity, falloff);
        });


        boolean isOn = getAttribute("isOn", xmlNode, Boolean::valueOf, true);
        flickeringLight.setOn(isOn);

        float intensityVariation = getAttribute("intensityVariation", xmlNode, Float::parseFloat, 0.f);
        flickeringLight.setIntensityVariation(intensityVariation);

        flickeringLight.setParent(parent);

        return flickeringLight;
    }

    /**
     * Construct a camera node from its xml representation.
     *
     * @param xmlNode node from the xml document which represents a camera node.
     * @param parent the transform node which will be set as the newly constructed node's parent.
     * @return a newly constructed camera node with the same state as represented in the xml node.
     */
    private static CameraNode parseCameraNode(Node xmlNode, TransformNode parent) {
        String id = getAttribute("id", xmlNode, Function.identity());

        CameraNode cameraNode = parent.findNodeWithIdOrCreate(id, () ->
            new CameraNode(id, parent)
        );

        cameraNode.setParent(parent);

        return cameraNode;
    }

    /**
     * Construct a player node from its xml representation.
     *
     * @param xmlNode node from the xml document which represents a player node.
     * @param parent the transform node which will be set as the newly constructed node's parent.
     * @return a newly constructed player node with the same state as represented in the xml node.
     */
    private static Player parsePlayerNode(Node xmlNode, TransformNode parent) {
        String id = getAttribute("id", xmlNode, Function.identity());
        BoundingBox boundingBox = getAttribute("boundingBox", xmlNode, ParserManager.getFromStringFunction(BoundingBox.class), new BoundingBox(Vector3.zero, Vector3.zero));
        String colliderID = id + "Collider";

        Player player = parent.findNodeWithIdOrCreate(id, () -> new Player(id, parent));

        player.setParent(parent);

        CollisionNode collider = (CollisionNode)parent.nodeWithID(colliderID).orElseGet(() -> new CollisionNode(colliderID, parent, boundingBox, CollisionNode.CollisionFlag.Player));
        collider.setParent(parent);

        player.setCollisionNode(collider);

        return player;
    }

    /**
     * Construct a ambient light node from its xml representation.
     *
     * @param xmlNode node from the xml document which represents a ambient light node.
     * @param parent the transform node which will be set as the newly constructed node's parent.
     * @return a newly constructed ambient light node with the same state as represented in the xml node.
     */
    private static Light parseAmbientLight(Node xmlNode, TransformNode parent) {
        String id = getAttribute("id", xmlNode, Function.identity());
        Vector3 colour = getAttribute("colour", xmlNode, ParserManager.getFromStringFunction(Vector3.class), Vector3.one);
        float intensity = getAttribute("intensity", xmlNode, Float::parseFloat, 1.f);

        Light node = parent.findNodeWithIdOrCreate(id, () -> Light.createAmbientLight(id, parent, colour, intensity));

        node.setColour(colour);
        node.setIntensity(intensity);
        node.setParent(parent);

        return node;
    }

    /**
     * Construct a directional light node from its xml representation.
     *
     * @param xmlNode node from the xml document which represents a directional light node.
     * @param parent the transform node which will be set as the newly constructed node's parent.
     * @return a newly constructed directional light node with the same state as represented in the xml node.
     */
    private static Light parseDirectionalLight(Node xmlNode, TransformNode parent) {
        String id = getAttribute("id", xmlNode, Function.identity());
        Vector3 colour = getAttribute("colour", xmlNode, ParserManager.getFromStringFunction(Vector3.class), Vector3.one);
        float intensity = getAttribute("intensity", xmlNode, Float::parseFloat, 1.f);
        Vector3 fromDirection = getAttribute("fromDirection", xmlNode, ParserManager.getFromStringFunction(Vector3.class));

        Light node = parent.findNodeWithIdOrCreate(id, () -> Light.createDirectionalLight(id, parent, colour, intensity, fromDirection));

        node.setColour(colour);
        node.setIntensity(intensity);
        node.setParent(parent);

        return node;
    }

    /**
     * Construct a point light node from its xml representation.
     *
     * @param xmlNode node from the xml document which represents a point light node.
     * @param parent the transform node which will be set as the newly constructed node's parent.
     * @return a newly constructed point light node with the same state as represented in the xml node.
     */
    private static Light parsePointLight(Node xmlNode, TransformNode parent) {
        String id = getAttribute("id", xmlNode, Function.identity());
        Vector3 colour = getAttribute("colour", xmlNode, ParserManager.getFromStringFunction(Vector3.class), Vector3.one);
        float intensity = getAttribute("intensity", xmlNode, Float::parseFloat, 1.f);
        Light.LightFalloff falloff = getAttribute("falloff", xmlNode, Light.LightFalloff::fromString);

        Light node = parent.findNodeWithIdOrCreate(id, () -> Light.createPointLight(id, parent, colour, intensity, falloff));

        node.setColour(colour);
        node.setIntensity(intensity);
        node.setParent(parent);

        return node;
    }

    /**
     * Construct a transform node from its xml representation.
     *
     * @param xmlNode node from the xml document which represents a transform node.
     * @param parent the transform node which will be set as the newly constructed node's parent.
     * @return a newly constructed transform node with the same state as represented in the xml node.
     */
    private static TransformNode parseTransformNode(Node xmlNode, TransformNode parent) {
        String id = getAttribute("id", xmlNode, Function.identity());
        Vector3 translation = getAttribute("translation", xmlNode, ParserManager.getFromStringFunction(Vector3.class), Vector3.zero);
        Quaternion rotation = getAttribute("rotation", xmlNode, ParserManager.getFromStringFunction(Quaternion.class), new Quaternion());
        Vector3 scale = getAttribute("scale", xmlNode, ParserManager.getFromStringFunction(Vector3.class), Vector3.one);

        boolean isDynamic = getAttribute("isDynamic", xmlNode, ParserManager.getFromStringFunction(Boolean.class), false);

        TransformNode node = parent.findNodeWithIdOrCreate(id, () -> new TransformNode(id, parent, isDynamic, translation, rotation, scale));
        if (node.isDynamic()) {
            node.setTranslation(translation);
            node.setRotation(rotation);
            node.setScale(scale);
        }

        node.setParent(parent);

        // now parse any children
        NodeList children = xmlNode.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            parseNode(child, node);
        }

        return node;
    }

    /**
     * Construct a container node from its xml representation.
     *
     * @param xmlNode node from the xml document which represents a container node.
     * @param parent the transform node which will be set as the newly constructed node's parent.
     * @return a newly container camera node with the same state as represented in the xml node.
     */
    private static Container parseContainer(Node xmlNode, TransformNode parent) {
        String id = getAttribute("id", xmlNode, Function.identity());

        int capacity = getAttribute("capacity", xmlNode, ParserManager.getFromStringFunction(Integer.class), 10);
        boolean showTopItem = getAttribute("showTopItem", xmlNode, ParserManager.getFromStringFunction(Boolean.class), true);

        Container container = parent.findNodeWithIdOrCreate(id, () -> new Container(id, parent, capacity));
        container.setShowTopItem(showTopItem);

        return container;
    }

    /**
     * Signal that the parsing of this input has failed by throwing a unchecked exception with the given message
     *
     * @param message message to be given to the exception
     * @throws RuntimeException always throws a RuntimeException
     */
    private static void fail(String message) throws RuntimeException {
        throw new RuntimeException(message);
    }

    /**
     * Helper method for getting the value of a attribute and converting it with the given function or
     *      returning a default value in the case that the attribute doesn't exist.
     *
     * @param name the name of the attribute
     * @param node xml node to get the attribute from
     * @param converter function to convert the string value into some other type
     * @param defaultValue the value returned if the node doesn't have a attribute with the given name
     * @return the value of the converted attribute or the defaultValue if it doesn't exist
     */
    private static <T> T getAttribute(String name, Node node, Function<String, T> converter, T defaultValue) {
        NamedNodeMap attributes = node.getAttributes();
        Node valueNode = attributes.getNamedItem(name);
        if (valueNode == null) {
            if (defaultValue == null) {
                fail("Node " + attributes.getNamedItem("id") + " of type " + node + " has no attribute for name " + name);
            }
            return defaultValue;
        }
        String value = valueNode.getTextContent();
        return converter.apply(value);
    }

    /**
     * Helper method for getting the value of a attribute and converting it with the given function.
     *
     * @param name the name of the attribute
     * @param node xml node to get the attribute from
     * @param converter function to convert the string value into some other type
     * @return the value of the converted attribute
     */
    private static <T> T getAttribute(String name, Node node, Function<String, T> converter) {
       return getAttribute(name, node, converter, null);
    }

    /**
     * Helper method for getting the value of a attribute.
     *
     * @param name the name of the attribute
     * @param node xml node to get the attribute from
     * @return the value the attribute
     */
    private static String getAttribute(String name, Node node) {
        return getAttribute(name, node, Function.identity(), null);
    }

    /**
     * Helper method for getting the value of a attribute and converting it with the standard parser for the given type.
     *
     * @param name the name of the attribute
     * @param node xml node to get the attribute from
     * @param class0 the type of the desired value
     * @return the value of the converted attribute
     */
    private static <T> T getAttribute(String name, Node node, Class<T> class0) {
        return getAttribute(name, node, ParserManager.getFromStringFunction(class0), null);
    }

    /**
     * Helper method for getting the value of a attribute and converting it with the standard parser for the given type,
     *      or returns the given defaultValue if the attribute doesn't exist.
     *
     * @param name the name of the attribute
     * @param node xml node to get the attribute from
     * @param class0 the type of the desired value
     * @param defaultValue value to be returned if the attribute doesn't exist
     * @return the value of the converted attribute
     */
    private static <T> T getAttribute(String name, Node node, Class<T> class0, T defaultValue) {
        return getAttribute(name, node, ParserManager.getFromStringFunction(class0), defaultValue);
    }

    /**
     * Helper method for getting a optional of the value of a attribute and converting it with the standard parser for the given type,
     *      or if the the attribute doesn't exist it will return a empty optional
     *
     * @param name the name of the attribute
     * @param node xml node to get the attribute from
     * @param class0 the type of the desired value
     * @return optional of the value of the converted attribute
     */
    private static <T> Optional<T> getOptionalAttribute(String name, Node node, Class<T> class0) {
        Function<T, Optional<T>> toOptional = Optional::of;
        Function<String, T> fromString = ParserManager.getFromStringFunction(class0);
        Function<String, Optional<T>> convert =  toOptional.compose(fromString);
        return getAttribute(name, node, convert, Optional.empty());
    }

    /**
     * Helper method for getting a optional of the value of a attribute,
     *          or if the the attribute doesn't exist it will return a empty optional.
     *
     * @param name the name of the attribute
     * @param node xml node to get the attribute from
     * @return optional of the value of the attribute
     */
    private static Optional<String> getOptionalAttribute(String name, Node node) {
        return getOptionalAttribute(name, node, String.class);
    }
}
