package swen.adventure.engine.datastorage;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import swen.adventure.engine.Utilities;
import swen.adventure.engine.rendering.maths.BoundingBox;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.*;
import swen.adventure.game.scenenodes.FlickeringLight;
import swen.adventure.game.scenenodes.Player;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 04/10/15.
 */
public class SceneGraphSerializer {

    private static final ParserManager PARSER_MANAGER = new ParserManager();

    private final Document document = Utilities.createDocument();

    public static void serializeToFile(SceneNode root, File file) throws FileNotFoundException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        serializeToStream(root, fileOutputStream);
    }

    public static String serializeToString(SceneNode root) {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        serializeToStream(root, arrayOutputStream);
        return new String(arrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
    }

    public static void serializeToStream(SceneNode root, OutputStream outputStream) {
        new SceneGraphSerializer().start(root, outputStream);
    }

    private SceneGraphSerializer(){
    }

    public void start(SceneNode root, OutputStream outputStream) {
        serializeSceneNode(root, document);
        Utilities.writeOutDocument(document, outputStream, true);
    }


    private void serializeSceneNode(SceneNode sceneNode, Node xmlParentNode) {
        Node serializedNode;

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

        else
            throw new RuntimeException("Don't recognise node " + sceneNode);

        sceneNode.getChildren()
                .forEach(node -> serializeSceneNode(node, serializedNode));
    }

    private boolean isRoot(SceneNode sceneNode) {
        return sceneNode instanceof TransformNode && sceneNode.id.equals("root");
    }

    private boolean isAmbientLight(SceneNode sceneNode) {
        return sceneNode instanceof Light && ((Light) sceneNode).getType() == Light.LightType.Ambient;
    }

    private boolean isPointLight(SceneNode sceneNode) {
        return sceneNode instanceof Light && ((Light) sceneNode).getType() == Light.LightType.Point;
    }

    private boolean isDirectionalLight(SceneNode sceneNode) {
        return sceneNode instanceof Light && ((Light) sceneNode).getType() == Light.LightType.Directional;
    }

    private boolean isFlickeringLight(SceneNode sceneNode) {
        return sceneNode instanceof FlickeringLight;
    }

    private Node serializeRoot(Node xmlParentNode) {
        return createElement("root", xmlParentNode);
    }

    private Node serializeTransformNode(TransformNode transformNode, Node xmlParentNode) {
        Element xmlElement = createElementForNode(transformNode, xmlParentNode);
        setAttribute("translation", transformNode.translation(), Vector3.class, xmlElement);
        setAttribute("rotation", transformNode.rotation(), Quaternion.class, xmlElement);
        setAttribute("scale", transformNode.scale(), Vector3.class, xmlElement);
        setAttribute("isDynamic", transformNode.isDynamic(), Boolean.class, xmlElement);
        return xmlElement;
    }

    private Node serializeMeshNode(MeshNode meshNode, Node xmlParentNode) {
        Element xmlElement = createElementForNode(meshNode, xmlParentNode);
        setAttribute("fileName", meshNode.getFileName(), xmlElement);
        setAttribute("directory", meshNode.getDirectory(), xmlElement);
        setAttribute("textureRepeat", meshNode.getTextureRepeat(), Vector3.class, xmlElement);
        setAttribute("isCollidable", meshNode.isCollidable(), Boolean.class, xmlElement);


        return xmlElement;
    }

    private Node serializeGameObjectNode(GameObject gameObject, Node xmlParentNode) {
        // TODO serializeToStream different game objects
        return createElementForNode(gameObject, xmlParentNode);
    }

    private Node serializeAmbientLightNode(Light lightNode, Node xmlParentNode) {
        Element xmlElement = createElementForNode("AmbientLight", lightNode, xmlParentNode);
        setAttribute("colour", lightNode.getColour(), Vector3.class, xmlElement);
        setAttribute("intensity", lightNode.getIntensity(), Float.class, xmlElement);
        return xmlElement;
    }

    private Node serializeDirectionalLightNode(Light lightNode, Node xmlParentNode) {
        Element xmlElement = createElementForNode("DirectionalLight", lightNode, xmlParentNode);
        setAttribute("colour", lightNode.getColour(), Vector3.class, xmlElement);
        setAttribute("intensity", lightNode.getIntensity(), Float.class, xmlElement);
        setAttribute("fromDirection", lightNode.getDirection().get(), Vector3.class, xmlElement);
        return xmlElement;
    }

    private Node serializePointLightNode(Light lightNode, Node xmlParentNode) {
        Element xmlElement = createElementForNode("PointLight", lightNode, xmlParentNode);
        setAttribute("colour", lightNode.getColour(), Vector3.class, xmlElement);
        setAttribute("intensity", lightNode.getIntensity(), Float.class, xmlElement);
        setAttribute("falloff", lightNode.getFalloff().toString(), xmlElement);
        return xmlElement;
    }

    private Node serializeCameraNode(CameraNode cameraNode, Node xmlParentNode) {
        return createElementForNode(cameraNode, xmlParentNode);
    }

    private Node serializePlayerNode(Player playerNode, Node xmlParentNode) {
        Element xmlElement = createElementForNode(playerNode, xmlParentNode);
        setAttribute("boundingBox", playerNode.collisionNode().get().boundingBox(),
                BoundingBox.class, xmlElement);
        return xmlElement;
    }

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

    private <T> String parseToString(T object, Class<T> class0) {
        return PARSER_MANAGER.getToStringFunction(class0).apply(object);
    }

    private void setAttribute(String name, String value, Element xmlElement) {
        xmlElement.setAttribute(name, value);
    }

    private <T> void setAttribute(String name, T object, Class<T> class0, Element xmlElement) {
        setAttribute(name, parseToString(object, class0), xmlElement);
    }

    private Element createElement(String tagName, Node xmlParentNode) {
        Element newElement = document.createElement(tagName);
        xmlParentNode.appendChild(newElement);
        return newElement;
    }

    private Element createElementForNode(SceneNode sceneNode, Node xmlParentNode) {
        String tagName = sceneNode.getClass().getSimpleName();
        return createElementForNode(tagName, sceneNode, xmlParentNode);
    }

    private Element createElementForNode(String tagName, SceneNode sceneNode, Node xmlParentNode) {
        Element newNode = createElement(tagName, xmlParentNode);
        newNode.setAttribute("id", sceneNode.id);
        return newNode;
    }

}
