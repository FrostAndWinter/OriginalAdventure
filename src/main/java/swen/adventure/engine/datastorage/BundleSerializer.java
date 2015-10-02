package swen.adventure.engine.datastorage;

import org.w3c.dom.*;
import swen.adventure.engine.Utilities;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 18/09/15.
 */
public class BundleSerializer {

    private static final ParserManager PARSER_MANAGER = new ParserManager();

    public String toXml(BundleObject bundleObject) {
        Document document = Utilities.createDocument();
        writeBundleObject(document, document, bundleObject);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Utilities.writeOutDocument(document, os);
        return new String(os.toByteArray(), StandardCharsets.UTF_8);
    }

    public void toXmlFile(BundleObject bundleObject, File file) throws FileNotFoundException {
        String xml = toXml(bundleObject);
        try (PrintStream ps = new PrintStream(file)) {
            ps.println(xml);
            ps.flush();
        }
    }

    public BundleObject fromXml(String xml) {
        InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        return fromXml(is);
    }

    public BundleObject fromXml(File file) throws FileNotFoundException {
        InputStream is = new FileInputStream(file);
        return fromXml(is);
    }

    public BundleObject fromXml(InputStream is) {
        Document document = Utilities.loadExistingXmlDocument(is);
        document.getDocumentElement().normalize();
        return readBundleObject(document.getFirstChild());
    }

    private static BundleObject readBundleObject(Node node) {
        NodeList nList = node.getChildNodes();
        Map<String, Property> storedValues = new HashMap<>();
        for (int i = 0; i < nList.getLength(); i++) {
            Property property = loadProperty(nList.item(i));
            storedValues.put(property.name, property);
        }
        return new BundleObject(storedValues);
    }

    private static Property loadProperty(Node node) {
        NamedNodeMap propertyNodeMap = node.getAttributes();
        String name = propertyNodeMap.getNamedItem("name").getNodeValue();

        Node valueNode = node.getChildNodes().item(0);
        NamedNodeMap valueTypeNodeMap = valueNode.getAttributes();
        String type = valueTypeNodeMap.getNamedItem("type").getNodeValue();

        Node value = valueNode.getChildNodes().item(0);
        try {
            Class<?> class0 = Class.forName(type);

            Object instance;
            if(class0 == BundleObject.class)
                instance = readBundleObject(value);
            else if(class0 == BundleArray.class)
                instance = readBundleArray(value);
            else
                instance = PARSER_MANAGER.convertFromString(value.getTextContent(), class0);

            return new Property(name, instance, class0);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static BundleArray readBundleArray(Node node) {
        NodeList nList = node.getChildNodes();
        List<Property> storedValues = new ArrayList<>();
        for (int i = 0; i < nList.getLength(); i++) {
            Property property = loadProperty(nList.item(i));
            storedValues.add(property);
        }
        return new BundleArray(storedValues);
    }

    private static void writeBundleObject(Node parent, Document document, BundleObject bundleObject){
        Element root = document.createElement("BundleObject");
        parent.appendChild(root);

        bundleObject.getProperties()
                .forEach(property -> addProperty(property, document, root));
    }

    private static void writeBundleArray(Node parent, Document document, BundleArray bundleArray) {
        Element root = document.createElement("BundleArray");
        parent.appendChild(root);

        bundleArray.getProperties()
                .forEach(property -> addProperty(property, document, root));
    }

    private static void addProperty(Property property, Document document, Element parent){
        Element propertyElem = document.createElement("property");
        parent.appendChild(propertyElem);
        propertyElem.setAttribute("name", property.name);

        Element valueElem = document.createElement("value");
        propertyElem.appendChild(valueElem);
        valueElem.setAttribute("type", property.class0.getCanonicalName());

        if(property.class0 == BundleObject.class) {
            writeBundleObject(valueElem, document, (BundleObject)property.value);
        } else if(property.class0 == BundleArray.class) {
            writeBundleArray(valueElem, document, (BundleArray) property.value);
        } else {
            @SuppressWarnings("unchecked")
            Node valueText = document.createTextNode(PARSER_MANAGER.convertToString(property.value, (Class)property.class0));
            valueElem.appendChild(valueText);
        }
    }


}
