package swen.adventure.datastorage;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import swen.adventure.Utilities;
import swen.adventure.rendering.maths.Quaternion;
import swen.adventure.rendering.maths.Vector3;
import swen.adventure.scenegraph.TransformNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 22/09/15.
 */
public class WorldDataManager {

    private static final ParserManager PARSER_MANAGER = new ParserManager();

    public TransformNode parseTransformNode(String input){
        InputStream is = Utilities.stringToInputStream(input);
        return parseTransformNode(is);
    }

    public TransformNode parseTransformNode(File inputFile) throws FileNotFoundException {
        InputStream is = Utilities.fileToInputStream(inputFile);
        return parseTransformNode(is);
    }

    private TransformNode parseTransformNode(InputStream is){
        Document doc = Utilities.loadExistingXmlDocument(is);
        Optional<TransformNode> parent = Optional.empty();
        Node transformNode = doc.getFirstChild();
        return parseTransformNodeFromXmlNode(transformNode, Optional.empty());
    }

    private static TransformNode parseTransformNodeFromXmlNode(Node node, Optional<TransformNode> parent) {
        String id = getAttribute("id", node, Function.identity());
        Vector3 translation = getAttribute("translation", node, PARSER_MANAGER.getFromStringFunction(Vector3.class));
        Quaternion rotation = getAttribute("rotation", node, PARSER_MANAGER.getFromStringFunction(Quaternion.class));
        Vector3 scale = getAttribute("scale", node, PARSER_MANAGER.getFromStringFunction(Vector3.class));

        if(parent.isPresent()){
           boolean isDynamic = getAttribute("isDynamic", node, PARSER_MANAGER.getFromStringFunction(Boolean.class));
        }

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    private static <T> T getAttribute(String name, Node node, Function<String, T> converter) {
        NamedNodeMap attributes = node.getAttributes();
        String value = attributes.getNamedItem(name).getTextContent();
        return converter.apply(value);
    }

}
