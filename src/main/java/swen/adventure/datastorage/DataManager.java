package swen.adventure.datastorage;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Liam O'Neill, Student ID 300312734, on 18/09/15.
 */
public class DataManager {

    private static final ParserManager PARSER_MANAGER = new ParserManager();

    public String toXml(BundleObject bundleObject) {
        Document document = createNewDocument();
        writeBundleObject(document, document, bundleObject);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        writeOut(document, os);
        return new String(os.toByteArray(), StandardCharsets.UTF_8);
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
        Document document = createExistingDocument(is);
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
                instance = null;
            else
                instance = PARSER_MANAGER.convertFromString(value.getTextContent(), class0);

            return new Property(name, instance, class0);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeBundleObject(Node parent, Document doc, BundleObject bundleObject){
        Element root = doc.createElement("BundleObject");
        parent.appendChild(root);

        bundleObject.getProperties()
                .forEach(property -> addProperty(property, doc, root));
    }

    private static void writeBundleArray(Node parent, Document document, BundleArray value) {

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
            Node valueText = document.createTextNode(PARSER_MANAGER.convertToString(property.value, property.class0));
            valueElem.appendChild(valueText);
        }
    }

    private static Document createNewDocument() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            return docBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private static Document createExistingDocument(InputStream is) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            return docBuilder.parse(is);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeOut(Document doc, OutputStream os) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            //transformerFactory.setAttribute("indent-number", 2);
            Transformer transformer = transformerFactory.newTransformer();
            //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(os);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }
}
