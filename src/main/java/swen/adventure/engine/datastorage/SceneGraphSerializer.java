package swen.adventure.engine.datastorage;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import swen.adventure.engine.Utilities;
import swen.adventure.engine.rendering.maths.BoundingBox;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.*;
import swen.adventure.game.scenenodes.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 04/10/15.
 *
 * The SceneGraphSerializer can take a SceneGraph describing a scene graph and generate a xml representation of that graph.
 * These files are generally used to describe the level, but can also describe the saved state.
 */
public class SceneGraphSerializer {

    // xml document which will be mutated by saving serialized SceneNodes into it
    private final Document document = Utilities.createDocument();

    /**
     * Serialize SceneGraph into a file.
     * Note that the SceneNode passed in should be the root.
     *
     * @param root root of the of scene graph to serialize
     * @param file file to save the xml result in
     * @throws FileNotFoundException will be thrown if the file doesn't exist
     */
    public static void serializeToFile(SceneNode root, File file) throws FileNotFoundException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        serializeToStream(root, fileOutputStream);
    }

    /**
     * Serialize SceneGraph into a String.
     * Note that the SceneNode passed in should be the root.
     *
     * @param root root of the of scene graph to serialize
     * @return a xml string holding the serialized xml
     */
    public static String serializeToString(SceneNode root) {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        serializeToStream(root, arrayOutputStream);
        return new String(arrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * Serialize SceneGraph into a OutputSteam.
     * Note that the SceneNode passed in should be the root.
     *
     * @param root root of the of scene graph to serialize
     * @param outputStream stream to save the xml result in
     */
    public static void serializeToStream(SceneNode root, OutputStream outputStream) {
        new SceneGraphSerializer().start(root, outputStream);
    }

    /** Don't allow outside classes to create instances */
    private SceneGraphSerializer(){
    }

    /**
     * Start the serialization of the tree into the given output stream.
     * @param root the root of the scene graph to serialize
     * @param outputStream stream to save the resulting xml into
     */
    private void start(SceneNode root, OutputStream outputStream) {
        serializeSceneNode(root, document);
        Utilities.writeOutDocument(document, outputStream, true);
    }

    /**
     * Recursive method to serialize a node and its children.
     * NOTE: the root shouldn't be serialized by this method as it has no parent.
     * This method serializes the tree by doing depth first traversal.
     *
     * @param sceneNode node to serialize
     * @param xmlParentNode the xml node which belongs to this node's parent.
     */
    private void serializeSceneNode(SceneNode sceneNode, Node xmlParentNode) {
        Node serializedNode; // save the this node's xml node so it can be parsed in as the parent for its children.

        if (isRoot(sceneNode))
            serializedNode = serializeRoot(xmlParentNode);

        else if (sceneNode instanceof TransformNode)
            serializedNode = serializeTransformNode((TransformNode) sceneNode, xmlParentNode);

        else if (sceneNode instanceof MeshNode)
            serializedNode = serializeMeshNode((MeshNode) sceneNode, xmlParentNode);

        else if (isAmbientLight(sceneNode))
            serializedNode = serializeAmbientLightNode((Light) sceneNode, xmlParentNode);

        else if (isPointLight(sceneNode))
            serializedNode = serializePointLightNode((Light) sceneNode, xmlParentNode);

        else if (isDirectionalLight(sceneNode))
            serializedNode = serializeDirectionalLightNode((Light) sceneNode, xmlParentNode);

        else if (isFlickeringLight(sceneNode))
            serializedNode = serializeFlickeringLightNode((FlickeringLight) sceneNode, xmlParentNode);

        else if (sceneNode instanceof CameraNode)
            serializedNode = serializeCameraNode((CameraNode) sceneNode, xmlParentNode);

        else if (sceneNode instanceof Player)
            serializedNode = serializePlayerNode((Player) sceneNode, xmlParentNode);

        else if (sceneNode instanceof Lever)
            serializedNode = serializeLeverNode((Lever) sceneNode, xmlParentNode);

        else if (isStrictInstanceOf(sceneNode, Container.class))
            serializedNode = serializeContainerNode((Container) sceneNode, xmlParentNode);

        else if (isStrictInstanceOf(sceneNode, Item.class))
            serializedNode = serializeItemNode((Item) sceneNode, xmlParentNode);

        else if (isStrictInstanceOf(sceneNode, Inventory.class))
            serializedNode = serializedInventoryNode((Inventory) sceneNode, xmlParentNode);

        else
            throw new RuntimeException("Don't recognise node " + sceneNode);

        sceneNode.children()
                .forEach(node -> serializeSceneNode(node, serializedNode));
    }

    /**
     * Serialize inventory nodes.
     *
     * @param sceneNode node to serialize
     * @param xmlParentNode xml node corresponding to this node's parent
     * @return xml node corresponding to this SceneGraph node
     */
    private Node serializedInventoryNode(Inventory sceneNode, Node xmlParentNode) {
        Element xmlElement = createElementForNode(sceneNode, xmlParentNode);
        setAttribute("selectedSlot", sceneNode.selectedSlot(), Integer.class, xmlElement);
        return xmlElement;
    }

    /**
     * Serialize container nodes.
     *
     * @param containerNode node to serialize
     * @param xmlParentNode xml node corresponding to this node's parent
     * @return xml node corresponding to this SceneGraph node
     */
    private Node serializeContainerNode(Container containerNode, Node xmlParentNode) {
        Element xmlElement = createElementForNode(containerNode, xmlParentNode);
        setAttribute("capacity", containerNode.capacity(), Integer.class, xmlElement);
        setAttribute("showTopItem", containerNode.getShowTopItem(), Boolean.class, xmlElement);
        return xmlElement;
    }

    /**
     * Serialize item nodes.
     *
     * @param itemNode node to serialize
     * @param xmlParentNode xml node corresponding to this node's parent
     * @return xml node corresponding to this SceneGraph node
     */
    private Node serializeItemNode(Item itemNode, Node xmlParentNode) {
        Element xmlElement = createElementForNode(itemNode, xmlParentNode);

        itemNode.containingContainer()
                .map(container -> container.id)
                .ifPresent(id -> setAttribute("inContainer", id, xmlElement)
        );

        itemNode.description
                .ifPresent(description -> setAttribute("description", description, xmlElement));

        return xmlElement;
    }

    /**
     * Returns the result of whether the obj is a strict instance of the given class.
     * A strict instance is when {@code obj.getClass() == class0 }.
     *
     * @param obj instance to test on
     * @param class0 class object for that type
     * @return true iff the object's class equals the passed class
     */
    private boolean isStrictInstanceOf(Object obj, Class<?> class0) {
        return obj.getClass() == class0;
    }

    /**
     * Serialize a lever node.
     *
     * @param leverNode node to serialize
     * @param xmlParentNode xml node corresponding to this node's parent
     * @return xml node corresponding to this SceneGraph node
     */
    private Node serializeLeverNode(Lever leverNode, Node xmlParentNode) {
        Element xmlElement = createElementForNode(leverNode, xmlParentNode);
        setAttribute("isDown", leverNode.isDown(), Boolean.class, xmlElement);
        return xmlElement;
    }

    /**
     * Returns the result of whether this sceneNode is the root of a the scene graph.
     * The root is determined by whether the its node id is equal to {@code "root"}.
     *
     * @param sceneNode node to test
     * @return true iff this node is the root.
     */
    private boolean isRoot(SceneNode sceneNode) {
        return sceneNode instanceof TransformNode && sceneNode.id.equals("root");
    }

    /**
     * Returns the result of whether this sceneNode is the root of a the scene graph.
     * The root is determined by whether the its node id is equal to  {@code "root"}.
     *
     * @param sceneNode node to test
     * @return true iff this node is the root.
     */
    private boolean isAmbientLight(SceneNode sceneNode) {
        return sceneNode instanceof Light && ((Light) sceneNode).getType() == Light.LightType.Ambient;
    }

    /**
     * Results the result of whether this sceneNode is a point light.
     *
     * @param sceneNode node to test
     * @return true iff this node is a point light
     */
    private boolean isPointLight(SceneNode sceneNode) {
        return sceneNode instanceof Light && ((Light) sceneNode).getType() == Light.LightType.Point;
    }

    /**
     * Results the result of whether this sceneNode is a directional light.
     *
     * @param sceneNode node to test
     * @return true iff this node is a directional light
     */
    private boolean isDirectionalLight(SceneNode sceneNode) {
        return sceneNode instanceof Light && ((Light) sceneNode).getType() == Light.LightType.Directional;
    }

    /**
     * Results the result of whether this sceneNode is a flickering light.
     *
     * @param sceneNode node to test
     * @return true iff this node is a flickering light
     */
    private boolean isFlickeringLight(SceneNode sceneNode) {
        return sceneNode instanceof FlickeringLight;
    }

    /**
     * Serialize the root node.
     *
     * @param xmlParentNode the xml node of the parent (in this case it will be the document itself)
     * @return the xml node corresponding to this root
     */
    private Node serializeRoot(Node xmlParentNode) {
        return createElement("root", xmlParentNode);
    }

    /**
     * Serialize a transform node.
     *
     * @param transformNode node to serialize
     * @param xmlParentNode xml node corresponding to this node's parent
     * @return xml node corresponding to this SceneGraph node
     */
    private Node serializeTransformNode(TransformNode transformNode, Node xmlParentNode) {
        Element xmlElement = createElementForNode(transformNode, xmlParentNode);
        setAttribute("translation", transformNode.translation(), Vector3.class, xmlElement);
        setAttribute("rotation", transformNode.rotation(), Quaternion.class, xmlElement);
        setAttribute("scale", transformNode.scale(), Vector3.class, xmlElement);
        setAttribute("isDynamic", transformNode.isDynamic(), Boolean.class, xmlElement);
        return xmlElement;
    }

    /**
     * Serialize a mesh node.
     *
     * @param meshNode node to serialize
     * @param xmlParentNode xml node corresponding to this node's parent
     * @return xml node corresponding to this SceneGraph node
     */
    private Node serializeMeshNode(MeshNode meshNode, Node xmlParentNode) {
        Element xmlElement = createElementForNode(meshNode, xmlParentNode);
        setAttribute("fileName", meshNode.getFileName(), xmlElement);
        setAttribute("directory", meshNode.getDirectory(), xmlElement);
        setAttribute("textureRepeat", meshNode.getTextureRepeat(), Vector3.class, xmlElement);
        setAttribute("isCollidable", meshNode.isCollidable(), Boolean.class, xmlElement);
        return xmlElement;
    }

    /**
     * Serialize an ambient light node.
     *
     * @param lightNode to serialize
     * @param xmlParentNode xml node corresponding to this node's parent
     * @return xml node corresponding to this SceneGraph node
     */
    private Node serializeAmbientLightNode(Light lightNode, Node xmlParentNode) {
        Element xmlElement = createElementForNode("AmbientLight", lightNode, xmlParentNode);
        setAttribute("colour", lightNode.getColour(), Vector3.class, xmlElement);
        setAttribute("intensity", lightNode.getIntensity(), Float.class, xmlElement);
        return xmlElement;
    }

    /**
     * Serialize a directional light node.
     *
     * @param lightNode node to serialize
     * @param xmlParentNode xml node corresponding to this node's parent
     * @return xml node corresponding to this SceneGraph node
     */
    private Node serializeDirectionalLightNode(Light lightNode, Node xmlParentNode) {
        Element xmlElement = createElementForNode("DirectionalLight", lightNode, xmlParentNode);
        setAttribute("colour", lightNode.getColour(), Vector3.class, xmlElement);
        setAttribute("intensity", lightNode.getIntensity(), Float.class, xmlElement);
        setAttribute("fromDirection", lightNode.getDirection().get(), Vector3.class, xmlElement);
        return xmlElement;
    }

    /**
     * Serialize a point light node.
     *
     * @param lightNode node to serialize
     * @param xmlParentNode xml node corresponding to this node's parent
     * @return xml node corresponding to this SceneGraph node
     */
    private Node serializePointLightNode(Light lightNode, Node xmlParentNode) {
        Element xmlElement = createElementForNode("PointLight", lightNode, xmlParentNode);
        setAttribute("colour", lightNode.getColour(), Vector3.class, xmlElement);
        setAttribute("intensity", lightNode.getIntensity(), Float.class, xmlElement);
        setAttribute("falloff", lightNode.getFalloff().toString(), xmlElement);
        return xmlElement;
    }

    /**
     * Serialize a camera node.
     *
     * @param cameraNode node to serialize
     * @param xmlParentNode xml node corresponding to this node's parent
     * @return xml node corresponding to this SceneGraph node
     */
    private Node serializeCameraNode(CameraNode cameraNode, Node xmlParentNode) {
        return createElementForNode(cameraNode, xmlParentNode);
    }

    /**
     * Serialize a player node.
     *
     * @param playerNode node to serialize
     * @param xmlParentNode xml node corresponding to this node's parent
     * @return xml node corresponding to this SceneGraph node
     */
    private Node serializePlayerNode(Player playerNode, Node xmlParentNode) {
        Element xmlElement = createElementForNode(playerNode, xmlParentNode);
        setAttribute("boundingBox", playerNode.collisionNode().get().boundingBox(),
                BoundingBox.class, xmlElement);
        return xmlElement;
    }

    /**
     * Serialize a flickering light node.
     *
     * @param flickeringLightNode node to serialize
     * @param xmlParentNode xml node corresponding to this node's parent
     * @return xml node corresponding to this SceneGraph node
     */
    private Node serializeFlickeringLightNode(FlickeringLight flickeringLightNode, Node xmlParentNode) {
        Element xmlElement = createElementForNode(flickeringLightNode, xmlParentNode);
        setAttribute("directory", flickeringLightNode.mesh().get().getDirectory(), xmlElement);
        setAttribute("fileName", flickeringLightNode.mesh().get().getDirectory(), xmlElement);
        setAttribute("colour", flickeringLightNode.getColour(), Vector3.class, xmlElement);
        setAttribute("intensity", flickeringLightNode.getIntensity(), Float.class, xmlElement);
        setAttribute("falloff", flickeringLightNode.getFalloff().toString(), xmlElement);
        setAttribute("isOn", flickeringLightNode.isOn(), Boolean.class, xmlElement);
        setAttribute("intensityVariation", flickeringLightNode.getIntensityVariation(), Float.class, xmlElement);
        return xmlElement;
    }

    /**
     * Convert the given object into a string
     *
     * @param object object to convert
     * @param class0 class of the object
     * @return the result of applying the class's toString function on the object
     */
    private <T> String parseToString(T object, Class<T> class0) {
        return ParserManager.getToStringFunction(class0).apply(object);
    }

    /**
     * Set an attribute on a xmlElement with the given value.
     *
     * @param name key of the attribute
     * @param value value of the attribute
     * @param xmlElement element to set the attribute on
     */
    private void setAttribute(String name, String value, Element xmlElement) {
        xmlElement.setAttribute(name, value);
    }

    /**
     * Set an attribute on a xmlElement with the given value.
     *
     * @param name key of the attribute
     * @param object object to convert into a string which will be assigned as the value
     * @param class0 class object denoting the type of the object
     * @param xmlElement element to set the attribute on
     */
    private <T> void setAttribute(String name, T object, Class<T> class0, Element xmlElement) {
        setAttribute(name, parseToString(object, class0), xmlElement);
    }

    /**
     * Create a element which will be a child of xmlParentNode and with name assigned to tagName.
     *
     * @param tagName the name to be assigned the new element.
     * @param xmlParentNode the node which will parent the newly created node
     * @return the newly created node
     */
    private Element createElement(String tagName, Node xmlParentNode) {
        Element newElement = document.createElement(tagName);
        xmlParentNode.appendChild(newElement);
        return newElement;
    }

    /**
     * Create a element which will be a child of xmlParentNode and with name assigned to sceneNode's class's name.
     * The sceneNode's id will be assigned as an attribute.
     *
     * @param sceneNode sceneNode which will be represented by this newly created xml element.
     * @param xmlParentNode the node which will parent the newly created node.
     * @return the newly created node.
     */
    private Element createElementForNode(SceneNode sceneNode, Node xmlParentNode) {
        String tagName = sceneNode.getClass().getSimpleName();
        return createElementForNode(tagName, sceneNode, xmlParentNode);
    }

    /**
     * Create a element which will be a child of xmlParentNode and with a name equal to tagName.
     * The sceneNode's id will be assigned as an attribute.
     *
     * @param tagName name which will be set as the xml node's name
     * @param sceneNode sceneNode which will be represented by this newly created xml element.
     * @param xmlParentNode the node which will parent the newly created node.
     * @return the newly created node.
     */
    private Element createElementForNode(String tagName, SceneNode sceneNode, Node xmlParentNode) {
        Element newNode = createElement(tagName, xmlParentNode);
        newNode.setAttribute("id", sceneNode.id);
        return newNode;
    }

}
